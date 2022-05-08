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
package nl.teslanet.mule.connectors.plc.internal;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer.XmlSerializerResult;


/**
 * Event processor.
 */
public class EventProcessor implements Consumer< PlcSubscriptionEvent >
{
    /**
     * The logger of this class.
     */
    private static final Logger logger= LoggerFactory.getLogger( EventProcessor.class );

    /**
     * Name of the handler.
     */
    private String handlerName= null;

    /**
     * The list of response handlers
     */
    private CopyOnWriteArrayList< EventListener > listeners= new CopyOnWriteArrayList<>();

    /**
     * @param handlerName
     */
    public EventProcessor( String handlerName )
    {
        super();
        this.handlerName= handlerName;
    }

    /**
     * @return The handler name.
     */
    public String getHandlerName()
    {
        return handlerName;
    }

    /**
     * Add listener to process events.
     */
    synchronized void addListener( EventListener listener )
    {
        listeners.add( listener );
    }

    /**
     * Remove a listener
     */
    void removeListener( EventListener listener )
    {
        listeners.remove( listener );
    }

    /**
     * Get listeners.
     * @return The listener of this handler.
     */
    public List< EventListener > getListeners()
    {
        return listeners;
    }

    /**
     * Register this as handler for the subscription.
     * @param subscriptionName the name of the subscription.
     */
    public synchronized void register( PlcSubscriptionResponse subscriptionResponse )
    {
        for ( PlcSubscriptionHandle handle : subscriptionResponse.getSubscriptionHandles() )
        {
            handle.register( this );
        }
    }

    /**
     * Accept PLC event, serialize to XML and hand over to the listeners.
     */
    @Override
    public void accept( PlcSubscriptionEvent event )
    {
        XmlSerializerResult serializedContent;
        try
        {
            serializedContent= XmlSerializer.xmlSerialize( event );
        }
        catch ( ParserConfigurationException e )
        {
            logger.error( "EventHandler { " + handlerName + " } cannot process event.", e );
            return;
        }
        //hand over to listeners
        listeners.stream().forEach( listener -> listener.accept( serializedContent ) );
    }
}
