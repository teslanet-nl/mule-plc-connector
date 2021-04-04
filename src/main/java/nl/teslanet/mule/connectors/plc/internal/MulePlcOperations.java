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
package nl.teslanet.mule.connectors.plc.internal;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcReadRequest;
import org.apache.plc4x.java.api.messages.PlcWriteRequest;
import org.apache.plc4x.java.spi.messages.DefaultPlcReadResponse;
import org.apache.plc4x.java.spi.messages.DefaultPlcWriteResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import nl.teslanet.mule.connectors.plc.api.ReadItem;
import nl.teslanet.mule.connectors.plc.api.ReadRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.WriteItem;
import nl.teslanet.mule.connectors.plc.api.WriteRequestBuilder;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MulePlcOperations
{
    private final Logger LOGGER= LoggerFactory.getLogger( MulePlcOperations.class );

    /**
     * Ping the PLC.
     */
    @MediaType(value= MediaType.ANY, strict= false)
    public Boolean ping( @Config MulePlcConfig configuration, @Connection MulePlcConnection connection )
    {
        try
        {
            connection.getPlcConnection().ping().get();
        }
        catch ( ConnectionException | InterruptedException | ExecutionException e )
        {
            //TODO better to throw when not supprted.
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     *  Read PLC items.
     * @throws Exception 
     */
    @MediaType(value= MediaType.ANY, strict= true)
    public Result< InputStream, ReceivedResponseAttributes > read(
        @Config MulePlcConfig configuration,
        @Connection MulePlcConnection connection,
        @ParameterGroup(name= "Request") ReadRequestBuilder readRequestBuilder ) throws Exception
    {
        PlcConnection plcConnection= connection.getPlcConnection();
        // Check if this connection support reading of data.
        if ( !plcConnection.getMetadata().canRead() )
        {
            //TODO
            LOGGER.error( "This connection doesn't support reading." );
            throw new Exception( "This connection doesn't support reading." );
        }
        PlcReadRequest.Builder builder= plcConnection.readRequestBuilder();
        for ( ReadItem item : readRequestBuilder.getItems() )
        {
            builder.addItem( item.getAlias(), item.getAddress() );
        }
        DefaultPlcReadResponse response= null;
        try
        {
            response= (DefaultPlcReadResponse) builder.build().execute().get( configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( Exception e )
        {
            // TODO specify exception
            LOGGER.error( "Error on read." );
            throw new ConnectionException( "Error on read.", e );
        }
        if ( response == null )
        {
            LOGGER.error( "Null response on read." );
            throw new Exception( "Null response on read." );
        }
        Document responseDom= XmlSerializer.xmlSerialize( response );
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer().transform( new DOMSource( responseDom ), new StreamResult( outputStream ) );
        byte[] bytes= outputStream.toByteArray();
        return Result.< InputStream, ReceivedResponseAttributes > builder().output( new ByteArrayInputStream( bytes ) ).attributes(
            new ReceivedResponseAttributes( "read" ) ).build();
    }

    /**
     *  Write PLC items.
     * @throws Exception 
     */
    @MediaType(value= MediaType.ANY, strict= true)
    public Result< InputStream, ReceivedResponseAttributes > write(
        @Config MulePlcConfig configuration,
        @Connection MulePlcConnection connection,
        @ParameterGroup(name= "Request") WriteRequestBuilder requestBuilder ) throws Exception
    {
        PlcConnection plcConnection= connection.getPlcConnection();
        // Check if this connection support reading of data.
        if ( !plcConnection.getMetadata().canWrite() )
        {
            LOGGER.error( "This connection doesn't support writing." );
            throw new Exception( "This connection doesn't support writing." );
        }
        PlcWriteRequest.Builder builder= plcConnection.writeRequestBuilder();
        for ( WriteItem item : requestBuilder.getWriteItems() )
        {
            builder.addItem( item.getAlias(), item.getAddress(), item.getValues().toArray() );
        }
        DefaultPlcWriteResponse response= null;
        try
        {
            response= (DefaultPlcWriteResponse) builder.build().execute().get( configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( Exception e )
        {
            // TODO specify exception
            LOGGER.error( "Error on write." );
            throw new ConnectionException( "Error on write.", e );
        }
        if ( response == null )
        {
            LOGGER.error( "Null response on read." );
            throw new Exception( "Null response on write." );
        }
        Document responseDom= XmlSerializer.xmlSerialize( response );
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        TransformerFactory.newInstance().newTransformer().transform( new DOMSource( responseDom ), new StreamResult( outputStream ) );
        byte[] bytes= outputStream.toByteArray();
        return Result.< InputStream, ReceivedResponseAttributes > builder().output( new ByteArrayInputStream( bytes ) ).attributes(
            new ReceivedResponseAttributes( "write" ) ).build();
    }
}
