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
package nl.teslanet.mule.connectors.plc.test.utils;


import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.mock.connection.MockDevice;
import org.apache.plc4x.java.spi.messages.utils.ResponseItem;
import org.apache.plc4x.java.spi.values.PlcDATE_AND_TIME;
import org.apache.plc4x.java.spi.values.PlcSTRING;
import org.apache.plc4x.java.spi.values.PlcStruct;


/**
 * Plc device for testing. T
 * he response delays make the device suitable for concurency tests.
 */
public class TestPlc implements MockDevice
{
    @Override
    public ResponseItem< PlcValue > read( String fieldQuery )
    {
        HashMap< String, PlcValue > values= new HashMap<>();
        values.put( "field", new PlcSTRING( fieldQuery ) );
        values.put( "begin", new PlcDATE_AND_TIME( Instant.now().toEpochMilli() / 1000 ) );
        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            return new ResponseItem< PlcValue >( PlcResponseCode.INTERNAL_ERROR, new PlcStruct( values ) );
        }
        values.put( "end", new PlcDATE_AND_TIME( Instant.now().toEpochMilli() / 1000 ) );
        return new ResponseItem< PlcValue >( PlcResponseCode.OK, new PlcStruct( values ) );
    }

    @Override
    public PlcResponseCode write( String fieldQuery, Object value )
    {
        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            return PlcResponseCode.REMOTE_BUSY;
        }
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
