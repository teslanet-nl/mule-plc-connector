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
package nl.teslanet.mule.connectors.plc.test.utils;


import java.io.ByteArrayInputStream;
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

import org.apache.commons.io.IOUtils;
import org.mule.runtime.api.metadata.TypedValue;
//import org.mule.runtime.core.api.message.OutputHandler;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;


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
        return IOUtils.resourceToString( resourcePath, StandardCharsets.UTF_8, TestUtils.class.getClassLoader() );
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
        return new ByteArrayInputStream(IOUtils.resourceToByteArray( resourcePath, TestUtils.class.getClassLoader() ));
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
        else if ( object instanceof InputStream )
        {
            return IOUtils.toString( (InputStream) object, StandardCharsets.UTF_8 );
        }
        else if ( object instanceof byte[] )
        {
            return new String( (byte[]) object, StandardCharsets.UTF_8 );
        }
        else
        {
            return object.toString();
        }
    }

    /**
     * Determine the differnce between two xml strings.
     * @param xml1 The first xml string.
     * @param xml2 The second xml string.
     * @return The Difference.
     */
    public static Diff difference( String xml1, String xml2 )
    {
        return DiffBuilder.compare( xml1 ).withTest( xml2 ).ignoreComments().ignoreWhitespace().build();
    }

}
