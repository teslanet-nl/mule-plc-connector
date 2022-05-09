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


import java.util.concurrent.ConcurrentHashMap;

import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidHandlerNameException;


/**
 * Event handler registry.
 */
public class EventHandlerRegistry
{
    /**
     * The list of response handlers
     */
    private ConcurrentHashMap< String, EventProcessor > eventHandlers= new ConcurrentHashMap<>();

    /**
     * Get handler to process events.
     * @param handlerName the name of the handler
     * @throws InternalInvalidHandlerNameException When handler name is invalid
     */
    public synchronized EventProcessor getEventProcessor( String handlerName ) throws InternalInvalidHandlerNameException
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new InternalInvalidHandlerNameException( "empty response handler name not allowed" );
        EventProcessor processor= eventHandlers.get( handlerName );
        if ( processor == null )
        {
            processor= new EventProcessor( handlerName );
            eventHandlers.put( handlerName, processor );
        }
        return processor;
    }

    /**
     * Remove a handler
     * @param handlerName the name of the handler to remove
     * @throws InternalInvalidHandlerNameException When handler name is invalid
     */
    public synchronized void removeHandler( String handlerName ) throws InternalInvalidHandlerNameException
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new InternalInvalidHandlerNameException( "empty response handler name not allowed" );
        EventProcessor processor= eventHandlers.get( handlerName );
        if ( processor != null )
        {
            eventHandlers.remove( handlerName );
        }
    }

    /**
     * Clear all handlers.
     */
    public synchronized void clear()
    {
        eventHandlers.clear();
    }
}
