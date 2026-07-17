/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
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
