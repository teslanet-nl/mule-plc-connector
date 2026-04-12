/*-
 * #%L
 * Mule CoAP Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcTagResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.ArrayInfo;
import org.apache.plc4x.java.api.model.PlcTag;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.sdk.api.runtime.operation.Result;

import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;


public class XmlSerializer
{
    /**
     * Abstraction of the method to get PlcValues.
     */
    private interface PlcValueProvider
    {
        /**
         * Retrieve values.
         * 
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
        // NOOP
    }

    /**
     * @param response The response to serialize to XML.
     * @return The serialized response document.
     * @throws ParserConfigurationException
     */
    public static XmlSerializerResult xmlSerialize( PlcReadResponse response )
    {
        Document doc= DocumentHelper.createDocument();
        // root element
        Element rootElement= doc.addElement( "plcReadResponse" );
        // build content
        boolean allOk= seralizeFields( rootElement, response, alias -> response.getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize a write response.
     * 
     * @param response The write response to serialize.
     * @return The serialized result.
     */
    public static XmlSerializerResult xmlSerialize( PlcWriteResponse response )
    {
        Document doc= DocumentHelper.createDocument();
        // root element
        Element rootElement= doc.addElement( "plcWriteResponse" );
        // build content
        boolean allOk= seralizeFields( rootElement, response, alias -> response.getRequest().getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize a subscription response.
     * 
     * @param response The subscription response to serialize.
     * @return The serialized result.
     */
    public static XmlSerializerResult xmlSerialize( PlcSubscriptionResponse response )
    {
        Document doc= DocumentHelper.createDocument();
        // root element
        Element rootElement= doc.addElement( "plcSubscribeResponse" );
        // build content
        boolean allOk= seralizeFields( rootElement, response );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize a unsubscription response.
     * 
     * @param response The subscription response to serialize.
     * @return The serialized result.
     */
    public static XmlSerializerResult xmlSerialize( PlcUnsubscriptionResponse response )
    {
        Document doc= DocumentHelper.createDocument();
        // root element
        Element rootElement= doc.addElement( "plcUnsubscribeResponse" );
        // build content
        boolean allOk= seralizeFields( rootElement, response );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Serialize an event.
     * 
     * @param event The event to serialize.
     * @return The serialized result.
     */
    public static XmlSerializerResult xmlSerialize( PlcSubscriptionEvent event )
    {
        Document doc= DocumentHelper.createDocument();
        // root element
        Element rootElement= doc.addElement( "plcEvent" );
        // add timestamp
        rootElement.addAttribute( "ts", Instant.now().toString() );
        // build content
        boolean allOk= seralizeFields( rootElement, event, alias -> event.getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    /**
     * Create Mule Result that can be passed to Mule flow
     * 
     * @param responsePayload The payload contents of the message to return.
     * @return The Result object created.
     */
    public static Result< InputStream, ReceivedResponseAttributes > createMuleResult( XmlSerializerResult responsePayload )
    {
        // small messages expected -> store payload in byte array
        return Result
            .< InputStream, ReceivedResponseAttributes > builder()
            .output( createInputStream( responsePayload ) )
            .attributes( new ReceivedResponseAttributes( responsePayload.isIndicatesSucces() ) )
            .mediaType( MediaType.APPLICATION_XML.withCharset( StandardCharsets.ISO_8859_1 ) )
            .build();
    }

    /**
     * Create InputStream from result.
     * 
     * @param responsePayload The payload contents of the message to return.
     * @return The InputStream created.
     */
    public static InputStream createInputStream( XmlSerializerResult responsePayload )
    {
        // small messages expected -> store payload in byte array
        return new ByteArrayInputStream( responsePayload.getDocument().asXML().getBytes() );
    }

    /**
     * @param doc
     * @param parent
     * @param response
     * @param valueProvider
     * @return
     */
    private static boolean seralizeFields( Branch parent, PlcTagResponse response, PlcValueProvider valueProvider )
    {
        boolean allOk= true;

        for ( String alias : response.getTagNames() )
        {
            Element fieldElement= parent.addElement( "field" );
            fieldElement.addAttribute( "alias", alias );
            PlcResponseCode responseCode= response.getResponseCode( alias );
            allOk= allOk && ( responseCode == PlcResponseCode.OK );
            fieldElement.addAttribute( "responseCode", responseCode.name() );
            try
            {
                PlcTag field= response.getTag( alias );
                fieldElement.addAttribute( "type", field.getPlcValueType().name() );
                int count= 0;
                for ( ArrayInfo info : field.getArrayInfo() )
                {
                    count+= info.getSize();
                }
                fieldElement.addAttribute( "count", String.valueOf( count == 0 ? 1 : count ) );
            }
            catch ( Exception e )
            {
                // Ignore
            }
            xmlSeralize( fieldElement, valueProvider.getValue( alias ) );
        }
        return allOk;
    }

    private static boolean seralizeFields( Branch parent, PlcSubscriptionResponse response )
    {
        boolean allOk= true;

        for ( String alias : response.getTagNames() )
        {
            Element fieldElement= parent.addElement( "field" );
            fieldElement.addAttribute( "alias", alias );
            PlcResponseCode responseCode= response.getResponseCode( alias );
            allOk= allOk && ( responseCode == PlcResponseCode.OK );
            fieldElement.addAttribute( "responseCode", responseCode.name() );
        }
        return allOk;
    }

    private static boolean seralizeFields( Branch parent, PlcUnsubscriptionResponse response )
    {
        // No field info available
        return true;
    }

    private static void xmlSeralize( Branch parent, PlcValue plcValue )
    {
        xmlSeralize( parent, null, plcValue );
    }

    private static void xmlSeralize( Branch parent, String key, PlcValue plcValue )
    {
        Element valueElement;
        if ( plcValue == null || plcValue.isNull() )
        {
            valueElement= parent.addElement( "nullValue" );
            appendOptionalAttribute( valueElement, "key", key );
        }
        else if ( plcValue.isSimple() )
        {
            valueElement= parent.addElement( "value" );
            appendOptionalAttribute( valueElement, "key", key );
            valueElement.addText( plcValue.getObject().toString() );
        }
        else if ( plcValue.isList() )
        {
            valueElement= parent.addElement( "values" );
            appendOptionalAttribute( valueElement, "key", key );
            for ( PlcValue listItem : plcValue.getList() )
            {
                xmlSeralize( valueElement, listItem );
            }

        }
        else if ( plcValue.isStruct() )
        {
            valueElement= parent.addElement( "values" );
            appendOptionalAttribute( valueElement, "key", key );
            for ( Entry< String, ? extends PlcValue > structItem : plcValue.getStruct().entrySet() )
            {
                xmlSeralize( valueElement, structItem.getKey(), structItem.getValue() );
            }
        }
        else
        {
            valueElement= parent.addElement( "unkownValue" );
            appendOptionalAttribute( valueElement, "key", key );
        }
    }

    private static void appendOptionalAttribute( Element element, String name, String value )
    {
        if ( value != null )
        {
            element.addAttribute( name, value );
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
         * 
         * @param docIndicatesSucces the succes indicator of the response.
         * @param document           The serialized response document.
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
