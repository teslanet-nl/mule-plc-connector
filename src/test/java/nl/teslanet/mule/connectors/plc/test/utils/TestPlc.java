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


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.plc4x.java.mock.connection.MockDevice;
import org.apache.plc4x.java.mock.field.MockField;
import org.apache.plc4x.java.mock.field.MockPlcValue;
import org.apache.plc4x.java.mock.field.MockType;
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
    private ConcurrentHashMap< String, TestPlcField > fields= new ConcurrentHashMap<>();

    public TestPlc()
    {
    }

    @Override
    public ResponseItem< PlcValue > read( String fieldQuery )
    {
        TestPlcField field= fields.get( fieldQuery );
        if ( field == null )
        {
            field= new TestPlcField(fieldQuery);
            field.setMember( "address", new PlcSTRING( fieldQuery ) );
            field.setMember( "value", new PlcSTRING( "empty" ) );
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
        TestPlcField field= fields.get( fieldQuery );
        //TestPlcValue field= null;
        if ( field == null )
        {
            field= new TestPlcField(fieldQuery);
            field.setMember( "address", new PlcSTRING( fieldQuery ) );
            fields.put( fieldQuery, field );
        }
        field.setMember( "value", new PlcSTRING( ( (MockPlcValue) value ).getObject(0).toString()) );
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
        for ( Consumer< PlcSubscriptionEvent > client : field.consumers )
        {
            client.accept( new TestPlcSubscriptionEvent( field ) );
        }
        return PlcResponseCode.OK;
    }

    @Override
    public ResponseItem< PlcSubscriptionHandle > subscribe( String fieldQuery )
    {
        TestPlcField field= fields.get( fieldQuery );
        if ( field == null )
        {
            field= new TestPlcField(fieldQuery);
            field.setMember( "address", new PlcSTRING( fieldQuery ) );
            field.setMember( "value", new PlcSTRING( "empty" ) );
            fields.put( fieldQuery, field );
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
     *
     */
    class TestPlcField extends MockField implements PlcSubscriptionHandle
    {
        private final ConcurrentHashMap< String, PlcValue > members= new ConcurrentHashMap<>();

        private final CopyOnWriteArraySet< Consumer< PlcSubscriptionEvent > > consumers= new CopyOnWriteArraySet<>();

        public TestPlcField( String address )
        {
            super(address, MockType.BOOL);
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

        public void notifyConsumers()
        {
            for ( Consumer< PlcSubscriptionEvent > consumer : consumers )
            {
                consumer.accept( new TestPlcSubscriptionEvent( this ) );
            }
        }
    }
   
    class TestPlcSubscriptionEvent implements PlcSubscriptionEvent
    {
        private final TestPlcField field;

        private Instant ts;

        public TestPlcSubscriptionEvent( TestPlcField testPlcField )
        {
            this.field= testPlcField;
            this.ts= Instant.now();
        }

        @Override
        public PlcReadRequest getRequest()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlcValue getAsPlcValue()
        {
            return new PlcStruct( field.getMembers() );
        }

        @Override
        public PlcValue getPlcValue( String name )
        {
            return new PlcStruct( field.getMembers() );
        }

        @Override
        public int getNumberOfValues( String name )
        {
            return( field.getMember( name ) == null ? 0 : 1 );
        }

        @Override
        public Object getObject( String name )
        {
            return field.getMember( name );
        }

        @Override
        public Object getObject( String name, int index )
        {
            return( index == 0 ? field.getMember( name ) : null );
        }

        @Override
        public Collection< Object > getAllObjects( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidBoolean( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidBoolean( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Boolean getBoolean( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Boolean getBoolean( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Boolean > getAllBooleans( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidByte( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidByte( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Byte getByte( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Byte getByte( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Byte > getAllBytes( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidShort( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidShort( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Short getShort( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Short getShort( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Short > getAllShorts( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidInteger( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidInteger( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Integer getInteger( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Integer getInteger( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Integer > getAllIntegers( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidBigInteger( String name )
        {
            // TODO Auto-generated method stubnull
            return false;
        }

        @Override
        public boolean isValidBigInteger( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public BigInteger getBigInteger( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BigInteger getBigInteger( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< BigInteger > getAllBigIntegers( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidLong( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidLong( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Long getLong( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Long getLong( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Long > getAllLongs( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidFloat( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidFloat( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Float getFloat( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Float getFloat( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Float > getAllFloats( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidDouble( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidDouble( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public Double getDouble( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Double getDouble( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< Double > getAllDoubles( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidBigDecimal( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidBigDecimal( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public BigDecimal getBigDecimal( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public BigDecimal getBigDecimal( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< BigDecimal > getAllBigDecimals( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidString( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidString( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public String getString( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getString( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< String > getAllStrings( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidTime( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidTime( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public LocalTime getTime( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LocalTime getTime( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< LocalTime > getAllTimes( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidDate( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidDate( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public LocalDate getDate( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LocalDate getDate( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< LocalDate > getAllDates( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isValidDateTime( String name )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isValidDateTime( String name, int index )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public LocalDateTime getDateTime( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public LocalDateTime getDateTime( String name, int index )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< LocalDateTime > getAllDateTimes( String name )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection< String > getFieldNames()
        {
            ArrayList< String > list= new ArrayList<>();
            list.add( field.getMember( "address" ).getString() );
            return Collections.unmodifiableList( list );
        }

        @Override
        public PlcField getField( String name )
        {
            return field;
        }

        @Override
        public PlcResponseCode getResponseCode( String name )
        {
            return( !name.equals( field.getMember( "address" ).getString() ) ? PlcResponseCode.INVALID_ADDRESS : PlcResponseCode.OK );
        }

        @Override
        public Instant getTimestamp()
        {
            return ts;
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
                TestPlcField field= (TestPlcField) handle;
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
                TestPlcField field= (TestPlcField) handle;
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
