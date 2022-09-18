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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.mock.connection.MockDevice;
import org.apache.plc4x.java.mock.field.MockPlcValue;
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
    /**
     * The fields of the PLC.
     */
    private ConcurrentHashMap< String, TestPlcValue > fields= new ConcurrentHashMap<>();

    public TestPlc()
    {
    }

    @Override
    public ResponseItem< PlcValue > read( String fieldQuery )
    {
        TestPlcValue field= fields.get( fieldQuery );
        if ( field == null )
        {
            field= new TestPlcValue();
            field.setMember( "field", new PlcSTRING( fieldQuery ) );
            field.setMember( "value", new PlcSTRING( "empty") );
            fields.put( fieldQuery, field );
        }
        field.setMember( "read_begin_dt", plcNow() );
        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            return new ResponseItem< PlcValue >( PlcResponseCode.INTERNAL_ERROR, new PlcStruct( field.getMembers() ) );
        }
        field.setMember( "read_end_dt", plcNow() );
        return new ResponseItem< PlcValue >( PlcResponseCode.OK, new PlcStruct( field.getMembers() ) );
    }

    @Override
    public PlcResponseCode write( String fieldQuery, Object value )
    {
        TestPlcValue field= fields.get( fieldQuery );
        //TestPlcValue field= null;
        if ( field == null )
        {
            field= new TestPlcValue();
            field.setMember( "field", new PlcSTRING( fieldQuery ) );
            fields.put( fieldQuery, field );
        }
        field.setMember( "value", ((MockPlcValue) value) );
        field.setMember( "write_begin_dt", plcNow() );
        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            return PlcResponseCode.REMOTE_BUSY;
        }
        field.setMember( "write_end_dt", plcNow() );
        return PlcResponseCode.OK;
    }

    @Override
    public ResponseItem< PlcSubscriptionHandle > subscribe( String fieldQuery )
    {
        //TestPlcValue field= fields.get( fieldQuery );
        TestPlcValue field= null;
        if ( field == null )
        {
            field= new TestPlcValue();
            field.setMember( "field", new PlcSTRING( fieldQuery ) );
            field.setMember( "value", new PlcSTRING( "empty") );
            //fields.put( fieldQuery, field );
        }
        field.setMember( "subscribe_begin_dt", plcNow() );
        try
        {
            Thread.sleep( 2000 );
        }
        catch ( InterruptedException e )
        {
            return new ResponseItem<>( PlcResponseCode.REMOTE_BUSY, field );
        }
        field.setMember( "subscribe_end_dt", plcNow() );
        return new ResponseItem<>( PlcResponseCode.OK, field );
    }

    @Override
    public void unsubscribe()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public PlcConsumerRegistration register( Consumer< PlcSubscriptionEvent > consumer, Collection< PlcSubscriptionHandle > handles )
    {
        return new TestPlcConsumerRegistration( consumer, handles );
    }

    @Override
    public void unregister( PlcConsumerRegistration registration )
    {
        registration.unregister();
    }

    /**
     * Handle storing subscriptions
     *
     */
    class TestPlcValue implements PlcSubscriptionHandle
    {
        private final ConcurrentHashMap< String, PlcValue > members= new ConcurrentHashMap<>();

        private final CopyOnWriteArraySet< Consumer< PlcSubscriptionEvent > > consumers= new CopyOnWriteArraySet<>();

        public TestPlcValue()
        {
        }

        public PlcValue getMember( String key )
        {
            return members.get( key );
        }

        public Map< String, PlcValue > getMembers()
        {
            return members;
        }

        public PlcValue setMember( String key, PlcValue value )
        {
            return members.put( key, value );
        }

        @Override
        public PlcConsumerRegistration register( Consumer< PlcSubscriptionEvent > consumer )
        {
            //TODO return? 
            consumers.add( consumer );
            return null;
        }

        public void unregister( Consumer< PlcSubscriptionEvent > consumer )
        {
            consumers.remove( consumer );
        }
    }

    class TestPlcConsumerRegistration implements PlcConsumerRegistration
    {
        private final Consumer< PlcSubscriptionEvent > consumer;

        private final CopyOnWriteArrayList< PlcSubscriptionHandle > handles= new CopyOnWriteArrayList<>();

        public TestPlcConsumerRegistration( Consumer< PlcSubscriptionEvent > consumer, Collection< PlcSubscriptionHandle > handles )
        {
            this.consumer= consumer;
            this.handles.addAll( handles );
            for ( PlcSubscriptionHandle handle : this.handles )
            {
                TestPlcValue field= (TestPlcValue) handle;
                field.register( consumer );
            }
        }

        @Override
        public Integer getConsumerId()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List< PlcSubscriptionHandle > getSubscriptionHandles()
        {
            return handles;
        }

        @Override
        public void unregister()
        {
            for ( PlcSubscriptionHandle handle : this.handles )
            {
                TestPlcValue field= (TestPlcValue) handle;
                field.unregister( consumer );
            }
        }
    }

    /**
     * @return Now timestamp as plc value.
     */
    private PlcDATE_AND_TIME plcNow()
    {
        return new PlcDATE_AND_TIME( Instant.now().toEpochMilli() / 1000 );
    }
}
