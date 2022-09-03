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
import java.util.concurrent.locks.Lock;

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
import org.mule.runtime.api.lock.LockFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.api.ReadField;
import nl.teslanet.mule.connectors.plc.api.SubscribeField;
import nl.teslanet.mule.connectors.plc.api.UnsubscribeField;
import nl.teslanet.mule.connectors.plc.api.WriteField;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalConcurrencyException;
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
     * The read lock pool to control concurrent reads.
     */
    protected final boolean readAllowed;

    /**
     * The read lock pool to control concurrent writes.
     */
    protected final boolean writeAllowed;

    /**
     * The subscribe lock pool to control concurrent subscribe and unsubscribe operations.
     */
    protected final boolean subscribeAllowed;

    /**
     * The IO lock pool to control overall concurrent operations.
     */
    protected final LockPool ioLocks;

    /**
     * The read lock pool to control concurrent reads.
     */
    protected final LockPool readLocks;

    /**
     * The read lock pool to control concurrent writes.
     */
    protected final LockPool writeLocks;

    /**
     * The subscribe lock pool to control concurrent subscribe and unsubscribe operations.
     */
    protected final LockPool subscribeLocks;

    /**
     * Constructor.
     * @param connectionString The connection string of the PLC.
     * @param plcConnection The PLC Connection. 
     * @throws ConnectionException When the conncetion failed.
     */
    public DefaultMulePlcConnection( String connectionString, PlcConnection plcConnection, LockFactory lockFactory, ConcurrencyParams concurrencyParams ) throws ConnectionException
    {
        this.connectionString= connectionString;
        this.plcConnection= plcConnection;

        ioLocks= ( concurrencyParams.getConcurrentIo() >= 0 ? new LockPool( lockFactory, this.toString() + "-subscribe-", concurrencyParams.getConcurrentIo() ) : null );
        readAllowed= ( concurrencyParams.getConcurrentReads() != 0 );
        readLocks= ( concurrencyParams.getConcurrentReads() > 0 ? new LockPool( lockFactory, this.toString() + "-read-", concurrencyParams.getConcurrentReads() ) : null );
        writeAllowed= ( concurrencyParams.getConcurrentWrites() != 0 );
        writeLocks= ( concurrencyParams.getConcurrentWrites() > 0 ? new LockPool( lockFactory, this.toString() + "-write-", concurrencyParams.getConcurrentWrites() ) : null );
        subscribeAllowed= ( concurrencyParams.getConcurrentWrites() != 0 );
        subscribeLocks= ( concurrencyParams.getConcurrentWrites() > 0
            ? new LockPool( lockFactory, this.toString() + "-subscribe-", concurrencyParams.getConcurrentWrites() ) : null );
        logger.info( "connection created { " + this + " }" );
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
        ClassLoader actualClassLoader= plcConnection.getClass().getClassLoader();
        if ( !plcConnection.isConnected() )
        {
            try
            {
                plcConnection.connect();
                logger.info( "(re)Connected connection { " + this + "::" + plcConnection + " }" );
            }
            catch ( PlcConnectionException e )
            {
                logger.error( "Failed reconnecting { " + this + "::" + plcConnection + " }" );
            }
            //TODO
//            if ( !plcConnection.isConnected() )
//            {
//                throw new InternalConnectionException( "Error on connection { " + this + " }" );
//            }
        }
    }

    @Override
    public boolean isConnected()
    {
        ClassLoader actualClassLoader= plcConnection.getClass().getClassLoader();
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

    //TODO concurrency
    /**
     * Ping operation
     */
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
        return( readAllowed && plcConnection.getMetadata().canRead() );
    }

    /**
     * Read PLC fields.
     */
    @Override
    public PlcReadResponse read( List< ReadField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( !readAllowed ) throw new InternalConcurrencyException( "No read allowed on this connection." );
        Lockable< PlcReadResponse > operation= () -> {
            return readIoLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * Read PLC fields.
     */
    private PlcReadResponse readIoLocked( List< ReadField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( readLocks == null )
        {
            return readLocked( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcReadResponse > operation= () -> {
                return readLocked( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( readLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     * @throws InternalConcurrencyException
     */
    private PlcReadResponse readLocked( List< ReadField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        connect();
        PlcReadRequest.Builder builder= plcConnection.readRequestBuilder();
        for ( ReadField field : fields )
        {
            builder.addItem( field.getAlias(), field.getAddress() );
        }
        return builder.build().execute().get( timeout, timeoutUnit );
    }

    /**
     * Estblish that the connection can write.
     */
    @Override
    public boolean canWrite()
    {
        return( writeAllowed && plcConnection.getMetadata().canWrite() );
    }

    /**
     * Read PLC fields.
     */
    @Override
    public PlcWriteResponse write( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( !writeAllowed ) throw new InternalConcurrencyException( "No write allowed on this connection." );
        Lockable< PlcWriteResponse > operation= () -> {
            return writeIoLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     * @throws InternalConcurrencyException
     */
    private PlcWriteResponse writeIoLocked( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( writeLocks == null )
        {
            return writeLocked( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcWriteResponse > operation= () -> {
                return writeLocked( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( writeLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     * @throws InternalConcurrencyException
     */
    private PlcWriteResponse writeLocked( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        connect();
        PlcWriteRequest.Builder builder= plcConnection.writeRequestBuilder();
        for ( WriteField field : fields )
        {
            builder.addItem( field.getAlias(), field.getAddress(), field.getValues().toArray() );
        }
        return builder.build().execute().get( timeout, timeoutUnit );
    }

    /**
     *
     */
    @Override
    public boolean canSubscribe()
    {
        return( subscribeAllowed && plcConnection.getMetadata().canSubscribe() );
    }

    @Override
    public synchronized PlcSubscriptionResponse subscribe( List< SubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( !subscribeAllowed ) throw new InternalConcurrencyException( "No subscribe allowed on this connection." );
        Lockable< PlcSubscriptionResponse > operation= () -> {
            return subscribeIoLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     * @throws InternalConcurrencyException 
     */
    public synchronized PlcSubscriptionResponse subscribeIoLocked( List< SubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( subscribeLocks == null )
        {
            return subscribeLocked( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcSubscriptionResponse > operation= () -> {
                return subscribeLocked( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( subscribeLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     */
    public synchronized PlcSubscriptionResponse subscribeLocked( List< SubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException
    {
        PlcSubscriptionResponse subscribeResponse;

        connect();
        PlcSubscriptionRequest.Builder builder= plcConnection.subscriptionRequestBuilder();
        for ( SubscribeField field : fields )
        {
            //TODO make configurable what type of subscription is wanted
            builder.addChangeOfStateField( field.getAlias(), field.getAddress() );
        }
        subscribeResponse= builder.build().execute().get( timeout, timeoutUnit );
        for ( String fieldName : subscribeResponse.getFieldNames() )
        {
            handles.put( fieldName, subscribeResponse.getSubscriptionHandle( fieldName ) );
        }
        return subscribeResponse;
    }

    /**
     *
     */
    @Override
    public PlcUnsubscriptionResponse unSubscribe( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( !subscribeAllowed ) throw new InternalConcurrencyException( "No subscribe allowed on this connection." );
        Lockable< PlcUnsubscriptionResponse > operation= () -> {
            return unSubscribeIoLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     * @throws InternalConcurrencyException
     */
    public PlcUnsubscriptionResponse unSubscribeIoLocked( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException
    {
        if ( subscribeLocks == null )
        {
            return unSubscribeLocked( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcUnsubscriptionResponse > operation= () -> {
                return unSubscribeLocked( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( subscribeLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * @param fields
     * @param timeout
     * @param timeoutUnit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InternalConnectionException
     */
    public PlcUnsubscriptionResponse unSubscribeLocked( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException
    {
        PlcUnsubscriptionResponse subscribeResponse;
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
        subscribeResponse= builder.build().execute().get( timeout, timeoutUnit );
        fields.forEach( (field) -> handles.remove( field.getAlias() ) );
        return subscribeResponse;
    }

    /**
     * Interface for IO lockable operations.
     *
     */
    @FunctionalInterface
    private interface Lockable< R >
    {
        R run() throws InterruptedException, ExecutionException, TimeoutException, InternalConnectionException, InternalConcurrencyException;

        /**
         * @param fields
         * @param timeout
         * @param timeoutUnit
         * @return
         * @throws InterruptedException
         * @throws ExecutionException
         * @throws TimeoutException
         * @throws InternalConnectionException
         * @throws InternalConcurrencyException
         */
        default R doLocked( LockPool pool, Lockable< R > operation, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
            ExecutionException,
            TimeoutException,
            InternalConnectionException,
            InternalConcurrencyException
        {
            if ( pool == null )
            {
                return operation.run();
            }
            else
            {
                Lock lock= pool.getLock();
                if ( lock.tryLock( timeout, timeoutUnit ) )
                {
                    try
                    {
                        return operation.run();
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
                else
                {
                    throw new TimeoutException( "Timeout on getting lock." );
                }
            }
        }
    }
}
