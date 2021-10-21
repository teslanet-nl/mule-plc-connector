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
package nl.teslanet.mule.connectors.plc.internal.error;


import java.util.HashSet;
import java.util.Set;

import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;


/**
 * Provider of errors that can be thrown by operations.
 */
public class OperationErrorProvider implements ErrorTypeProvider
{
    @SuppressWarnings( "rawtypes" )
    @Override
    public Set< ErrorTypeDefinition > getErrorTypes()
    {
        Set< ErrorTypeDefinition > errors= new HashSet<>();
        errors.add( Errors.UNSUPPORTED_OPERATION );
        errors.add( Errors.IO_ERROR );
        errors.add( Errors.INTERRUPTED );
        errors.add( Errors.EXECUTION_ERROR );
        return errors;
    }
}
