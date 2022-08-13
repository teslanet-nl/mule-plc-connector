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


import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.fixed.OutputXmlType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.operation.Result;

import nl.teslanet.mule.connectors.plc.api.EventHandlingGroup;
import nl.teslanet.mule.connectors.plc.api.ReadRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.Subscription;
import nl.teslanet.mule.connectors.plc.api.Unsubscription;
import nl.teslanet.mule.connectors.plc.api.WriteRequestBuilder;
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
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer.XmlSerializerResult;


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
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.ANY, strict= false )
    @Throws( PingErrorProvider.class )
    public Boolean ping( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection ) throws InterruptedException
    {
        try
        {
            return connection.ping();
        }
        catch ( InternalUnsupportedException e )
        {
            throw new UnsupportedException( "Protocol does not support ping." );
        }
    }

    /**
    * Read PLC flields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param requestBuilder The builder containing request parameters.
    * @return The readResponse as Result
    * @throws ConnectionException 
    * @throws InterruptedException When the operation was interrupted.
     * @throws InternalConcurrencyException When the operation is not allowed.
    */
    @MediaType( value= MediaType.APPLICATION_XML, strict= true )
    @Throws( OperationErrorProvider.class )
    @OutputXmlType(qname = "{https://www.teslanet.nl/schema/mule/connectors/plc/v1/plc.xsd}plcReadResponse", schema = "nl/teslanet/mule/connectors/plc/v1/plc.xsd")
    public Result< InputStream, ReceivedResponseAttributes > read( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Request" )
    ReadRequestBuilder requestBuilder ) throws ConnectionException, InterruptedException
    {
        // Check if this connection support reading of data.
        if ( !connection.canRead() )
        {
            throw new UnsupportedException( "Protocol does not support read." );
        }
        PlcReadResponse response= null;
        try
        {
            response= connection.read( requestBuilder.getReadFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
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
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on read." );
        }
        XmlSerializerResult responsePayload;
        try
        {
            responsePayload= XmlSerializer.xmlSerialize( response );
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing read response.", e );
        }
        if ( requestBuilder.isThrowExceptionOnIoError() && !responsePayload.isIndicatesSucces() ) throw new IoErrorException( "One or more fields are not successfully read" );
        return XmlSerializer.createMuleResult( responsePayload );
    }

    /**
    * Write PLC fields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param requestBuilder The builder containing request parameters.
    * @return The writeResponse as Result.
    * @throws ConnectionException When connection is lost.
    * @throws InterruptedException When the operation was interrupted.
     * @throws InternalConcurrencyException 
    */
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
    @Throws( OperationErrorProvider.class )
    public Result< InputStream, ReceivedResponseAttributes > write( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Request" )
    WriteRequestBuilder requestBuilder ) throws ConnectionException, InterruptedException
    {
        // Check if this connection support writing of data.
        if ( !connection.canWrite() )
        {
            throw new UnsupportedException( "Protocol does not support write." );
        }
        PlcWriteResponse response= null;
        try
        {
            response= connection.write( requestBuilder.getWriteFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
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
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on write." );
        }
        XmlSerializerResult responsePayload;
        try
        {
            responsePayload= XmlSerializer.xmlSerialize( response );
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing write response.", e );
        }
        if ( requestBuilder.isThrowExceptionOnIoError() && !responsePayload.isIndicatesSucces() ) throw new IoErrorException( "One or more fields are not successfully written" );
        return XmlSerializer.createMuleResult( responsePayload );
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
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
    @Throws( SubscribeErrorProvider.class )
    public Result< InputStream, ReceivedResponseAttributes > subscribe( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Event Handling" )
    EventHandlingGroup eventHandling, @ParameterGroup( name= "Subscription" )
    Subscription subscription ) throws ConnectionException, InterruptedException
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
            response= connection.subscribe( subscription.getSubscribeFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on subscription.", e );
        }
        catch ( InternalConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "Connection Error on subscription.", e );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on subscription." );
        }
        XmlSerializerResult responsePayload;
        try
        {
            responsePayload= XmlSerializer.xmlSerialize( response );
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing read response.", e );
        }
        if ( subscription.isThrowExceptionOnIoError() && !responsePayload.isIndicatesSucces() ) throw new IoErrorException( "One or more fields are not successfully read" );
        //register subscription
        eventProcessor.register( response );
        return XmlSerializer.createMuleResult( responsePayload );
    }

    /**
    * Unsubscribe PLC fields.
    * @param configuration The PLC connector configuration.
    * @param connection The connection instance
    * @param unsubscription The subscription parameters.
    * @return The readResponse as Result
    * @throws ConnectionException 
    * @throws InterruptedException When the operation was interrupted.
    */
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
    @Throws( SubscribeErrorProvider.class )
    public Result< InputStream, ReceivedResponseAttributes > unsubscribe( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Subscription" )
    Unsubscription unsubscription ) throws ConnectionException, InterruptedException
    {
        // Check if this connection supports subscribing.
        if ( !connection.canSubscribe() )
        {
            throw new UnsupportedException( "Protocol does not support subscribing." );
        }
        PlcUnsubscriptionResponse response= null;
        try
        {
            response= connection.unSubscribe( unsubscription.getUnsubscribeFields(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on unsubscription.", e );
        }
        catch ( InternalConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "Connection Error on subscription.", e );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on unsubscription." );
        }
        XmlSerializerResult responsePayload;
        try
        {
            responsePayload= XmlSerializer.xmlSerialize( response );
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing read response.", e );
        }
        if ( unsubscription.isThrowExceptionOnIoError() && !responsePayload.isIndicatesSucces() ) throw new IoErrorException( "One or more fields are not successfully read" );
        return XmlSerializer.createMuleResult( responsePayload );
    }
}
