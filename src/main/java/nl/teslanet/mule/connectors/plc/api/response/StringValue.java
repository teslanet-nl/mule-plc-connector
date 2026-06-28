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
 * String response value.
 */
public record StringValue( String value ) implements Valuable
{

    @Override
    public boolean isSimple()
    {
        return true;
    }

    @Override
    public boolean isBoolean()
    {
        try
        {
            Boolean.parseBoolean( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public boolean getAsBoolean()
    {
        if ( !isBoolean() ) Valuable.super.getAsBoolean();
        return Boolean.parseBoolean( value );
    }

    @Override
    public boolean isByte()
    {
        try
        {
            Byte.parseByte( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public byte getAsByte()
    {
        if ( !isByte() ) Valuable.super.getAsByte();
        return Byte.parseByte( value );
    }

    @Override
    public boolean isShort()
    {
        try
        {
            Short.parseShort( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public short getAsShort()
    {
        if ( !isShort() ) Valuable.super.getAsShort();
        return Short.parseShort( value );
    }

    @Override
    public boolean isInteger()
    {
        try
        {
            Integer.parseInt( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public int getAsInteger()
    {
        if ( !isInteger() ) Valuable.super.getAsInteger();
        return Integer.parseInt( value );
    }

    @Override
    public boolean isLong()
    {
        try
        {
            Long.parseLong( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public long getAsLong()
    {
        if ( !isLong() ) Valuable.super.getAsLong();
        return Long.parseLong( value );
    }

    @Override
    public boolean isBigInteger()
    {
        try
        {
            new BigInteger( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public BigInteger getAsBigInteger()
    {
        if ( !isBigInteger() ) Valuable.super.getAsBigInteger();
        return new BigInteger( value );
    }

    @Override
    public boolean isFloat()
    {
        try
        {
            Float.parseFloat( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public float getAsFloat()
    {
        if ( !isFloat() ) Valuable.super.getAsFloat();
        return Float.parseFloat( value );
    }

    @Override
    public boolean isDouble()
    {
        try
        {
            Double.parseDouble( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public double getAsDouble()
    {
        if ( !isDouble() ) Valuable.super.getAsDouble();
        return Double.parseDouble( value );
    }

    @Override
    public boolean isBigDecimal()
    {
        try
        {
            new BigDecimal( value );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    @Override
    public BigDecimal getAsBigDecimal()
    {
        if ( !isBigDecimal() ) Valuable.super.getAsBigDecimal();
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
        return value;
    }
}