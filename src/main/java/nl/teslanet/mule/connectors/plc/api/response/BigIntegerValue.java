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
 * Big integer response value.
 */
public record BigIntegerValue( BigInteger value ) implements Valuable
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
        return !value.equals( BigInteger.ZERO );
    }

    @Override
    public boolean isByte()
    {
        return ( value.compareTo( BigInteger.valueOf( Byte.MAX_VALUE ) ) <= 0 ) && ( value.compareTo( BigInteger.valueOf( Byte.MIN_VALUE ) ) >= 0 );
    }

    @Override
    public byte getAsByte()
    {
        if ( !isByte() ) return Valuable.super.getAsByte();
        return value.byteValue();
    }

    @Override
    public boolean isShort()
    {
        return ( value.compareTo( BigInteger.valueOf( Short.MAX_VALUE ) ) <= 0 ) && ( value.compareTo( BigInteger.valueOf( Short.MIN_VALUE ) ) >= 0 );
    }

    @Override
    public short getAsShort()
    {
        if ( !isShort() ) return Valuable.super.getAsShort();
        return value.shortValue();
    }

    @Override
    public boolean isInteger()
    {
        return ( value.compareTo( BigInteger.valueOf( Integer.MAX_VALUE ) ) <= 0 ) && ( value.compareTo( BigInteger.valueOf( Integer.MIN_VALUE ) ) >= 0 );
    }

    @Override
    public int getAsInteger()
    {
        if ( !isInteger() ) return Valuable.super.getAsInteger();
        return value.intValue();
    }

    @Override
    public boolean isLong()
    {
        return ( value.compareTo( BigInteger.valueOf( Long.MAX_VALUE ) ) <= 0 ) && ( value.compareTo( BigInteger.valueOf( Long.MIN_VALUE ) ) >= 0 );
    }

    @Override
    public long getAsLong()
    {
        if ( !isInteger() ) return Valuable.super.getAsInteger();
        return value.longValue();
    }

    @Override
    public boolean isBigInteger()
    {
        return true;
    }

    @Override
    public BigInteger getAsBigInteger()
    {
        return value;
    }

    @Override
    public boolean isBigDecimal()
    {
        return true;
    }

    @Override
    public BigDecimal getAsBigDecimal()
    {
        return new BigDecimal( value );
    }

    @Override
    public boolean isString()
    {
        return true;
    }

    @Override
    public String getAsString()
    {
        return value.toString();
    }
}