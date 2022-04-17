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
package nl.teslanet.mule.connectors.plc.api;


import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.mule.runtime.api.value.Value;
import org.mule.runtime.extension.api.values.ValueBuilder;
import org.mule.runtime.extension.api.values.ValueProvider;
import org.mule.runtime.extension.api.values.ValueResolvingException;


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
    * @see org.mule.runtime.extension.api.values.ValueProvider#resolve()
    */
    @Override
    public Set< Value > resolve() throws ValueResolvingException
    {
        return ValueBuilder.getValuesFor( Stream.of( getKeys() ).map( e -> e.nextElement() ) );
    }
}
