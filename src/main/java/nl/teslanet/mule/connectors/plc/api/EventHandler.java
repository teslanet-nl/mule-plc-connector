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
import org.mule.sdk.api.annotation.dsl.xml.TypeDsl;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.RefName;
import org.mule.sdk.api.annotation.param.display.Example;
import org.mule.sdk.api.annotation.param.display.Text;
import org.mule.sdk.api.meta.ExpressionSupport;


/**
 * Configuration of a PLC eventHandler.
 */
@TypeDsl( allowInlineDefinition= false, allowTopLevelDefinition= true )
public class EventHandler
{
    /**
     * Name of the handler.
     */
    @RefName
    private String handlerName= null;

    /**
     * Description of this handler.
     */
    @Parameter
    @Optional
    @Text
    @Expression( ExpressionSupport.NOT_SUPPORTED )
    @Example( "Some usage explanation of this handler." )
    private String description;

    /**
     * @return The handler name.
     */
    public String getHandlerName()
    {
        return handlerName;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription( String description )
    {
        this.description= description;
    }
}
