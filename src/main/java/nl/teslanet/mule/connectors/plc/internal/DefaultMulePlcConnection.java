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
     * The ping operations are allowed by user.
     */
    protected final boolean pingAllowed;

    /**
     * The read operations are allowed by user.
     */
    protected final boolean readAllowed;

    /**
     * The write operations are allowed by user.
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
     * The ping lock pool to control concurrent ping.
     */
    protected final LockPool pingLocks;

    /**
     * The read lock pool to control concurrent reads.
     */
    protected final LockPool readLocks;

    /**
     * The write lock pool to control concurrent writes.
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

        ioLocks= ( concurrencyParams.getConcurrentIo() >= 0 ? new LockPool( lockFactory, this.toString() + "-io-", concurrencyParams.getConcurrentIo() ) : null );
        pingAllowed= ( concurrencyParams.getConcurrentPings() != 0 );
        pingLocks= ( concurrencyParams.getConcurrentPings() > 0 ? new LockPool( lockFactory, this.toString() + "-ping-", concurrencyParams.getConcurrentPings() ) : null );
        readAllowed= ( concurrencyParams.getConcurrentReads() != 0 );
        readLocks= ( concurrencyParams.getConcurrentReads() > 0 ? new LockPool( lockFactory, this.toString() + "-read-", concurrencyParams.getConcurrentReads() ) : null );
        writeAllowed= ( concurrencyParams.getConcurrentWrites() != 0 );
        writeLocks= ( concurrencyParams.getConcurrentWrites() > 0 ? new LockPool( lockFactory, this.toString() + "-write-", concurrencyParams.getConcurrentWrites() ) : null );
        subscribeAllowed= ( concurrencyParams.getConcurrentSubscribes() != 0 );
        subscribeLocks= ( concurrencyParams.getConcurrentSubscribes() > 0
            ? new LockPool( lockFactory, this.toString() + "-subscribe-", concurrencyParams.getConcurrentSubscribes() ) : null );
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

    /**
     * Connect to PLC
     */
    @Override
    public synchronized void connect() throws InternalConnectionException
    {
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

    /**
     * Return true when the connection is valid.
     */
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

    /**
     * Ping IO locked.
     * @throws InternalUnsupportedException 
     */
    @Override
    public Boolean pingIoLocked( long timeout, TimeUnit timeoutUnit ) throws InterruptedException, InternalConcurrencyException, InternalUnsupportedException
    {
        if ( !pingAllowed ) throw new InternalConcurrencyException( "No ping allowed on this connection." );
        Lockable< Boolean > operation= () -> {
            return pingLocked( timeout, timeoutUnit );
        };
        try
        {
            return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
        }
        catch ( TimeoutException | InternalConnectionException | ExecutionException e )
        {
            if ( e.getCause() instanceof PlcUnsupportedOperationException )
            {
                throw new InternalUnsupportedException();
            }
            return Boolean.FALSE;
        }
    }

    /**
     * Ping locked.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return True when the PLC is reached, otherwise False
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws InternalConcurrencyException When operation is not allowed.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not established.
     * @throws InternalUnsupportedException 
     */
    private Boolean pingLocked( long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        TimeoutException,
        InternalConcurrencyException,
        ExecutionException,
        InternalConnectionException,
        InternalUnsupportedException
    {
        if ( pingLocks == null )
        {
            return ping( timeout, timeoutUnit );
        }
        else
        {
            Lockable< Boolean > operation= () -> {
                return ping( timeout, timeoutUnit );
            };
            return operation.doLocked( pingLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * Ping operation
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return True when the PLC is reached, otherwise False
     * @throws InterruptedException When the operations is interrupted.
     * @throws ExecutionException When execution failed.
     * @throws InternalUnsupportedException When ping operation is not supported by device.
     */
    public Boolean ping( long timeout, TimeUnit timeoutUnit ) throws InterruptedException, ExecutionException, InternalUnsupportedException
    {
        try
        {
            plcConnection.ping().get( timeout, timeoutUnit );
            return Boolean.TRUE;
        }
        catch ( ExecutionException e )
        {
            if ( e.getCause() instanceof UnsupportedOperationException )
            {
                throw new InternalUnsupportedException( "Ping operation not support by device.", e );
            }
            throw e;
        }
        catch ( TimeoutException e )
        {
            return Boolean.FALSE;
        }
    }

    @Override
    public boolean canRead()
    {
        return( readAllowed && plcConnection.getMetadata().canRead() );
    }

    /**
     * Read PLC fields.
     * @throws InternalUnsupportedException 
     */
    @Override
    public PlcReadResponse readIoLocked( List< ReadField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( !readAllowed ) throw new InternalConcurrencyException( "No read allowed on this connection." );
        Lockable< PlcReadResponse > operation= () -> {
            return readLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * Read PLC fields locked.
     * @param fields The PLC fields to read.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields read.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws InternalConcurrencyException When operation is not allowed.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not established.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    private PlcReadResponse readLocked( List< ReadField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( readLocks == null )
        {
            return read( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcReadResponse > operation= () -> {
                return read( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( readLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * Read PLC fields.
     * @param fields The PLC fields to read.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields read.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not establishe
     * @throws InternalConcurrencyException When the concurrent operation is not allowed.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    private PlcReadResponse read( List< ReadField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
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
     * Write PLC fields IO locked.
     */
    @Override
    public PlcWriteResponse writeIoLocked( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( !writeAllowed ) throw new InternalConcurrencyException( "No write allowed on this connection." );
        Lockable< PlcWriteResponse > operation= () -> {
            return writeLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
    * Write PLC fields locked.
    * @param fields The PLC fields to write.
    * @param timeout Operation timeout.
    * @param timeoutUnit Unit of the timeout value.
    * @return The PLC fields written.
    * @throws InterruptedException When the operations is interrupted.
    * @throws TimeoutException When operation duration exceeds timeout period.
    * @throws ExecutionException When execution failed.
    * @throws InternalConnectionException When connection to PLC is not establishe
    * @throws InternalConcurrencyException When the concurrent operation is not allowed.
    * @throws InternalUnsupportedException When the operation is not supported by device.
    */
    private PlcWriteResponse writeLocked( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( writeLocks == null )
        {
            return write( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcWriteResponse > operation= () -> {
                return write( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( writeLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * Write PLC fields.
     * @param fields The PLC fields to write.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields written.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not establishe
     * @throws InternalConcurrencyException When the concurrent operation is not allowed.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    private PlcWriteResponse write( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
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
     * Establish that the connection supports subscription.
     */
    @Override
    public boolean canSubscribe()
    {
        return( subscribeAllowed && plcConnection.getMetadata().canSubscribe() );
    }

    /**
     * Subscribe to PLC fields.
     */
    @Override
    public PlcSubscriptionResponse subscribeIoLocked( List< SubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( !subscribeAllowed ) throw new InternalConcurrencyException( "No subscribe allowed on this connection." );
        Lockable< PlcSubscriptionResponse > operation= () -> {
            return subscribeLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * Subscribe to PLC fields locked.
     * @param fields The PLC fields to write.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields subscribed to.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not establishe
     * @throws InternalConcurrencyException When the concurrent operation is not allowed.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    public PlcSubscriptionResponse subscribeLocked( List< SubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( subscribeLocks == null )
        {
            return subscribe( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcSubscriptionResponse > operation= () -> {
                return subscribe( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( subscribeLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * Subscribe to PLC fields.
     * @param fields The PLC fields to write.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields subscribed to.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not establishe
     * @throws InternalConcurrencyException When the concurrent operation is not allowed.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    public PlcSubscriptionResponse subscribe( List< SubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
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
     * Unsubscribe to PLC fields IO locked.
     * @throws InternalUnsupportedException 
     */
    @Override
    public PlcUnsubscriptionResponse unSubscribeIoLocked( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( !subscribeAllowed ) throw new InternalConcurrencyException( "No subscribe allowed on this connection." );
        Lockable< PlcUnsubscriptionResponse > operation= () -> {
            return unsubscribeLocked( fields, timeout, timeoutUnit );
        };
        return operation.doLocked( ioLocks, operation, timeout, timeoutUnit );
    }

    /**
     * Unsubscribe to PLC fields locked.
     * @param fields The PLC fields to write.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields unsubscribed to.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not establishe
     * @throws InternalConcurrencyException When the concurrent operation is not allowed.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    public PlcUnsubscriptionResponse unsubscribeLocked( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException,
        InternalUnsupportedException
    {
        if ( subscribeLocks == null )
        {
            return unsubscribe( fields, timeout, timeoutUnit );
        }
        else
        {
            Lockable< PlcUnsubscriptionResponse > operation= () -> {
                return unsubscribe( fields, timeout, timeoutUnit );
            };
            return operation.doLocked( subscribeLocks, operation, timeout, timeoutUnit );
        }
    }

    /**
     * Unsubscribe to PLC fields.
     * @param fields The PLC fields to write.
     * @param timeout Operation timeout.
     * @param timeoutUnit Unit of the timeout value.
     * @return The PLC fields unsubscribed to.
     * @throws InterruptedException When the operations is interrupted.
     * @throws TimeoutException When operation duration exceeds timeout period.
     * @throws ExecutionException When execution failed.
     * @throws InternalConnectionException When connection to PLC is not establishe
     * @throws InternalConcurrencyException When the concurrent operation is not allowed.
     * @throws InternalUnsupportedException When the operation is not supported by device.
     */
    public PlcUnsubscriptionResponse unsubscribe( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
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
        fields.forEach( ( field ) -> handles.remove( field.getAlias() ) );
        return subscribeResponse;
    }

    /**
     * Interface for lockable operations.
     */
    @FunctionalInterface
    private interface Lockable< R >
    {
        /**
         * Execute the operation
         * @return The result.
         * @throws InterruptedException When the operations is interrupted.
         * @throws TimeoutException When operation duration exceeds timeout period.
         * @throws ExecutionException When execution failed.
         * @throws InternalConnectionException When connection to PLC is not establishe
         * @throws InternalConcurrencyException When the concurrent operation is not allowed.
         * @throws InternalUnsupportedException When the operation is not supported by device.
         */
        R run() throws InterruptedException, ExecutionException, TimeoutException, InternalUnsupportedException, InternalConnectionException, InternalConcurrencyException;

        /**
         * @param pool The pool with locks. When null no locking is done.
         * @param operation The operation to execute locked.
         * @param timeout Operation timeout.
         * @param timeoutUnit Unit of the timeout value.
         * @return The result.
         * @throws InterruptedException When the operations is interrupted.
         * @throws TimeoutException When operation duration exceeds timeout period.
         * @throws ExecutionException When execution failed.
         * @throws InternalConnectionException When connection to PLC is not establishe
         * @throws InternalConcurrencyException When the concurrent operation is not allowed.
         * @throws InternalUnsupportedException When the operation is not supported by device.
         */
        default R doLocked( LockPool pool, Lockable< R > operation, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
            ExecutionException,
            TimeoutException,
            InternalConnectionException,
            InternalConcurrencyException,
            InternalUnsupportedException
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
