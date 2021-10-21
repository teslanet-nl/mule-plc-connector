/*-
 * #%L
 * Mule PLC Connector
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
import java.util.concurrent.TimeoutException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.operation.Result;

import nl.teslanet.mule.connectors.plc.api.ReadRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.WriteRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.error.ConnectorExecutionException;
import nl.teslanet.mule.connectors.plc.api.error.ConnectorInterruptedException;
import nl.teslanet.mule.connectors.plc.api.error.UnsupportedException;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer.XmlSerializerResult;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MulePlcOperations
{
    /**
     * Xml transformer factory for processing responses.
     */
    private final TransformerFactory transformerFactory;

    /**
     * Default constructor
     * Creates and configures transformerfactory instance.
     */
    public MulePlcOperations()
    {
        transformerFactory= javax.xml.transform.TransformerFactory.newInstance();
        transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
        transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
    }

    /**
     * Ping the PLC.
     */
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.ANY, strict= false )
    public Boolean ping( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection )
    {
        return connection.ping();
    }

    /**
     *  Read PLC items.
     * @param configuration
     * @param connection
     * @param requestBuilder
     * @return The readResponse as Result
     * @throws UnsupportedException
     * @throws ConnectorExecutionException
     * @throws ConnectorInterruptedException
     * @throws ConnectionException
     */
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
    public Result< InputStream, ReceivedResponseAttributes > read( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Request" )
    ReadRequestBuilder requestBuilder ) throws UnsupportedException, ConnectorExecutionException, ConnectorInterruptedException, ConnectionException
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
        catch ( InterruptedException e )
        {
            throw new ConnectorInterruptedException( "Interruption on read.", e );
        }
        catch ( ConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "IO Error on read.", e );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on read." );
        }
        XmlSerializerResult responseResult;
        try
        {
            responseResult= XmlSerializer.xmlSerialize( response );
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing read response.", e );
        }
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        try
        {
            transformerFactory.newTransformer().transform( new DOMSource( responseResult.getDocument() ), new StreamResult( outputStream ) );
        }
        catch ( TransformerException e )
        {
            throw new ConnectorExecutionException( "Internal error on transforming read response.", e );
        }
        byte[] bytes= outputStream.toByteArray();
        return Result.< InputStream, ReceivedResponseAttributes > builder().output( new ByteArrayInputStream( bytes ) ).attributes(
            new ReceivedResponseAttributes( responseResult.isDocIndicatesSucces() )
        ).mediaType( MediaType.APPLICATION_XML ).build();
    }

    /**
     *  Write PLC items.
     * @param configuration
     * @param connection
     * @param requestBuilder
     * @return The wrtieResponse as Result.
     * @throws UnsupportedException
     * @throws ConnectorExecutionException
     * @throws ConnectorInterruptedException
     * @throws ConnectionException
     */
    @org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
    public Result< InputStream, ReceivedResponseAttributes > write( @Config
    MulePlcConfig configuration, @Connection
    MulePlcConnection connection, @ParameterGroup( name= "Request" )
    WriteRequestBuilder requestBuilder ) throws UnsupportedException, ConnectorExecutionException, ConnectorInterruptedException, ConnectionException
    {
        // Check if this connection support writing of data.
        if ( !connection.canWrite() )
        {
            throw new UnsupportedException( "Protocol does not support write." );
        }
        PlcWriteResponse response= null;
        try
        {
            response= connection.write( requestBuilder.getWriteItems(), configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on write.", e );
        }
        catch ( InterruptedException e )
        {
            throw new ConnectorInterruptedException( "Interruption on write.", e );
        }
        catch ( ConnectionException | TimeoutException e )
        {
            throw new ConnectionException( "IO Error on write.", e );
        }
        if ( response == null )
        {
            throw new ConnectorExecutionException( "Null response on write." );
        }
        XmlSerializerResult responseResult;
        try
        {
            responseResult= XmlSerializer.xmlSerialize( response );
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing write response.", e );
        }
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        try
        {
            transformerFactory.newTransformer().transform( new DOMSource( responseResult.getDocument() ), new StreamResult( outputStream ) );
        }
        catch ( TransformerException e )
        {
            throw new ConnectorExecutionException( "Internal error on transforming write response.", e );
        }
        byte[] bytes= outputStream.toByteArray();
        return Result.< InputStream, ReceivedResponseAttributes > builder().output( new ByteArrayInputStream( bytes ) ).attributes(
            new ReceivedResponseAttributes( responseResult.isDocIndicatesSucces() )
        ).mediaType( MediaType.APPLICATION_XML ).build();
    }
}
