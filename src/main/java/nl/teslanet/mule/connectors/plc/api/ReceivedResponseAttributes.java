/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 (teslanet.nl) Rogier Cobben
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


/**
 * The attributes of a response.
 */
public class ReceivedResponseAttributes
{
    /**
     * Indication of successfully completion of the request.
     * If the request concerns multiple fields, a True value indicates 
     * that the operation succeeded on all fields. 
     */
    private boolean success;
    
    /**
     * Constructor.
     * @param success The success indicator.
     */
    public ReceivedResponseAttributes( boolean success)
    {
        this.success= success;
    }

    /**
     * @return the success
     */
    public boolean isSuccess()
    {
        return success;
    }
}
