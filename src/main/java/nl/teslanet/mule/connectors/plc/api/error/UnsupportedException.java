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
package nl.teslanet.mule.connectors.plc.api.error;


import org.mule.runtime.extension.api.exception.ModuleException;


/**
 * InvalidHandlerNameException occurs when a given handler name is invalid.
 *
 */
public class UnsupportedException extends ModuleException
{
    /**
     * 
     */
    private static final long serialVersionUID= 1L;

    public UnsupportedException( String message )
    {
        super( message, Errors.UNSUPPORTED_OPERATION );
    }

    public UnsupportedException( Throwable cause )
    {
        super( Errors.UNSUPPORTED_OPERATION, cause );
    }

    public UnsupportedException( String message, Throwable cause )
    {
        super( message, Errors.UNSUPPORTED_OPERATION, cause );
    }
}
