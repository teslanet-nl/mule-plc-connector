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
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.inject.Inject;

import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.TypedValue;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.runtime.api.transformation.TransformationService;
import org.mule.runtime.core.api.message.OutputHandler;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.internal.util.ArrayUtils;


/**
 * Utilities for handling message content.
 *
 */
public class MessageUtils
{
    /**
     * Mule transformation service.
     */
    @Inject
    static private TransformationService transformationService;

    /**
     * Do not create objects.
     */
    private MessageUtils()
    {
        //NOOP
    }

    /**
     * Convert a typed value to byte array.
     * @param typedValueObject is the value to convert.
     * @return converted value as bytes
     * @throws IOException when the value is an outputhandler that cannot write.
     */
    public static byte[] toByteArray( TypedValue< Object > typedValueObject ) throws IOException
    {
        Object object= TypedValue.unwrap( typedValueObject );

        if ( object == null )
        {
            return null;
        }
        else if ( object instanceof String )
        {
            return ( (String) object ).getBytes( "iso-8859-1" );
        }
        if ( object instanceof CursorStreamProvider )
        {
            return IOUtils.toByteArray( (CursorStreamProvider) object );
        }
        else if ( object instanceof InputStream )
        {
            return IOUtils.toByteArray( (InputStream) object );
        }
        else if ( object instanceof byte[] )
        {
            return (byte[]) object;
        }
        else if ( object instanceof Byte[] )
        {
            return (byte[]) object;
        }
        else if ( object instanceof OutputHandler )
        {
            ByteArrayOutputStream output= new ByteArrayOutputStream();
            ( (OutputHandler) object ).write( null, output );
            return output.toByteArray();
        }
        else //do transform using Mule's transformers.
        {
            return (byte[]) transformationService.transform( Message.builder().payload( typedValueObject ).build(), DataType.BYTE_ARRAY ).getPayload().getValue();
        }
    }

    /**
     * Convert a typed value to String.
     * @param typedValueObject is the value to convert.
     * @return converted value as String
     * @throws IOException when the value is an outputhandler that cannot write.
     */
    public static String toString( TypedValue< Object > typedValueObject ) throws IOException
    {
        Object object= TypedValue.unwrap( typedValueObject );

        if ( object == null )
        {
            return null;
        }
        else if ( object instanceof String )
        {
            return (String) object;
        }
        if ( object instanceof CursorStreamProvider )
        {
            return new String( IOUtils.toByteArray( (CursorStreamProvider) object ), "iso-8859-1" );
        }
        else if ( object instanceof InputStream )
        {
            return new String( IOUtils.toByteArray( (InputStream) object ), "iso-8859-1" );
        }
        else if ( object instanceof byte[] )
        {
            return new String( (byte[]) object, "iso-8859-1" );
        }
        else if ( object instanceof Byte[] )
        {
            return new String( (byte[]) object, "iso-8859-1" );
        }
        else if ( object instanceof OutputHandler )
        {
            ByteArrayOutputStream output= new ByteArrayOutputStream();
            ( (OutputHandler) object ).write( null, output );
            return new String( output.toByteArray(), "iso-8859-1" );
        }
        else //do transform using Mule's transformers.
        {
            return (String) transformationService.transform( Message.builder().payload( typedValueObject ).build(), DataType.STRING ).getPayload().getValue();
        }
    }

    /**
     * Convert a typed value to {@code InputStream}.
     * @param typedValueObject is the value to convert.
     * @return converted value as {@code InputStream}.
     * @throws IOException when the value is an outputhandler that cannot write.
     */
    public static InputStream toInputStream( TypedValue< Object > typedValueObject ) throws IOException
    {
        Object object= TypedValue.unwrap( typedValueObject );

        if ( object == null )
        {
            return null;
        }
        else if ( object instanceof String )
        {
            return new ByteArrayInputStream( ( (String) object ).getBytes( "iso-8859-1" ) );
        }
        if ( object instanceof CursorStreamProvider )
        {
            return ( (CursorStreamProvider) object ).openCursor();
        }
        else if ( object instanceof InputStream )
        {
            return (InputStream) object;
        }
        else if ( object instanceof byte[] )
        {
            return new ByteArrayInputStream( (byte[]) object );
        }
        else if ( object instanceof Byte[] )
        {
            return new ByteArrayInputStream( ArrayUtils.toPrimitive( (Byte[]) object ) );
        }
        else if ( object instanceof OutputHandler )
        {
            PipedOutputStream output= new PipedOutputStream();
            ( (OutputHandler) object ).write( null, output );
            return new PipedInputStream( output );
        }
        else //do transform using Mule's transformers.
        {
            return (InputStream) transformationService.transform( Message.builder().payload( typedValueObject ).build(), DataType.INPUT_STREAM ).getPayload().getValue();
        }
    }
}
