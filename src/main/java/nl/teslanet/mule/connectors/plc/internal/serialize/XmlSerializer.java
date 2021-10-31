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
package nl.teslanet.mule.connectors.plc.internal.serialize;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.plc4x.java.api.messages.PlcFieldResponse;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.ResponseCodeValueProvider;
import nl.teslanet.mule.connectors.plc.internal.error.ConnectorExecutionException;


public class XmlSerializer
{
    /**
    * Xml transformer factory for processing responses.
    */
    private static final TransformerFactory transformerFactory;

    /**
    * Create and configures transformerfactory instance.
    */
    static
    {
        transformerFactory= javax.xml.transform.TransformerFactory.newInstance();
        transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
        transformerFactory.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
    }
    
    /**
     * The Document Builer factory.
     */
    private static final DocumentBuilderFactoryImpl dbFactory= (DocumentBuilderFactoryImpl) DocumentBuilderFactoryImpl.newInstance();

    /**
     * Abstraction of the method to get PlcValues.
     */
    private interface PlcValueProvider
    {
        /**
         * Retrieve values.
         * @param valueAlias The alias of the field.
         * @return The value of the field.
         */
        PlcValue getValue( String valueAlias );
    }

    /**
     * No instances.
     */
    private XmlSerializer()
    {
    }

    /**
     * @param response The response to serialize to XML.
     * @return The serialized response document.
     * @throws ParserConfigurationException 
     */
    public static XmlSerializerResult xmlSerialize( PlcReadResponse response ) throws ParserConfigurationException
    {
        DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
        Document doc= dBuilder.newDocument();

        // root element
        Element rootElement= doc.createElement( "plcReadResponse" );
        doc.appendChild( rootElement );
        //build content
        boolean allOk= seralizeFields( doc, rootElement, response, alias -> response.getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize a write response.
     * @param response The write response to serialize.
     * @return The serialized result.
     * @throws ParserConfigurationException On failing XML configuration.
     */
    public static XmlSerializerResult xmlSerialize( PlcWriteResponse response ) throws ParserConfigurationException
    {
        DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
        Document doc= dBuilder.newDocument();

        // root element
        Element rootElement= doc.createElement( "plcWriteResponse" );
        doc.appendChild( rootElement );
        //build content
        boolean allOk= seralizeFields( doc, rootElement, response, alias -> response.getRequest().getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize a subscription response.
     * @param handlerName The name of the handler owning the subscription.
     * @param subscriptionName The name of the subscription.
     * @param response The subscription response to serialize.
     * @return The serialized result.
     * @throws ParserConfigurationException On failing XML configuration.
     */
    public static XmlSerializerResult xmlSerialize( String handlerName, String subscriptionName, PlcSubscriptionResponse response ) throws ParserConfigurationException
    {
        DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
        Document doc= dBuilder.newDocument();

        // root element
        Element rootElement= doc.createElement( "plcSubscribeResponse" );
        rootElement.setAttribute( "handler", handlerName );
        rootElement.setAttribute( "subscription", subscriptionName );
        doc.appendChild( rootElement );
        //build content
        boolean allOk= seralizeFields( doc, rootElement, response );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize a unsubscription response.
     * @param handlerName The name of the handler owning the subscription.
     * @param subscriptionName The name of the subscription.
     * @param response The unsubscription response to serialize.
     * @return The serialized result.
     * @throws ParserConfigurationException On failing XML configuration.
     */
    public static XmlSerializerResult xmlSerialize( String handlerName, String subscriptionName, PlcUnsubscriptionResponse response ) throws ParserConfigurationException
    {
        DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
        Document doc= dBuilder.newDocument();

        // root element
        Element rootElement= doc.createElement( "plcUnsubscribeResponse" );
        rootElement.setAttribute( "handler", handlerName );
        rootElement.setAttribute( "subscription", subscriptionName );
        doc.appendChild( rootElement );
        //build content
        boolean allOk= seralizeFields( doc, rootElement, response );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize an event.
     * @param event The event to serialize.
     * @return The serialized result.
     * @throws ParserConfigurationException On failing XML configuration.
     */
    public static XmlSerializerResult xmlSerialize( PlcSubscriptionEvent event ) throws ParserConfigurationException
    {
        DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
        Document doc= dBuilder.newDocument();

        // root element
        Element rootElement= doc.createElement( "plcEvent" );
        doc.appendChild( rootElement );
        //build content
        boolean allOk= seralizeFields( doc, rootElement, event, alias -> event.getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }
    
    /**
     * Create Mule Result that can be passed to Mule flow
     * @param responsePayload The payload contents of the message to return. 
     * @return The Result object created.
     */
    public static Result< InputStream, ReceivedResponseAttributes > createMuleResult( XmlSerializerResult responsePayload )
    {
        ByteArrayOutputStream outputStream= new ByteArrayOutputStream();
        try
        {
            Transformer transformer= transformerFactory.newTransformer();
            transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
            transformer.transform( new DOMSource( responsePayload.getDocument() ), new StreamResult( outputStream ) );
        }
        catch ( TransformerException e )
        {
            throw new ConnectorExecutionException( "Internal error on transforming read response.", e );
        }
        //small messages expected -> store payload in byte array
        ByteArrayInputStream inputStream= new ByteArrayInputStream( outputStream.toByteArray() );
        return Result.< InputStream, ReceivedResponseAttributes > builder().output( inputStream ).attributes(
            new ReceivedResponseAttributes( responsePayload.isIndicatesSucces() )
        ).mediaType( MediaType.APPLICATION_XML ).build();
    }

    /**
     * @param doc
     * @param parent
     * @param response
     * @param valueProvider
     * @return
     */
    private static boolean seralizeFields( Document doc, Element parent, PlcFieldResponse response, PlcValueProvider valueProvider )
    {
        boolean allOk= true;

        for ( String alias : response.getFieldNames() )
        {
            Element fieldElement= doc.createElement( "field" );
            fieldElement.setAttribute( "alias", alias );
            PlcResponseCode responseCode= response.getResponseCode( alias );
            allOk= allOk && ( responseCode == PlcResponseCode.OK );
            fieldElement.setAttribute( "responseCode", ResponseCodeValueProvider.getKey( responseCode ));
            try
            {
                PlcField field= response.getField( alias );
                fieldElement.setAttribute( "type", field.getPlcDataType() );
                fieldElement.setAttribute( "count", String.valueOf( field.getNumberOfElements() ) );
            }
            catch ( Exception e )
            {
                //Ignore
            }
            fieldElement.appendChild( xmlSeralize( doc, valueProvider.getValue( alias ) ) );
            parent.appendChild( fieldElement );
        }
        return allOk;
    }

    private static boolean seralizeFields( Document doc, Element parent, PlcSubscriptionResponse response )
    {
        boolean allOk= true;

        for ( String alias : response.getFieldNames() )
        {
            Element fieldElement= doc.createElement( "field" );
            fieldElement.setAttribute( "alias", alias );
            PlcResponseCode responseCode= response.getResponseCode( alias );
            allOk= allOk && ( responseCode == PlcResponseCode.OK );
            fieldElement.setAttribute( "responseCode", ResponseCodeValueProvider.getKey( responseCode ));
            parent.appendChild( fieldElement );
        }
        return allOk;
    }
    
    private static boolean seralizeFields( Document doc, Element parent, PlcUnsubscriptionResponse response )
    {
        boolean allOk= true;
        //No field info available
        return allOk;
    }
    
    private static Element xmlSeralize( Document doc, PlcValue plcValue )
    {
        return xmlSeralize( doc, null, plcValue );
    }

    private static Element xmlSeralize( Document doc, String key, PlcValue plcValue )
    {
        Element valueElement;
        if ( plcValue == null || plcValue.isNull() )
        {
            valueElement= doc.createElement( "nullValue" );
            appendOptionalAttribute( valueElement, "key", key );
        }
        else if ( plcValue.isSimple() )
        {
            valueElement= doc.createElement( "value" );
            appendOptionalAttribute( valueElement, "key", key );
            valueElement.appendChild( doc.createTextNode( plcValue.getObject().toString() ) );
        }
        else if ( plcValue.isList() )
        {
            valueElement= doc.createElement( "values" );
            appendOptionalAttribute( valueElement, "key", key );
            for ( PlcValue listItem : plcValue.getList() )
            {
                valueElement.appendChild( xmlSeralize( doc, null, listItem ) );
            }

        }
        else if ( plcValue.isStruct() )
        {
            valueElement= doc.createElement( "values" );
            appendOptionalAttribute( valueElement, "key", key );
            for ( Entry< String, ? extends PlcValue > structItem : plcValue.getStruct().entrySet() )
            {
                valueElement.appendChild( xmlSeralize( doc, structItem.getKey(), structItem.getValue() ) );
            }
        }
        else
        {
            valueElement= doc.createElement( "unkownValue" );
            appendOptionalAttribute( valueElement, "key", key );
        }
        return valueElement;
    }

    private static void appendOptionalAttribute( Element element, String name, String value )
    {
        if ( value != null )
        {
            element.setAttribute( name, value );
        }
    }

    /**
     * The result of serialization.
     */
    public static class XmlSerializerResult
    {
        /**
         * True when responseCodes for all fields is OK.
         */
        private boolean indicatesSucces;

        /**
         * The serialized response document.
         */
        private final Document document;

        /**
         * Constructor.
         * @param docIndicatesSucces the succes indicator of the response.
         * @param document The serialized response document.
         */
        private XmlSerializerResult( boolean docIndicatesSucces, Document document )
        {
            this.indicatesSucces= docIndicatesSucces;
            this.document= document;
        }

        /**
         * @return the success flag
         */
        public boolean isIndicatesSucces()
        {
            return indicatesSucces;
        }

        /**
         * @param indicatesSucces the success flag to set
         */
        public void setIndicatesSucces( boolean indicatesSucces )
        {
            this.indicatesSucces= indicatesSucces;
        }

        /**
         * @return the document
         */
        public Document getDocument()
        {
            return document;
        }
    }
}
