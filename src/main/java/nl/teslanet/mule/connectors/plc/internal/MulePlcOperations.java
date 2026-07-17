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
package nl.teslanet.mule.connectors.plc.internal;


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.sdk.api.annotation.error.Throws;
import org.mule.sdk.api.annotation.param.Config;
import org.mule.sdk.api.annotation.param.Connection;
import org.mule.sdk.api.annotation.param.ParameterGroup;
import org.mule.sdk.api.runtime.operation.Result;

import nl.teslanet.mule.connectors.plc.api.EventHandlingGroup;
import nl.teslanet.mule.connectors.plc.api.ReadRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.Subscription;
import nl.teslanet.mule.connectors.plc.api.Unsubscription;
import nl.teslanet.mule.connectors.plc.api.WriteRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.response.FieldResponse;
import nl.teslanet.mule.connectors.plc.api.response.FieldValueResponse;
import nl.teslanet.mule.connectors.plc.api.response.ResponseAttributes;
import nl.teslanet.mule.connectors.plc.internal.error.ConcurrencyException;
import nl.teslanet.mule.connectors.plc.internal.error.ConnectorExecutionException;
import nl.teslanet.mule.connectors.plc.internal.error.InvalidHandlerNameException;
import nl.teslanet.mule.connectors.plc.internal.error.IoErrorException;
import nl.teslanet.mule.connectors.plc.internal.error.OperationErrorProvider;
import nl.teslanet.mule.connectors.plc.internal.error.PingErrorProvider;
import nl.teslanet.mule.connectors.plc.internal.error.SubscribeErrorProvider;
import nl.teslanet.mule.connectors.plc.internal.error.UnsupportedException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalConcurrencyException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalConnectionException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidHandlerNameException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalUnsupportedException;
import nl.teslanet.mule.connectors.plc.internal.utils.MessageUtils;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MulePlcOperations
{
    /**
    * Default constructor
    */
    public MulePlcOperations()
    {
        //NOOP
    }

    /**
     * Ping the PLC.
    * @throws InterruptedException When the operation was interrupted.
     */
    @Throws( PingErrorProvider.class )
    public boolean ping( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection ) throws InterruptedException
    {
        try
        {
            return connection.pingIoLocked( configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( InternalConcurrencyException e )
        {
            throw new ConcurrencyException( "Concurrency Error on ping.", e );
        }
        catch ( InternalUnsupportedException e )
        {
            throw new UnsupportedException( "Protocol does not support ping.", e );
        }
    }

    /**
    * Read PLC flields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param requestBuilder The builder containing request parameters.
    * @return The read response as Result
    * @throws ConnectionException When the connection is not healthy.
    * @throws InterruptedException When the operation was interrupted.
    */
    @Throws( OperationErrorProvider.class )
    public Result< List< FieldValueResponse >, ResponseAttributes > read( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Request" )
    ReadRequestBuilder requestBuilder ) throws InterruptedException, ConnectionException
    {
        PlcReadResponse response= doRead( configuration, connection, requestBuilder );
        boolean success= true;
        for ( String tagName : response.getTagNames() )
        {
            if ( response.getResponseCode( tagName ) != PlcResponseCode.OK )
            {
                success= false;
            }
        }
        if ( !success && requestBuilder.isThrowExceptionOnIoError() ) throw new IoErrorException( "One or more fields are not successfully read." );
        ResponseAttributes responseAttributes= new ResponseAttributes( success );
        List< FieldValueResponse > fieldResponses= MessageUtils.createFieldResponses( response );
        return Result.< List< FieldValueResponse >, ResponseAttributes > builder().output( fieldResponses ).attributes( responseAttributes ).build();
    }

    /**
    * Read PLC flields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param requestBuilder The builder containing request parameters.
    * @return The read response as Result
    * @throws ConnectionException When the connection is not healthy.
    * @throws InterruptedException When the operation was interrupted.
    */
    private PlcReadResponse doRead( MulePlcConfig configuration, MulePlcConnection connection, ReadRequestBuilder requestBuilder ) throws InterruptedException, ConnectionException
    {
        // Check if this connection support reading of data.
        if ( !connection.canRead() )
        {
            throw new UnsupportedException( "Protocol does not support read." );
        }
        PlcReadResponse response= null;
        try
        {
            response= connection.readIoLocked( requestBuilder.getReadFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on read.", e );
        }
        catch ( InternalConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "Connection Error on read.", e );
        }
        catch ( InternalConcurrencyException e )
        {
            throw new ConcurrencyException( "Concurrency Error on read.", e );
        }
        catch ( InternalUnsupportedException e )
        {
            throw new UnsupportedException( "Operation does not support read." );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on read." );
        }
        return response;
    }

    /**
    * Write PLC fields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param requestBuilder The builder containing request parameters.
    * @return The writeResponse as Result.
    * @throws ConnectionException When the connection is not healthy.
    * @throws InterruptedException When the operation was interrupted.
    */
    @Throws( OperationErrorProvider.class )
    public Result< List< FieldResponse >, ReceivedResponseAttributes > write( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Request" )
    WriteRequestBuilder requestBuilder ) throws InterruptedException, ConnectionException
    {
        PlcWriteResponse response= doWrite( configuration, connection, requestBuilder );
        boolean success= true;
        for ( String tagName : response.getTagNames() )
        {
            if ( response.getResponseCode( tagName ) != PlcResponseCode.OK )
            {
                success= false;
            }
        }
        if ( !success && requestBuilder.isThrowExceptionOnIoError() ) throw new IoErrorException( "One or more fields are not successfully written." );
        ReceivedResponseAttributes responseAttributes= new ReceivedResponseAttributes( success );
        List< FieldResponse > fieldResponses= MessageUtils.createFieldResponses( response );
        return Result.< List< FieldResponse >, ReceivedResponseAttributes > builder().output( fieldResponses ).attributes( responseAttributes ).build();
    }

    /**
     * Execute write operation.
     * @param configuration The PLC connector configuration.
     * @param connection The connection instance
     * @param requestBuilder The builder containing request parameters.
     * @return The writeResponse as Result.
     * @throws ConnectionException When connection is lost.
     * @throws InterruptedException When the operation was interrupted.
     */
    private PlcWriteResponse doWrite( MulePlcConfig configuration, MulePlcConnection connection, WriteRequestBuilder requestBuilder ) throws InterruptedException,
        ConnectionException
    {
        // Check if this connection support writing of data.
        if ( !connection.canWrite() )
        {
            throw new UnsupportedException( "Protocol does not support write." );
        }
        PlcWriteResponse response= null;
        try
        {
            response= connection.writeIoLocked( requestBuilder.getWriteFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on write.", e );
        }
        catch ( InternalConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "Connection Error on write.", e );
        }
        catch ( InternalConcurrencyException e )
        {
            throw new ConcurrencyException( "Concurrency Error on read.", e );
        }
        catch ( InternalUnsupportedException e )
        {
            throw new UnsupportedException( "Operation does not support write." );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on write." );
        }
        return response;
    }

    /**
     * Subscribe PLC fields.
     * @param configuration The PLC connector configuration.
     * @param connection The connection instance
     * @param subscription The subscription parameters.
     * @return The readResponse as Result
     * @throws ConnectionException 
     * @throws InterruptedException When the operation was interrupted.
     */
    @Throws( SubscribeErrorProvider.class )
    public Result< List< FieldResponse >, ReceivedResponseAttributes > subscribe( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Event Handling" )
    EventHandlingGroup eventHandling, @ParameterGroup( name= "Subscription" )
    Subscription subscription ) throws ConnectionException, InterruptedException
    {
        PlcSubscriptionResponse response= doSubscribe( configuration, connection, subscription, eventHandling );
        boolean success= true;
        for ( String tagName : response.getTagNames() )
        {
            if ( response.getResponseCode( tagName ) != PlcResponseCode.OK )
            {
                success= false;
            }
        }
        if ( !success && subscription.isThrowExceptionOnIoError() ) throw new IoErrorException( "One or more fields are not successfully subscribed to." );
        ReceivedResponseAttributes responseAttributes= new ReceivedResponseAttributes( success );
        List< FieldResponse > fieldResponses= MessageUtils.createFieldResponses( response );
        return Result.< List< FieldResponse >, ReceivedResponseAttributes > builder().output( fieldResponses ).attributes( responseAttributes ).build();
    }

    /**
     * Execute subscribe operation.
     * @param configuration The PLC connector configuration.
     * @param connection The connection instance
     * @param subscription The builder containing request parameters.
     * @param eventHandling The event handling parameters
     * @return The writeResponse as Result.
     * @throws ConnectionException When connection is lost.
     * @throws InterruptedException When the operation was interrupted.
    **/
    private PlcSubscriptionResponse doSubscribe( MulePlcConfig configuration, MulePlcConnection connection, Subscription subscription, EventHandlingGroup eventHandling )
        throws ConnectionException,
        InterruptedException
    {
        // Check if this connection supports subscribing.
        if ( !connection.canSubscribe() )
        {
            throw new UnsupportedException( "Protocol does not support subscribing." );
        }
        String handlerName= eventHandling.getEventHandler().getHandlerName();
        EventProcessor eventProcessor;
        try
        {
            eventProcessor= EventProcessor.getEventProcessor( handlerName );
        }
        catch ( InternalInvalidHandlerNameException e )
        {
            throw new InvalidHandlerNameException( "Handler is invalid { " + handlerName + " }", e );
        }
        PlcSubscriptionResponse response= null;
        try
        {
            response= connection.subscribeIoLocked( subscription.getSubscribeFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on subscription.", e );
        }
        catch ( InternalConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "Connection Error on subscription.", e );
        }
        catch ( InternalConcurrencyException e )
        {
            throw new ConcurrencyException( "Concurrency Error on subscribe.", e );
        }
        catch ( InternalUnsupportedException e )
        {
            throw new UnsupportedException( "Operation does not support subscribing." );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on subscription." );
        }
        //register subscription
        eventProcessor.register( response );
        return response;
    }

    /**
    * Unsubscribe PLC fields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param unsubscription The subscription parameters.
    * @throws ConnectionException 
    * @throws InterruptedException When the operation was interrupted.
    */
    @Throws( SubscribeErrorProvider.class )
    public void unsubscribe( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Subscription" )
    Unsubscription unsubscription ) throws ConnectionException, InterruptedException
    {
        doUnSubscribe(configuration, connection, unsubscription);
    }
    
    /**
     * Execute un-subscribe operation.
     * @param configuration The PLC connector configuration.
     * @param connection The connection instance
     * @param unsubscription The builder containing request parameters.
     * @return The writeResponse as Result.
     * @throws ConnectionException When connection is lost.
     * @throws InterruptedException When the operation was interrupted.
    **/
    private PlcUnsubscriptionResponse doUnSubscribe( MulePlcConfig configuration, MulePlcConnection connection, Unsubscription unsubscription )
        throws ConnectionException,
        InterruptedException
    {
        // Check if this connection supports subscribing.
        if ( !connection.canSubscribe() )
        {
            throw new UnsupportedException( "Protocol does not support unsubscribing." );
        }
        PlcUnsubscriptionResponse response= null;
        try
        {
            response= connection.unSubscribeIoLocked( unsubscription.getUnsubscribeFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on unsubscription.", e );
        }
        catch ( InternalConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "Connection Error on unsubscription.", e );
        }
        catch ( InternalConcurrencyException e )
        {
            throw new ConcurrencyException( "Concurrency Error on unsubscribe.", e );
        }
        catch ( InternalUnsupportedException e )
        {
            throw new UnsupportedException( "Operation does not support unsubscribing." );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on unsubscription." );
        }
        return response;
    }
}
