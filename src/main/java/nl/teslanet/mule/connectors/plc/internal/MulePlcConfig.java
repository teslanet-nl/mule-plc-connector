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


import java.util.concurrent.TimeUnit;

import org.mule.sdk.api.annotation.Configuration;
import org.mule.sdk.api.annotation.Operations;
import org.mule.sdk.api.annotation.connectivity.ConnectionProviders;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.RefName;
import org.mule.sdk.api.annotation.param.display.Summary;


/**
 * Configuration of a PLC connection.
 */
@Configuration( name= "config" )
@Operations( MulePlcOperations.class )
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
