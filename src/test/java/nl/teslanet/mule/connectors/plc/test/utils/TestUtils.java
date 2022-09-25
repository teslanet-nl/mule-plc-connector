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
package nl.teslanet.mule.connectors.plc.test.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.message.OutputHandler;
import org.mule.runtime.core.api.util.IOUtils;
import org.xml.sax.SAXException;


/**
 * Utilities for testing
 *
 */
public class TestUtils
{
    static final String JAXP_SCHEMA_LANGUAGE= "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    static final String W3C_XML_SCHEMA= "http://www.w3.org/2001/XMLSchema";

    static final DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();

    private static Schema schema;

    static
    {
        try
        {
            String xsd= readResourceAsString( "nl/teslanet/mule/connectors/plc/v1/plc.xsd" );
            SchemaFactory sf= SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
            schema= sf.newSchema( new StreamSource( new StringReader( xsd ) ) );
        }
        catch ( SAXException | IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    /**
     * No instances needed.
     */
    private TestUtils()
    {
        // NOOP
    }

    public static void validate( String xml ) throws SAXException, IOException
    {
        Validator validator= schema.newValidator();
        validator.validate( new StreamSource( new StringReader( xml ) ) );
    }

    /**
     * Read resource as string.
     *
     * @param resourcePath the resource path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String readResourceAsString( String resourcePath ) throws IOException
    {
        return IOUtils.getResourceAsString( resourcePath, TestUtils.class );
    }

    /**
     * Read resource as string.
     *
     * @param resourcePath the resource path
     * @return the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static InputStream readResourceAsInputStream( String resourcePath ) throws IOException
    {
        return IOUtils.getResourceAsStream( resourcePath, TestUtils.class );
    }

    /**
     * Convert some payload to string.
     *
     * @param payload the payload to convert.
     * @return the string represenation.
     * @throws IOException when conversion failed
     */
    public static String toString( Object payload ) throws IOException
    {
        Object object;

        if ( payload == null )
        {
            return new String();
        }
        if ( payload instanceof TypedValue )
        {
            object= TypedValue.unwrap( payload );
        }
        else
        {
            object= payload;
        }
        // transform object
        if ( object instanceof String )
        {
            return (String) object;
        }
        if ( object instanceof CursorStreamProvider )
        {
            return IOUtils.toString( (CursorStreamProvider) object );
        }
        else if ( object instanceof InputStream )
        {
            return IOUtils.toString( (InputStream) object, StandardCharsets.UTF_8 );
        }
        else if ( object instanceof byte[] )
        {
            return new String( (byte[]) object, StandardCharsets.UTF_8 );
        }
        else if ( object instanceof OutputHandler )
        {
            ByteArrayOutputStream output= new ByteArrayOutputStream();
            ( (OutputHandler) object ).write( null, output );
            return output.toString();
        }
        else
        {
            return object.toString();
        }
    }
}
