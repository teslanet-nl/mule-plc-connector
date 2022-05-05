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
import java.util.concurrent.TimeUnit;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidHandlerNameException;


/**
 * Configuration of a PLC connection.
 */
@Configuration( name= "config" )
@Operations( MulePlcOperations.class )
@Sources( EventListener.class )
@ConnectionProviders( MulePlcConnectionProvider.class )
public class MulePlcConfig
{
    @RefName
    private String configName= null;

    /**
     * The timeout units used for plc communcation.
     */
    @Parameter
    @Optional( defaultValue= "1000" )
    @Summary( "The timeout used for plc communcation." )
    private long timeout= 1000L;

    /**
     * The timeout units user for plc communcation.
     */
    @Parameter
    @Optional( defaultValue= "MILLISECONDS" )
    @Summary( "The timeout units of the timeout value." )
    private TimeUnit timeoutUnits= TimeUnit.MILLISECONDS;

    /**
     * The list of response handlers
     */
    private ConcurrentHashMap< String, EventListener > handlers= new ConcurrentHashMap<>();

    /**
     * @return The configuration name.
     */
    public String getConfigName()
    {
        return configName;
    }

    /**
     * @return The timeout used for plc communcation.
     */
    public long getTimeout()
    {
        return timeout;
    }

    /**
     * @return The timeout units user for plc communcation.
     */
    public TimeUnit getTimeoutUnits()
    {
        return timeoutUnits;
    }

    /**
     * Add handler to process responses.
     * @param handlerName the name of the handler
     * @param callback the source callback that will process the responses
     * @throws InternalInvalidHandlerNameException 
     */
    synchronized void addHandler( String handlerName, EventListener handler ) throws InternalInvalidHandlerNameException
    {
        if ( handlerName == null || handlerName.isEmpty() ) throw new InternalInvalidHandlerNameException( "empty response handler name not allowed" );
        if ( handlers.get( handlerName ) != null ) throw new InternalInvalidHandlerNameException( "responsehandler name { " + handlerName + " } not unique" );
        handlers.put( handlerName, handler );
    }

    /**
     * Get handler by name.
     * @param handlerName The name of the handler.
     * @return The handler or null if no handler with given name exists.
     */
    public EventListener getHandler( String handlerName )
    {
        return handlers.get( handlerName );
    }

    /**
     * Remove a handler
     * @param handlerName the name of the handler to remove
     */
    void removeHandler( String handlerName )
    {
        handlers.remove( handlerName );
    }
}
