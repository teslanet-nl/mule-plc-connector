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
