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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.junit.Test;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;

import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.test.utils.MuleEventSpy;
import nl.teslanet.mule.connectors.plc.test.utils.TestUtils;


public class MulePlcSubscribeTest extends AbstractPlcTestCase
{
    @SuppressWarnings( "unused" )
    private final Logger logger= LoggerFactory.getLogger( MulePlcSubscribeTest.class );

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     * @throws PlcConnectionException when connection failed
     */
    @Override
    protected String getConfigFile()
    {
        return "testapps/subscribe.xml";
    }

    /**
     * Test the subscribe operation and the production of events.
     * @throws Exception When an error occurs.
     */
    @Test
    public void subscribeOperation() throws Exception
    {
        MuleEventSpy spy1= new MuleEventSpy( "handler1a" );
        spy1.clear();
        MuleEventSpy spy2= new MuleEventSpy( "handler1b" );
        spy2.clear();

        String response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        Diff writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 0;
        } );
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 0;
        } );

        //subscribe
        response= TestUtils.toString( flowRunner( "subscribe-subscribe" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscribe_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //second write
        response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 1;
        } );
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 1;
        } );

        Message spyMessage= (Message) spy1.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        ReceivedResponseAttributes attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        String payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        Diff eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_1.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        spyMessage= (Message) spy2.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_1.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        //third write
        response= TestUtils.toString( flowRunner( "subscribe-write-2" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_2.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 2;
        } );
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 2;
        } );

        spyMessage= (Message) spy1.getEvents().get( 1 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        spyMessage= (Message) spy2.getEvents().get( 1 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );
    }

    /**
     * Test the subscribe operation and the production of events.
     * @throws Exception When an error occurs.
     */
    @Test
    public void subscribeOperationSeparate() throws Exception
    {
        MuleEventSpy spy1= new MuleEventSpy( "handler1a" );
        spy1.clear();
        MuleEventSpy spy2= new MuleEventSpy( "handler2" );
        spy2.clear();

        String response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        Diff writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 0;
        } );
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 0;
        } );

        //subscribe 2a
        response= TestUtils.toString( flowRunner( "subscribe-subscribe-2a" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscribe_response_2a.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //subscribe 2b
        response= TestUtils.toString( flowRunner( "subscribe-subscribe-2b" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscribe_response_2b.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //second write
        response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 1;
        } );
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 0;
        } );

        Message spyMessage= (Message) spy1.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        ReceivedResponseAttributes attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        String payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        Diff eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_1.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        //third write
        response= TestUtils.toString( flowRunner( "subscribe-write-2" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_2.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 1;
        } );
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 1;
        } );

        spyMessage= (Message) spy2.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );
    }

    /**
     * Test the unsubscribe operation and the ending of production of events.
     * @throws Exception When an error occurs.
     */
    @Test
    public void unsubscribeOperation() throws Exception
    {
        MuleEventSpy spy1= new MuleEventSpy( "handler1a" );
        spy1.clear();
        MuleEventSpy spy2= new MuleEventSpy( "handler1b" );
        spy2.clear();

        String response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        Diff writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 0;
        } );
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 0;
        } );

        //subscribe
        response= TestUtils.toString( flowRunner( "subscribe-subscribe" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscribe_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //second write
        response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 1;
        } );
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 1;
        } );

        Message spyMessage= (Message) spy1.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        ReceivedResponseAttributes attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        String payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        Diff eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_1.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        spyMessage= (Message) spy2.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_1.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        //third write
        response= TestUtils.toString( flowRunner( "subscribe-write-2" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_2.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 2;
        } );
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 2;
        } );

        spyMessage= (Message) spy1.getEvents().get( 1 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        spyMessage= (Message) spy2.getEvents().get( 1 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        //unsubscribe
        response= TestUtils.toString( flowRunner( "subscribe-unsubscribe" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/unsubscribe_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //fourth write
        response= TestUtils.toString( flowRunner( "subscribe-write-3" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_3.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "fourth write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 3, TimeUnit.SECONDS ).until( () -> {
            return spy1.getEvents().size() == 2;
        } );
        await( "fourth write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 3, TimeUnit.SECONDS ).until( () -> {
            return spy2.getEvents().size() == 2;
        } );
    }

    /**
     * Test partial unsubscribe operation.
     * @throws Exception When an error occurs.
     */
    @Test
    public void partialUnsubscribeOperation() throws Exception
    {
        MuleEventSpy spy= new MuleEventSpy( "handler1a" );
        spy.clear();

        String response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        Diff writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "first write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy.getEvents().size() == 0;
        } );

        //subscribe
        response= TestUtils.toString( flowRunner( "subscribe-subscribe" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscribe_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //second write
        response= TestUtils.toString( flowRunner( "subscribe-write-1" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "second write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy.getEvents().size() == 1;
        } );

        Message spyMessage= (Message) spy.getEvents().get( 0 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        ReceivedResponseAttributes attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        String payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        Diff eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_1.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        //third write
        response= TestUtils.toString( flowRunner( "subscribe-write-2" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_2.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "third write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy.getEvents().size() == 2;
        } );

        spyMessage= (Message) spy.getEvents().get( 1 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );

        //unsubscribe
        response= TestUtils.toString( flowRunner( "subscribe-unsubscribe-partial" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/unsubscribe_response_1.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //fourth write
        response= TestUtils.toString( flowRunner( "subscribe-write-3" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_3.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "fourth write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy.getEvents().size() == 2;
        } );

        //fifth write
        response= TestUtils.toString( flowRunner( "subscribe-write-2" ).run().getMessage().getPayload().getValue() );
        TestUtils.validate( response );
        writeDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_write_response_2.xml" ) ).withTest(
            response
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        //let handler do its asynchronous work, if any
        await( "fifth write" ).pollDelay( 100, TimeUnit.MILLISECONDS ).atMost( 2, TimeUnit.SECONDS ).until( () -> {
            return spy.getEvents().size() == 3;
        } );

        spyMessage= (Message) spy.getEvents().get( 2 ).getContent();
        assertEquals(
            "wrong attributes class",
            new TypedValue< ReceivedResponseAttributes >( new ReceivedResponseAttributes( true ), null ).getClass(),
            spyMessage.getAttributes().getClass()
        );
        attributes= (ReceivedResponseAttributes) spyMessage.getAttributes().getValue();
        assertTrue( "wrong request code", attributes.isSuccess() );
        payloadValue= (String) spyMessage.getPayload().getValue();
        TestUtils.validate( payloadValue );
        eventDiff= DiffBuilder.compare( TestUtils.readResourceAsString( "testpayloads/subscription_event_2.xml" ) ).withTest(
            payloadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertEventHasTsDiffOnly( eventDiff );
    }

    /**
     * Assert that the only difference in an event is time stamp.
     * @param difference The difference to assert.
     */
    private void assertEventHasTsDiffOnly( Diff difference )
    {
        assertTrue( difference.toString(), difference.hasDifferences() );
        difference.getDifferences().forEach( diff -> {
            if ( "/plcEvent[1]/@ts".equals( diff.getComparison().getControlDetails().getXPath() ) )
            {
                assertEquals( "no difference", ComparisonResult.DIFFERENT, diff.getResult() );
                assertEquals( "wrong diff type", ComparisonType.ATTR_VALUE, diff.getComparison().getType() );
            }
            else
            {
                assertFalse( "unexpected difference", true );
            }
        } );
    }

}
