/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2025 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.internal.utils;


import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.mule.sdk.api.values.Value;
import org.mule.sdk.api.values.ValueBuilder;
import org.mule.sdk.api.values.ValueProvider;
import org.mule.sdk.api.values.ValueResolvingException;


public class ResponseCodeValueProvider implements ValueProvider
{
    private static final ConcurrentHashMap< String, PlcResponseCode > values= new ConcurrentHashMap<>();

    private static final ConcurrentHashMap< PlcResponseCode, String > seulav= new ConcurrentHashMap<>();

    private static void assureInitialized()
    {
        if ( values.isEmpty() )
        {
            for ( PlcResponseCode code : PlcResponseCode.values() )
            {
                values.put( code.name(), code );
                seulav.put( code, code.name() );
            }
        }
    }
    
    private String id= UUID.randomUUID().toString();

    public static Enumeration< String > getKeys()
    {
        assureInitialized();
        return values.keys();
    }

    public static String getKey( PlcResponseCode value )
    {
        assureInitialized();
        return seulav.get( value );
    }

    public static PlcResponseCode getValue( String key )
    {
        assureInitialized();
        return values.get( key );
    }

    /* (non-Javadoc)
    * @see org.mule.sdk.api.values.ValueProvider#resolve()
    */
    @Override
    public Set< Value > resolve() throws ValueResolvingException
    {
        return ValueBuilder.getValuesFor( Stream.of( getKeys() ).map( Enumeration< String >::nextElement ) );
    }

    @Override
    public String getId()
    {
        return id;
    }
}
