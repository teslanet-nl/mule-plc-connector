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
 * Long response value.
 */
public record LongValue( long value ) implements Valuable
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
        return value != 0;
    }

    @Override
    public boolean isByte()
    {
        return ( value <= Byte.MAX_VALUE ) && ( value >= Byte.MIN_VALUE );
    }

    @Override
    public byte getAsByte()
    {
        if ( !isByte() ) return Valuable.super.getAsByte();
        return (byte) value;
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
        return true;
    }

    @Override
    public float getAsFloat()
    {
        return (float) value;
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
