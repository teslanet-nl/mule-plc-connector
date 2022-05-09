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


import java.util.concurrent.TimeUnit;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;


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
}
