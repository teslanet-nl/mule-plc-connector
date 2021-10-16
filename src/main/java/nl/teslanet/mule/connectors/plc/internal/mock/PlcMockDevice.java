/*-
 * #%L
 * Mule CoAP Connector
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
package nl.teslanet.mule.connectors.plc.internal.mock;


import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.mock.connection.MockDevice;
import org.apache.plc4x.java.spi.messages.utils.ResponseItem;
import org.apache.plc4x.java.spi.values.PlcBOOL;
import org.apache.plc4x.java.spi.values.PlcINT;
import org.apache.plc4x.java.spi.values.PlcNull;


public class PlcMockDevice implements MockDevice
{
    private final ConcurrentHashMap< String, Object > fields= new ConcurrentHashMap<>();

    /**
     * Default constructor;
     */
    public PlcMockDevice()
    {
        write( "coil1:BOOL", new PlcBOOL( "true" ) );
        write( "register1:INT", new PlcINT( "345" ) );
    }

    public void remove( String fieldQuery )
    {
       fields.remove( fieldQuery );
    }
    
    @Override
    public ResponseItem< PlcValue > read( String fieldQuery )
    {
        if ( fields.containsKey( fieldQuery ) )
        {
            return new ResponseItem< PlcValue >( PlcResponseCode.OK, (PlcValue) fields.get( fieldQuery ) );
        }
        else
        {
            return new ResponseItem< PlcValue >( PlcResponseCode.NOT_FOUND, new PlcNull() );
        }
    }

    @Override
    public PlcResponseCode write( String fieldQuery, Object value )
    {
        fields.put( fieldQuery, ( value == null ? new PlcNull() : value ));
        return PlcResponseCode.OK;
    }

    @Override
    public ResponseItem< PlcSubscriptionHandle > subscribe( String fieldQuery )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unsubscribe()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public PlcConsumerRegistration register( Consumer< PlcSubscriptionEvent > consumer, Collection< PlcSubscriptionHandle > handles )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unregister( PlcConsumerRegistration registration )
    {
        // TODO Auto-generated method stub

    }

}
