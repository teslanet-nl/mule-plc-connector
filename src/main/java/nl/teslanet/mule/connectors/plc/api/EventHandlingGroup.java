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
package nl.teslanet.mule.connectors.plc.api;


import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.meta.ExpressionSupport;


/**
 * The Configuration of event handling.
 */
public class EventHandlingGroup
{
    /**
     * The event handler that will collect the PLC events produced by the subscription.
     */
    @Parameter
    @Expression( ExpressionSupport.SUPPORTED )
    @Summary( "The event handler that will collect the PLC events produced by the subscription." )
    private EventHandler eventHandler;

    /**
     * @return the handlerName
     */
    public EventHandler getEventHandler()
    {
        return eventHandler;
    }
}
