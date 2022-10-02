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
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.mock.connection.MockConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mule.runtime.api.message.Message;
import org.mule.test.runner.RunnerDelegateTo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.internal.MulePlcConnectionProvider;
import nl.teslanet.mule.connectors.plc.test.utils.MuleEventSpy;
import nl.teslanet.mule.connectors.plc.test.utils.MuleEventSpy.Event;
import nl.teslanet.mule.connectors.plc.test.utils.TestPlc;
import nl.teslanet.mule.connectors.plc.test.utils.TestUtils;


//@Ignore
@RunnerDelegateTo( Parameterized.class )
public class MulePlcConcurrentOperationsTest extends AbstractPlcTestCase
{
    @SuppressWarnings( "unused" )
    private final Logger LOGGER= LoggerFactory.getLogger( MulePlcConcurrentOperationsTest.class );

    /**
     * The list of tests with their parameters
     * @return Test parameters.
     */
    @Parameters( name= "config= {0}" )
    public static Collection< Object[] > data()
    {
        return Arrays.asList(
            new Object [] []
            {
                { "testapps/concurrent-io.xml" },
                { "testapps/concurrent-io-ping.xml" },
                { "testapps/concurrent-io-read.xml" },
                { "testapps/concurrent-io-write.xml" },
                { "testapps/concurrent-io-subscribe.xml" },
                { "testapps/concurrent-ping.xml" },
                { "testapps/concurrent-read.xml" },
                { "testapps/concurrent-write.xml" },
                { "testapps/concurrent-subscribe.xml" },
                { "testapps/concurrent-default.xml" } }
        );
    }

    /**
     * The mule flow to call.
     */
    @Parameter( 0 )
    public String config;

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     * @throws PlcConnectionException when connection failed
     */
    @Override
    protected String[] getConfigFiles()
    {
        String[] configs= { "testapps/concurrent.xml", config };
        return configs;
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
        PlcDriverManager driverManager= MulePlcConnectionProvider.getDrivermanager();
        MockConnection connection= (MockConnection) driverManager.getConnection( "mock:test-plc" );
        connection.setDevice( new TestPlc() );
    }

    /**
     * Test the ping operation. (MockDriver does not support ping).
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentPingOperation() throws Exception
    {
        MuleEventSpy spy= new MuleEventSpy( "ping-spy" );
        spy.clear();
        flowRunner( "concurrent-ping" ).run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 1, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return spy.getEvents().size() >= 4;
        } );

        assertEquals( "wrong number of responses", 4, spy.getEvents().size() );
        for ( Event response : spy.getEvents() )
        {
            Object payloadValue= ( (Message) response.getContent() ).getPayload().getValue();
            assertEquals( "response has wrong value", "UNSUPPORTED ERROR", payloadValue );
        }
    }

    /**
     * Test the read operation.
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentReadOperation() throws Exception
    {
        MuleEventSpy spy= new MuleEventSpy( "read-spy" );
        spy.clear();
        flowRunner( "concurrent-write-single" ).run().getMessage();
        flowRunner( "concurrent-read" ).run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 1, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return spy.getEvents().size() >= 4;
        } );

        assertEquals( "wrong number of responses", 4, spy.getEvents().size() );
        for ( Event response : spy.getEvents() )
        {
            String payloadValue= new String( (byte[]) ( (Message) response.getContent() ).getPayload().getValue(), StandardCharsets.UTF_8 );
            TestUtils.validate( payloadValue );
            assertThat(
                payloadValue,
                anyOf(
                    hasXPath( "/plcReadResponse/field[@alias = 'one' and @responseCode = 'OK']/values/value[text() = 'true'] " ),
                    hasXPath( "/plcReadResponse/field[@alias = 'one' and @responseCode = 'OK']/value[text() = 'true'] " )
                )
            );
            assertThat(
                payloadValue,
                anyOf(
                    hasXPath( "/plcReadResponse/field[@alias = 'two' and @responseCode = 'OK']/values/value[text() = 'false']" ),
                    hasXPath( "/plcReadResponse/field[@alias = 'two' and @responseCode = 'OK']/value[text() = 'false'] " )
                )
            );
        }
    }

    /**
     * Test the write operation.
     * @throws Exception When an error occurs.
     */
    @Test
    public void concurrentWriteOperation() throws Exception
    {
        MuleEventSpy spy= new MuleEventSpy( "write-spy" );
        spy.clear();
        flowRunner( "concurrent-write" ).run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 1, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return spy.getEvents().size() >= 4;
        } );

        assertEquals( "wrong number of responses", 4, spy.getEvents().size() );
        for ( Event response : spy.getEvents() )
        {
            String payloadValue= new String( (byte[]) ( (Message) response.getContent() ).getPayload().getValue(), StandardCharsets.UTF_8 );
            TestUtils.validate( payloadValue );
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
        MuleEventSpy subscribeSpy= new MuleEventSpy( "subscribe-spy" );
        subscribeSpy.clear();
        MuleEventSpy unsubscribeSpy= new MuleEventSpy( "unsubscribe-spy" );
        unsubscribeSpy.clear();
        MuleEventSpy eventSpy= new MuleEventSpy( "event-spy" );
        eventSpy.clear();
        flowRunner( "concurrent-subscribe" ).run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 1, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return subscribeSpy.getEvents().size() >= 4;
        } );

        assertEquals( "wrong number of responses", 4, subscribeSpy.getEvents().size() );
        for ( Event response : subscribeSpy.getEvents() )
        {
            String payloadValue= new String( (byte[]) ( (Message) response.getContent() ).getPayload().getValue(), StandardCharsets.UTF_8 );
            TestUtils.validate( payloadValue );
            assertThat( payloadValue, hasXPath( "/plcSubscribeResponse/field[@alias = 'one' and @responseCode = 'OK']" ) );
            assertThat( payloadValue, hasXPath( "/plcSubscribeResponse/field[@alias = 'two' and @responseCode = 'OK']" ) );
        }

        eventSpy.clear();
        flowRunner( "concurrent-write-single" ).run().getMessage();

        await( "retrieve events" ).pollDelay( 1, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return eventSpy.getEvents().size() >= 2;
        } );
        assertTrue( "wrong number of events", eventSpy.getEvents().size() >= 2 );
        for ( Event response : eventSpy.getEvents() )
        {
            String payloadValue= new String( (byte[]) ( (Message) response.getContent() ).getPayload().getValue(), StandardCharsets.UTF_8 );
            TestUtils.validate( payloadValue );
            assertThat(
                payloadValue,
                anyOf(
                    hasXPath( "/plcEvent/field[@alias = 'STATE/address_one:BOOL' and @responseCode = 'OK' and @type='BOOL']/values/value[@key='value' and text() = 'true'] " ),
                    hasXPath( "/plcEvent/field[@alias = 'STATE/address_two:BOOL' and @responseCode = 'OK' and @type='BOOL']/values/value[@key='value' and text() = 'false'] " ),
                    hasXPath( "/plcEvent/field[@alias = 'one' and @responseCode = 'OK' ]/value[text() = 'true'] " ),
                    hasXPath( "/plcEvent/field[@alias = 'two' and @responseCode = 'OK' ]/value[text() = 'false'] " )
                )
            );
        }

        flowRunner( "concurrent-unsubscribe" ).run().getMessage();
        //let handler do its asynchronous work, if any
        await( "retrieve responses" ).pollDelay( 1, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return unsubscribeSpy.getEvents().size() >= 4;
        } );

        assertEquals( "wrong number of responses", 4, unsubscribeSpy.getEvents().size() );
        for ( Event response : unsubscribeSpy.getEvents() )
        {
            String payloadValue= new String( (byte[]) ( (Message) response.getContent() ).getPayload().getValue(), StandardCharsets.UTF_8 );
            TestUtils.validate( payloadValue );
            assertThat( payloadValue, hasXPath( "/plcUnsubscribeResponse" ) );
        }

        eventSpy.clear();
        flowRunner( "concurrent-write-single" ).run().getMessage();

        await( "retrieve events" ).pollDelay( 2, TimeUnit.SECONDS ).pollInterval( 1, TimeUnit.SECONDS ).atMost( 10, TimeUnit.MINUTES ).until( () -> {
            return eventSpy.getEvents().size() >= 0;
        } );
        assertEquals( "wrong number of events", 0, eventSpy.getEvents().size() );

    }
}
