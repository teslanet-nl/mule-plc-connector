/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
 * 
 * Contributors:
 *     (teslanet.nl) Rogier Cobben - initial creation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */
package nl.teslanet.mule.connectors.plc.internal;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.sdk.api.runtime.operation.Result;

import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.response.FieldValueResponse;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidHandlerNameException;
import nl.teslanet.mule.connectors.plc.internal.utils.MessageUtils;


/**
 * Event processor.
 */
public class EventProcessor implements Consumer< PlcSubscriptionEvent >
{
    /**
     * The registry of response processors
     */
    private static ConcurrentHashMap< String, EventProcessor > registry= new ConcurrentHashMap<>();

    /**
     * Get reponse processor of the handler..
     * @param handlerName the name of the handler
     * @throws InternalInvalidHandlerNameException When handler name is invalid
     */
    public static EventProcessor getEventProcessor( String handlerName ) throws InternalInvalidHandlerNameException
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new InternalInvalidHandlerNameException( "empty response handler name not allowed" );
        registry.putIfAbsent( handlerName, new EventProcessor( handlerName ) );
        return registry.get( handlerName );
    }

    /**
     * Remove an event processor.
     * @param handlerName the name of the handler to remove
     * @throws InternalInvalidHandlerNameException When handler name is invalid
     */
    public static void removeEventProcessor( String handlerName ) throws InternalInvalidHandlerNameException
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new InternalInvalidHandlerNameException( "empty response handler name not allowed" );
        registry.remove( handlerName );
    }

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
     * @param subscriptionResponse The subscription response containing the handles.
     */
    public synchronized void register( PlcSubscriptionResponse subscriptionResponse )
    {
        for ( PlcSubscriptionHandle handle : subscriptionResponse.getSubscriptionHandles() )
        {
            handle.register( this );
        }
    }

    /**
     * Accept PLC event and hand over to the listeners.
     */
    @Override
    public void accept( PlcSubscriptionEvent event )
    {
        boolean success= true;
        for ( String tagName : event.getTagNames() )
        {
            if ( event.getResponseCode( tagName ) != PlcResponseCode.OK )
            {
                success= false;
            }
        }
        Result< List< FieldValueResponse >, ReceivedResponseAttributes > result= Result
            .< List< FieldValueResponse >, ReceivedResponseAttributes > builder()
            .output( MessageUtils.createFieldResponses( event ) )
            .attributes( new ReceivedResponseAttributes( success ) )
            .mediaType( MediaType.APPLICATION_JAVA )
            .build();
        //hand over to listeners
        listeners.stream().forEach( listener -> listener.accept( result ) );
    }
}
