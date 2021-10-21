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
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.messages.PlcSubscriptionRequest;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nl.teslanet.mule.connectors.plc.api.ReadField;
import nl.teslanet.mule.connectors.plc.api.ReadRequestBuilder;
import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.internal.error.ConnectorExecutionException;
import nl.teslanet.mule.connectors.plc.internal.error.ConnectorInterruptedException;
import nl.teslanet.mule.connectors.plc.internal.error.UnsupportedException;


/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class MulePlcSubscriptionOperations
{
    private final Logger logger= LoggerFactory.getLogger( MulePlcSubscriptionOperations.class );


    /**
     *  Subscribe PLC items. NOT READY FOR USE.
     * @throws ConnectionException 
     */
    @MediaType(value= MediaType.ANY, strict= true)
    public Result< InputStream, ReceivedResponseAttributes > subscribe(
        @Config MulePlcConfig configuration,
        @Connection PlcConnection connection,
        @ParameterGroup(name= "Request") ReadRequestBuilder readRequestBuilder ) throws UnsupportedException, ConnectorExecutionException, ConnectorInterruptedException, ConnectionException
    {
        // Check if this connection support reading of data.
        if ( !connection.getMetadata().canRead() )
        {
            logger.error( "This connection doesn't support subscribing." );
            throw new UnsupportedException( "This connection doesn't support subscribing." );
        }
        //prepare response
        DocumentBuilderFactoryImpl dbFactory= (DocumentBuilderFactoryImpl) DocumentBuilderFactoryImpl.newInstance();
        DocumentBuilder dBuilder;
        try
        {
            dBuilder= dbFactory.newDocumentBuilder();
        }
        catch ( ParserConfigurationException e )
        {
            throw new ConnectorExecutionException( "Internal error on serializing write response.", e );
        }
        Document responseDom= dBuilder.newDocument();

        //root element
        Element rootElement= responseDom.createElement( "subscribeResponse" );
        responseDom.appendChild( rootElement );
        //request element
        Element requestElement= responseDom.createElement( "subscribeRequest" );
        rootElement.appendChild( requestElement );

        PlcSubscriptionRequest.Builder builder= connection.subscriptionRequestBuilder();
        for ( ReadField item : readRequestBuilder.getReadFields() )
        {
            builder.addChangeOfStateField( item.getAlias(), item.getAddress() );
            //requestItem element
            Element requestItemElement= responseDom.createElement( "subscribeItem" );
            requestElement.appendChild( requestItemElement );
            requestItemElement.setAttribute( "alias", item.getAlias() );
            requestItemElement.setAttribute( "address", item.getAddress() );
        }
        PlcSubscriptionResponse response= null;
        try
        {
            response= (PlcSubscriptionResponse) builder.build().execute().get( configuration.getTimeout(), configuration.getTimeoutUnits() );
        }
        catch ( ExecutionException e )
        {
            throw new ConnectorExecutionException( "Execution Error on read.", e );
        }
        catch ( InterruptedException e )
        {
            throw new ConnectorInterruptedException( "Interruption on read.", e );
        }
        catch ( TimeoutException e )
        {
            throw new ConnectionException( "IO Error on read.", e );
        }
        //response element
        Element responseElement= responseDom.createElement( "subscribeResponse" );
        rootElement.appendChild( responseElement );
        boolean allOk= true;
        for ( String fieldName : response.getFieldNames() )
        {
            //requestItem element
            Element responseItemElement= responseDom.createElement( "subscribeResult" );
            responseElement.appendChild( responseItemElement );
            responseItemElement.setAttribute( "alias", fieldName );
            PlcResponseCode responseCode= response.getResponseCode( fieldName );
            allOk= allOk && ( responseCode == PlcResponseCode.OK );
            responseItemElement.setAttribute( "result", responseCode.name() );
        }
        //build content
        //Collection< PlcSubscriptionHandle > handels= response.getSubscriptionHandles();
        //create inputstream
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        try
        {
            TransformerFactory.newInstance().newTransformer().transform( new DOMSource( responseDom ), new StreamResult( outputStream ) );
        }
        catch ( TransformerException | TransformerFactoryConfigurationError e )
        {
            throw new ConnectorExecutionException( "Internal error on transforming read response.", e );
        }
        byte[] bytes= outputStream.toByteArray();
        return Result.< InputStream, ReceivedResponseAttributes > builder().output( new ByteArrayInputStream (bytes) ).attributes(
            new ReceivedResponseAttributes( allOk ) ).build();
    }
}
