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
package nl.teslanet.mule.connectors.plc.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.apache.plc4x.java.api.exceptions.PlcUnsupportedOperationException;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.api.ReadField;
import nl.teslanet.mule.connectors.plc.api.SubscribeField;
import nl.teslanet.mule.connectors.plc.api.UnsubscribeField;
import nl.teslanet.mule.connectors.plc.api.WriteField;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalConnectionException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalUnsupportedException;


/**
 * The plcConnection instance
 *
 */
public class DefaultMulePlcConnection implements MulePlcConnection
{
    /**
     * The logger of the class.
     */
    private static final Logger logger= LoggerFactory.getLogger( DefaultMulePlcConnection.class );

    /**
     * Handles of subscribed fields.
     */
    private ConcurrentHashMap< String, PlcSubscriptionHandle > handles= new ConcurrentHashMap<>();

    /**
     * The connection string of the plc.
     */
    private String connectionString;

    /**
     * The underlying PLC plcConnection.
     */
    protected final PlcConnection plcConnection;

    /**
     * Constructor.
     * @param connectionString The connection string of the PLC.
     * @param plcConnection The PLC Connection. 
     * @throws ConnectionException When the conncetion couls not 
     */
    public DefaultMulePlcConnection( String connectionString, PlcConnection plcConnection ) throws ConnectionException
    {
        this.connectionString= connectionString;
        this.plcConnection= plcConnection;
        logger.info( "Connection created { " + this + " }" );
    }

    /**
     * @return the connection string
     */
    @Override
    public String getConnectionString()
    {
        return connectionString;
    }

    /**
     * Close the plc connection.
     */
    @Override
    public synchronized void close()
    {
        logger.info( "Closing connection { " + this + " }" );
        if ( plcConnection.isConnected() )
        {
            try
            {
                plcConnection.close();
                logger.info( "Closed connection { " + this + " }" );
            }
            catch ( Exception e )
            {
                logger.error( "Exception while closing connection { " + this + " }", e );
            }
        }
    }

    @Override
    public synchronized void connect() throws InternalConnectionException
    {
        if ( !plcConnection.isConnected() )
        {
            try
            {
                plcConnection.connect();
                logger.info( "(re)Connected connection { " + this + " }" );
            }
            catch ( PlcConnectionException e )
            {
                logger.error( "Failed reconnecting { " + this + " }" );
            }
            if ( !plcConnection.isConnected() )
            {
                throw new InternalConnectionException( "Error on connection { " + this + " }" );
            }
        }
    }

    @Override
    public boolean isConnected()
    {
        //in case connection lost, try to reconnect first
        //TODO remove connect attempt
        try
        {
            connect();
        }
        catch ( InternalConnectionException e1 )
        {
            //Ignore
        }
        return plcConnection.isConnected();
    }

    @Override
    public synchronized Boolean ping() throws InterruptedException, InternalUnsupportedException
    {
        try
        {
            plcConnection.ping().get();
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof PlcUnsupportedOperationException )
            {
                throw new InternalUnsupportedException();
            }
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    public boolean canRead()
    {
        return plcConnection.getMetadata().canRead();
    }

    @Override
    public synchronized PlcReadResponse read( List< ReadField > fields, long timeout, TimeUnit timeOutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException
    {
        connect();
        PlcReadRequest.Builder builder= plcConnection.readRequestBuilder();
        for ( ReadField field : fields )
        {
            builder.addItem( field.getAlias(), field.getAddress() );
        }
        return builder.build().execute().get( timeout, timeOutUnit );
    }

    @Override
    public boolean canWrite()
    {
        return plcConnection.getMetadata().canWrite();
    }

    @Override
    public synchronized PlcWriteResponse write( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException
    {
        connect();
        PlcWriteRequest.Builder builder= plcConnection.writeRequestBuilder();
        for ( WriteField field : fields )
        {
            builder.addItem( field.getAlias(), field.getAddress(), field.getValues().toArray() );
        }
        return builder.build().execute().get( timeout, timeoutUnit );
    }

    @Override
    public boolean canSubscribe()
    {
        return plcConnection.getMetadata().canSubscribe();
    }

    @Override
    public synchronized PlcSubscriptionResponse subscribe( List< SubscribeField > fields, long timeout, TimeUnit timeOutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException
    {
        connect();
        PlcSubscriptionRequest.Builder builder= plcConnection.subscriptionRequestBuilder();
        for ( SubscribeField field : fields )
        {
            //TODO make configurable what type of subscription is wanted
            builder.addChangeOfStateField( field.getAlias(), field.getAddress() );
        }
        PlcSubscriptionResponse subscribeResponse= builder.build().execute().get( timeout, timeOutUnit );
        for ( String fieldName : subscribeResponse.getFieldNames() )
        {
            handles.put( fieldName, subscribeResponse.getSubscriptionHandle( fieldName ) );
        }
        return subscribeResponse;
    }

    @Override
    public PlcUnsubscriptionResponse unSubscribe( List< UnsubscribeField > fields, long timeout, TimeUnit timeOutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException
    {
        connect();
        List< PlcSubscriptionHandle > toUnsubscribe= new ArrayList<>();
        for ( UnsubscribeField field : fields )
        {
            PlcSubscriptionHandle handle= handles.get( field.getAlias() );
            if ( handle != null )
            {
                toUnsubscribe.add( handle );
            }
        }
        PlcUnsubscriptionRequest.Builder builder= plcConnection.unsubscriptionRequestBuilder();
        builder.addHandles( toUnsubscribe );
        return builder.build().execute().get( timeout, timeOutUnit );
    }
}
