/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
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
 * The response codes. The codes mirror PlcResponseCode
 */
public enum ResponseCode
{
    /**
     * Everything went ok.
     */
    OK,
    /**
     * The requested ressource could not be found on the target device.
     * (The address was syntactically correct, but the item it addressed simply didn't exist)
     */
    NOT_FOUND,
    /**
     * The remote device denied access to the requested resource.
     * (Possibly remote access is disabled, or an authentication is required)
     */
    ACCESS_DENIED,
    /**
     * The resource address was syntactically invalid.
     */
    INVALID_ADDRESS,
    /**
     * The requested datatype does not exist or was not compatible with the requested ressource.
     */
    INVALID_DATATYPE,
    INVALID_DATA,
    /**
     * Something went wrong internally in the driver logic.
     * (This is most probably a PLC4X bug)
     */
    INTERNAL_ERROR,
    /**
     * The remote device is currently unable to process the request due to overload.
     */
    REMOTE_BUSY,
    /**
     * Something went wrong on the remote side.
     */
    REMOTE_ERROR,
    /**
     * The requested resource uses a feature of the driver which has not been implemented.
     */
    UNSUPPORTED,
    /**
     * Indicates a response is pending.
     */
    RESPONSE_PENDING;
}
