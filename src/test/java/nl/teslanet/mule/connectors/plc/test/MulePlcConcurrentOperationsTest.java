/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2022 (teslanet.nl) Rogier Cobben
 * 
 * Contributors:
 *     (teslanet.nl) Rogier Cobben - initial creation
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package nl.teslanet.mule.connectors.plc.test;


import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.mock.connection.MockConnection;
import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.internal.MulePlcConnectionProvider;
import nl.teslanet.mule.connectors.plc.test.utils.TestPlc;


public class MulePlcConcurrentOperationsTest extends AbstractPlcTestCase
{
    @SuppressWarnings( "unused" )
    private final Logger logger= LoggerFactory.getLogger( MulePlcConcurrentOperationsTest.class );

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     * @throws PlcConnectionException when connection failed
     */
    @Override
    protected String getConfigFile()
    {
        return "testapps/concurrent.xml";
    }

    @Before
    public void settings()
    {
        setDisposeContextPerClass( true );
    }

    /**
     * Setup the PLC mock.
     * @throws PlcConnectionException When retrieving the mock connection failed.
     */
    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        //super.doSetUpBeforeMuleContextCreation();
        ClassLoader classLoader= getExecutionClassLoader();
        //ClassLoader classLoader= getConnectorClassLoader().get();
        //        //Class<?> drivermanagerClass= Class.forName( "org.apache.plc4x.java.PlcDriverManager", true, classLoader );
        //        Class<?> drivermanagerClass= classLoader.loadClass("org.apache.plc4x.java.PlcDriverManager");
        //        PlcDriverManager driverManager = (PlcDriverManager) drivermanagerClass.newInstance();
        //        Method getConnection = drivermanagerClass.getDeclaredMethod("getConnection", String.class);
        //        MockConnection connection= (MockConnection) getConnection.invoke( driverManager, "mock:test-plc");
        //        connection.setDevice( new TestPlc() );
        //        ClassLoader actualClassLoader= connection.getClass().getClassLoader();

        ClassLoader contextClassLoader= Thread.currentThread().getContextClassLoader();
        try
        {
            //Thread.currentThread().setContextClassLoader( classLoader );
            //PlcDriverManager driverManager= new PlcDriverManager( classLoader );
            PlcDriverManager driverManager= MulePlcConnectionProvider.getDrivermanager();
            MockConnection connection= (MockConnection) driverManager.getConnection( "mock:test-plc" );
            ClassLoader actualClassLoader= connection.getClass().getClassLoader();
            connection.setDevice( new TestPlc() );
        }
        finally
        {
            //Thread.currentThread().setContextClassLoader( contextClassLoader );
        }

        //       PlcDriverManager driverManager = new PlcDriverManager();
        //      MockConnection connection= (MockConnection) driverManager.getConnection( "mock:test-plc");
        //      connection.setDevice( new TestPlc() );
        //      ClassLoader actualClassLoader= connection.getClass().getClassLoader();
    }

    /**
     * Test the ping operation. (MockDriver does not support ping).
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentPingOperation() throws Exception
    {
        Message message= flowRunner( "concurrent-ping" ).run().getMessage();
        assertEquals( "wrong ping result expected of responses", "false", getPayloadAsString( message ) );
    }

    /**
     * Test the read operation.
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentReadOperation() throws Exception
    {
        Message message= flowRunner( "concurrent-read" ).keepStreamsOpen().run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 16, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            Message retieved= flowRunner( "concurrent-read-retrieve" ).keepStreamsOpen().run().getMessage();
            @SuppressWarnings( "unchecked" )
            Map< String, Object > responses= (Map< String, Object >) retieved.getPayload().getValue();
            return responses.size() >= 4;
        } );

        message= flowRunner( "concurrent-read-retrieve" ).keepStreamsOpen().run().getMessage();
        @SuppressWarnings( "unchecked" )
        Map< String, Object > responses= (Map< String, Object >) message.getPayload().getValue();
        assertEquals( "wrong number of responses", 4, responses.size() );
        for ( Entry< ? , ? > response : responses.entrySet() )
        {
            String payloadValue= new String( (byte[]) response.getValue(), StandardCharsets.UTF_8 );
            assertThat( payloadValue, hasXPath( "/plcReadResponse/field[@alias = 'one' and @responseCode = 'OK']/values/value[text() = 'empty'] " ) );
            assertThat( payloadValue, hasXPath( "/plcReadResponse/field[@alias = 'two' and @responseCode = 'OK']/values/value[text() = 'empty'] " ) );
        }
    }

    /**
     * Test the write operation.
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentWriteOperation() throws Exception
    {
        Message message= flowRunner( "concurrent-write" ).keepStreamsOpen().run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 16, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            Message retieved= flowRunner( "concurrent-write-retrieve" ).keepStreamsOpen().run().getMessage();
            @SuppressWarnings( "unchecked" )
            Map< String, Object > responses= (Map< String, Object >) retieved.getPayload().getValue();
            return responses.size() >= 4;
        } );

        message= flowRunner( "concurrent-write-retrieve" ).keepStreamsOpen().run().getMessage();
        @SuppressWarnings( "unchecked" )
        Map< String, Object > responses= (Map< String, Object >) message.getPayload().getValue();
        assertEquals( "wrong number of responses", 4, responses.size() );
        for ( Entry< ? , ? > response : responses.entrySet() )
        {
            String payloadValue= new String( (byte[]) response.getValue(), StandardCharsets.UTF_8 );
            assertThat( payloadValue, hasXPath( "/plcWriteResponse/field[@alias = 'one' and @responseCode = 'OK']" ) );
            assertThat( payloadValue, hasXPath( "/plcWriteResponse/field[@alias = 'two' and @responseCode = 'OK']" ) );
        }
    }

    /**
     * Test the write operation.
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentSubscribeOperation() throws Exception
    {
        Message message= flowRunner( "concurrent-subscribe" ).keepStreamsOpen().run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 10, TimeUnit.SECONDS ).pollInterval( 2, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            Message retieved= flowRunner( "concurrent-subscribe-retrieve" ).keepStreamsOpen().run().getMessage();
            @SuppressWarnings( "unchecked" )
            Map< String, Object > responses= (Map< String, Object >) retieved.getPayload().getValue();
            return responses.size() >= 4;
        } );

        message= flowRunner( "concurrent-subscribe-retrieve" ).keepStreamsOpen().run().getMessage();
        @SuppressWarnings( "unchecked" )
        Map< String, Object > responses= (Map< String, Object >) message.getPayload().getValue();
        assertEquals( "wrong number of responses", 4, responses.size() );
        for ( Entry< ? , ? > response : responses.entrySet() )
        {
            String payloadValue= new String( (byte[]) response.getValue(), StandardCharsets.UTF_8 );
            assertThat( payloadValue, hasXPath( "/plcSubscribeResponse/field[@alias = 'one' and @responseCode = 'OK']" ) );
            assertThat( payloadValue, hasXPath( "/plcSubscribeResponse/field[@alias = 'two' and @responseCode = 'OK']" ) );
        }
        /*
        message= flowRunner( "concurrent-write" ).keepStreamsOpen().run().getMessage();
        await( "retrieve responses" ).pollDelay( 10, TimeUnit.SECONDS ).pollInterval( 2, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            Message retieved= flowRunner( "concurrent-write-retrieve" ).keepStreamsOpen().run().getMessage();
            @SuppressWarnings( "unchecked" )
            Map< String, Object > responses2= (Map< String, Object >) retieved.getPayload().getValue();
            return responses.size() >= 4;
        } );
        message= flowRunner( "concurrent-read" ).keepStreamsOpen().run().getMessage();
        await( "retrieve responses" ).pollDelay( 10, TimeUnit.SECONDS ).pollInterval( 2, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            Message retieved= flowRunner( "concurrent-read-retrieve" ).keepStreamsOpen().run().getMessage();
            @SuppressWarnings( "unchecked" )
            Map< String, Object > responses3= (Map< String, Object >) retieved.getPayload().getValue();
            return responses.size() >= 4;
        } );
        message= flowRunner( "concurrent-read-retrieve" ).keepStreamsOpen().run().getMessage();
        String result= getPayloadAsString( message );
        */
    }

    /**
     * Read resource as string.
     *
     * @param resourcePath the resource path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String readResourceAsString( String resourcePath ) throws IOException
    {
        return IOUtils.getResourceAsString( resourcePath, this.getClass() );
    }
}
