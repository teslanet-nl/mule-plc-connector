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


import org.mule.runtime.extension.api.exception.ModuleException;


/**
 * InvalidHandlerNameException occurs when a given handler name is invalid.
 *
 */
public class InvalidHandlerNameException extends ModuleException
{
    /**
     * 
     */
    private static final long serialVersionUID= 1L;

    public InvalidHandlerNameException( String message )
    {
        super( message, Errors.INVALID_HANDLER_NAME );
    }

    public InvalidHandlerNameException( Throwable cause )
    {
        super( Errors.INVALID_HANDLER_NAME, cause );
    }

    public InvalidHandlerNameException( String message, Throwable cause )
    {
        super( message, Errors.INVALID_HANDLER_NAME, cause );
    }
}
