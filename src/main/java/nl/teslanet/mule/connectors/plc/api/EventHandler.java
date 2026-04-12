/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
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
