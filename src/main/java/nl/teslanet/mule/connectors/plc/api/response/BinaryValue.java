/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.api.response;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Hex;


/**
 * Binary value.
 */
public record BinaryValue( byte[] value ) implements Valuable
{

    @Override
    public boolean isSimple()
    {
        return true;
    }

    @Override
    public boolean isBinary()
    {
        return true;
    }

    @Override
    public InputStream getAsBinary()
    {
        return new ByteArrayInputStream( value );
    }

    @Override
    public boolean isString()
    {
        return true;
    }

    @Override
    public String getAsString()
    {
        return Hex.encodeHexString( value );
    }
}