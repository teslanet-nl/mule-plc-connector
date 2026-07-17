/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
 * 
 * Contributors:
 *     (teslanet.nl) Rogier Cobben - initial creation
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */
package nl.teslanet.mule.connectors.plc.internal.error;


import java.util.HashSet;
import java.util.Set;

import org.mule.sdk.api.annotation.error.ErrorTypeProvider;
import org.mule.sdk.api.error.ErrorTypeDefinition;


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
        errors.add( Errors.UNSUPPORTED );
        errors.add( Errors.IO_ERROR );
        errors.add( Errors.CONCURRENCY_ERROR );
        errors.add( Errors.EXECUTION_ERROR );
        return errors;
    }
}
