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

package nl.teslanet.mule.connectors.plc.api;


import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;


/**
 * Definition of an PLC field to read.
 *
 */
public class ReadField
{
    /**
     * The PLC address to be read.
     */
    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("The alias of the field to read for reference. ")
    private String alias;

    /**
     * The PLC address to be read.
     */
    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("The field address in protocol specific format.")
    private String address;

    /**
     * @return the alias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * @return the address
     */
    public String getAddress()
    {
        return address;
    }
}
