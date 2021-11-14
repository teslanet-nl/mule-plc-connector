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
package nl.teslanet.mule.connectors.plc.test;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.core.api.message.OutputHandler;
import org.mule.runtime.core.api.util.IOUtils;


/**
 * Utilities for testing
 *
 */
public class TestUtils
{

    /**
     * No instances needed.
     */
    private TestUtils()
    {
        // NOOP
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
            return IOUtils.toString( (InputStream) object, StandardCharsets.UTF_8  );
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
