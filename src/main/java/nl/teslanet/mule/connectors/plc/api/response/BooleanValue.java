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


import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Boolean response value.
 */
public record BooleanValue( boolean value ) implements Valuable
{

    @Override
    public boolean isSimple()
    {
        return true;
    }

    @Override
    public boolean isBoolean()
    {
        return true;
    }

    @Override
    public boolean getAsBoolean()
    {
        return value;
    }

    @Override
    public boolean isByte()
    {
        return true;
    }

    @Override
    public byte getAsByte()
    {
        return( value ? (byte) 1 : (byte) 0 );
    }

    @Override
    public boolean isShort()
    {
        return true;
    }

    @Override
    public short getAsShort()
    {
        return( value ? (short) 1 : (short) 0 );
    }

    @Override
    public boolean isInteger()
    {
        return true;
    }

    @Override
    public int getAsInteger()
    {
        return( value ? 1 : 0 );
    }

    @Override
    public boolean isLong()
    {
        return true;
    }

    @Override
    public long getAsLong()
    {
        return( value ? 1L : 0L );
    }

    @Override
    public boolean isBigInteger()
    {
        return true;
    }

    @Override
    public BigInteger getAsBigInteger()
    {
        return( value ? BigInteger.ONE : BigInteger.ZERO );
    }

    @Override
    public boolean isFloat()
    {
        return true;
    }

    @Override
    public float getAsFloat()
    {
        return( value ? 1.0f : 0.0f );
    }

    @Override
    public boolean isDouble()
    {
        return true;
    }

    @Override
    public double getAsDouble()
    {
        return( value ? 1.0 : 0.0 );
    }

    @Override
    public boolean isBigDecimal()
    {
        return true;
    }

    @Override
    public BigDecimal getAsBigDecimal()
    {
        return( value ? BigDecimal.ONE : BigDecimal.ZERO );
    }

    @Override
    public boolean isString()
    {
        return true;
    }

    @Override
    public String getAsString()
    {
        return Boolean.toString( value );
    }
}