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
package nl.teslanet.mule.connectors.plc.internal.exception;


/**
 * Internal Exception thrown when an unsupported operation was called.
 */
public class InternalUnsupportedException extends Exception
{
    /**
     * Serial version
     */
    private static final long serialVersionUID= 1L;

    /**
     * Default Constructor.
     */
    public InternalUnsupportedException()
    {
        super();
    }

    /**
     * Constructor with message.
     * @param message The error message.
     */
    public InternalUnsupportedException( String message )
    {
        super( message );
    }

    /**
     * Constructor with throwable.
     * @param cause The cause of the exception.
     */
    public InternalUnsupportedException( Throwable cause )
    {
        super( cause );
    }

    /**
     * Constructor with message and throwable.
     * @param message The error message.
     * @param cause The cause of the exception.
     */
    public InternalUnsupportedException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
