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
 * Canonical response value.
 * In fact an interface but Mule extension API needs concrete classes. 
 */
public record LongValue( long value ) implements Valuable
{
    @Override
    public boolean isSimple()
    {
        return true;
    }

    @Override
    public boolean isNullable()
    {
        return false;
    }

    @Override
    public boolean isNull()
    {
        return false;
    }

    @Override
    public boolean isBoolean()
    {
        return true;
    }

    @Override
    public boolean getAsBoolean()
    {
        return value != 0;
    }

    @Override
    public boolean isByte()
    {
        return false;
    }

    @Override
    public byte getAsByte()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShort()
    {
        return ( value <= Short.MAX_VALUE ) && ( value >= Short.MIN_VALUE );
    }

    @Override
    public short getAsShort()
    {
        if ( !isShort() ) return Valuable.super.getAsShort();
        return (short) value;
    }

    @Override
    public boolean isInteger()
    {
        return ( value <= Integer.MAX_VALUE ) && ( value >= Integer.MIN_VALUE );
    }

    @Override
    public int getAsInteger()
    {
        if ( !isInteger() ) return Valuable.super.getAsInteger();
        return (int) value;
    }

    @Override
    public boolean isLong()
    {
        return true;
    }

    @Override
    public long getAsLong()
    {
        return value;
    }

    @Override
    public boolean isBigInteger()
    {
        return true;
    }

    @Override
    public BigInteger getAsBigInteger()
    {
        return BigInteger.valueOf( value );
    }

    @Override
    public boolean isFloat()
    {
        return false;
    }

    @Override
    public float getAsFloat()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDouble()
    {
        return true;
    }

    @Override
    public double getAsDouble()
    {
        return (double) value;
    }

    @Override
    public boolean isBigDecimal()
    {
        return false;
    }

    @Override
    public BigDecimal getAsBigDecimal()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isString()
    {
        return true;
    }

    @Override
    public String getAsString()
    {
        return Long.toString( value );
    }
}