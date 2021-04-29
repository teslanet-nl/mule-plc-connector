/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 (teslanet.nl) Rogier Cobben
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;


public class MulePlcModbusOperationsTest extends AbstractPlcTestCase
{
    private final Logger LOGGER= LoggerFactory.getLogger( MulePlcModbusOperationsTest.class );

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     */
    @Override
    protected String getConfigFile()
    {
        return "testapps/basic-modbus.xml";
    }

    @Ignore
    @Test
    public void executePingOperation() throws Exception
    {
        Boolean payloadValue= ( (Boolean) flowRunner( "basic-ping" ).run().getMessage().getPayload().getValue() );
        assertTrue( "ping failed", payloadValue );
    }

    @Ignore
    @Test
    public void executeReadOperation() throws Exception
    {
        String payloadValue= (String) flowRunner( "basic-read" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        Diff diff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/modbus_response_read_1.xml" ) ).withTest(
            payloadValue ).ignoreComments().ignoreWhitespace().build();
        assertFalse( diff.toString(), diff.hasDifferences() );
    }

    @Ignore
    @Test(timeout= 600000)
    public void executeMultipleReadOperation() throws InterruptedException
    {
        String payloadValue= null;
        try
        {
            payloadValue= (String) flowRunner( "basic-read" ).run().getMessage().getPayload().getValue();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread.sleep( 5000 );
        LOGGER.info( "do" );
        try
        {
            payloadValue= (String) flowRunner( "basic-read" ).run().getMessage().getPayload().getValue();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread.sleep( 5000 );
        LOGGER.info( "do" );
        try
        {
            payloadValue= (String) flowRunner( "basic-read" ).run().getMessage().getPayload().getValue();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread.sleep( 5000 );
        LOGGER.info( "do" );
        try
        {
            payloadValue= (String) flowRunner( "basic-read" ).run().getMessage().getPayload().getValue();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Thread.sleep( 5000 );
        LOGGER.info( "do" );
        try
        {
            payloadValue= (String) flowRunner( "basic-read" ).run().getMessage().getPayload().getValue();
        }
        catch ( Exception e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull( payloadValue );
    }

    @Ignore
    @Test
    public void executeWriteOperation() throws Exception
    {
        String payloadValue= (String) flowRunner( "basic-write" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        Diff diff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/modbus_response_write_1.xml" ) ).withTest(
            payloadValue ).ignoreComments().ignoreWhitespace().build();
        assertFalse( diff.toString(), diff.hasDifferences() );
    }

    @Ignore
    @Test
    public void executeWriteWatchdogReset() throws Exception
    {
        String payloadValue= (String) flowRunner( "basic-write-watchdog-reset1" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        payloadValue= (String) flowRunner( "basic-write-watchdog-reset2" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        payloadValue= (String) flowRunner( "basic-write-true" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        Diff diff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/modbus_response_write_1.xml" ) ).withTest(
            payloadValue ).ignoreComments().ignoreWhitespace().build();
        assertFalse( diff.toString(), diff.hasDifferences() );
    }

    @Ignore
    @Test
    public void executeParallelWriteOperation() throws Exception
    {
        String payloadValue= (String) flowRunner( "basic-write-watchdog-reset1" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        payloadValue= (String) flowRunner( "basic-write-watchdog-reset2" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        payloadValue= (String) flowRunner( "parallel-write" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        try
        {
            await().atMost( 1, TimeUnit.SECONDS ).untilTrue( new AtomicBoolean( false ) );
        }
        catch ( Exception e )
        {
        }

        //Diff diff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/modbus_response_write_1.xml" ) ).withTest( payloadValue ).ignoreComments().ignoreWhitespace().build();
        //assertFalse( diff.toString(), diff.hasDifferences() );
    }
    
    @Ignore
    @Test
    public void executeSequentialWriteOperation() throws Exception
    {
        String payloadValue= (String) flowRunner( "basic-write-watchdog-reset1" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        payloadValue= (String) flowRunner( "basic-write-watchdog-reset2" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        payloadValue= (String) flowRunner( "sequential-write" ).run().getMessage().getPayload().getValue();
        LOGGER.info( payloadValue );
        try
        {
            //await().atMost( 10, TimeUnit.SECONDS ).untilTrue( new AtomicBoolean( false ) );
        }
        catch ( Exception e )
        {
        }

        //Diff diff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/modbus_response_write_1.xml" ) ).withTest( payloadValue ).ignoreComments().ignoreWhitespace().build();
        //assertFalse( diff.toString(), diff.hasDifferences() );
    }

}
