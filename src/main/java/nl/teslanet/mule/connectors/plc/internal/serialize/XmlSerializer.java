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


import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.plc4x.java.api.messages.PlcFieldResponse;
import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.apache.plc4x.java.api.model.PlcField;
import org.apache.plc4x.java.api.types.PlcResponseCode;
import org.apache.plc4x.java.api.value.PlcValue;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import nl.teslanet.mule.connectors.plc.api.ResponseCodeValueProvider;


public class XmlSerializer
{
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
        boolean allOk= seralizeFileds( doc, rootElement, response, alias -> response.getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    public static XmlSerializerResult xmlSerialize( PlcWriteResponse response ) throws ParserConfigurationException
    {
        DocumentBuilder dBuilder= dbFactory.newDocumentBuilder();
        Document doc= dBuilder.newDocument();

        // root element
        Element rootElement= doc.createElement( "plcWriteResponse" );
        doc.appendChild( rootElement );
        //build content
        boolean allOk= seralizeFileds( doc, rootElement, response, alias -> response.getRequest().getPlcValue( alias ) );
        return new XmlSerializerResult( allOk, doc );
    }

    private static boolean seralizeFileds( Document doc, Element parent, PlcFieldResponse response, PlcValueProvider valueProvider )
    {
        boolean allOk= true;

        for ( String alias : response.getFieldNames() )
        {
            Element fieldElement= doc.createElement( "field" );
            fieldElement.setAttribute( "alias", alias );
            PlcResponseCode responseCode= response.getResponseCode( alias );
            allOk= allOk && ( responseCode == PlcResponseCode.OK );
            fieldElement.setAttribute( "responseCode", ResponseCodeValueProvider.getKey( responseCode ));
            PlcField field= response.getField( alias );
            try
            {
                fieldElement.setAttribute( "type", field.getPlcDataType() );
            }
            catch ( Exception e )
            {
                //Ignore
            }
            fieldElement.setAttribute( "count", String.valueOf( field.getNumberOfElements() ) );
            fieldElement.appendChild( xmlSeralize( doc, valueProvider.getValue( alias ) ) );
            parent.appendChild( fieldElement );
        }
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
        private boolean docIndicatesSucces;

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
            this.docIndicatesSucces= docIndicatesSucces;
            this.document= document;
        }

        /**
         * @return the docIndicatesSucces
         */
        public boolean isDocIndicatesSucces()
        {
            return docIndicatesSucces;
        }

        /**
         * @param docIndicatesSucces the docIndicatesSucces to set
         */
        public void setDocIndicatesSucces( boolean docIndicatesSucces )
        {
            this.docIndicatesSucces= docIndicatesSucces;
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
