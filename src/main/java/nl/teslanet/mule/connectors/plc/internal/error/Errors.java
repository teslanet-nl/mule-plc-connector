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
package nl.teslanet.mule.connectors.plc.internal.error;


import org.mule.runtime.extension.api.error.ErrorTypeDefinition;


/**
 * PLC Connector Errors.
 *
 */
public enum Errors implements ErrorTypeDefinition< Errors >
{
    /**
     * The operation called is not supported by the protocol used.
     */
    UNSUPPORTED,

    /**
    * One or more fields could not be read or written successfully.
    */
    IO_ERROR,

    /**
    * An internal error occurred during execution of an operation.
    */
    EXECUTION_ERROR, 
    
    /**
     * A handlername is used that is invalid.
     */
    INVALID_HANDLER_NAME, 
    
    /**
     *  One or more Subscription parameters are invalid.
     */
    INVALID_SUBSCRIPTION
}
