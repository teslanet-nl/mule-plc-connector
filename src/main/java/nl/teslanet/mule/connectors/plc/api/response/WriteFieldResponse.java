/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2025 - 2026 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.api.response;


/**
 * Canonical write field response.
 * In fact an interface but Mule extension API needs concrete classes. 
 */
public class WriteFieldResponse
{
    /**
     * Get the alias of the field.
     * @return The alias of the field.
     */
    public String getAlias()
    {
        return null;
    }

    /**
     * Get the result of the field write operation.
     * @return The result of the field write.
     */
    public String getResult()
    {
        return null;
    }

    /**
     * Get the type of the field.
     * @return The field type.
     */
    public String getFieldType()
    {
        return null;
    }

    /**
     * Get the value of the write field.
     * @return The value of the field.
     */
    public ResponseValue getValue()
    {
        return null;
    }
}
