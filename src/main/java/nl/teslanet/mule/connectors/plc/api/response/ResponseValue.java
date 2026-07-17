/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2025 - 2026 (teslanet.nl) Rogier Cobben
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


import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.mule.sdk.api.annotation.param.display.Summary;


/**
 * Canonical response value.
 * In fact an interface but Mule extension API needs concrete classes. 
 */
public record ResponseValue( Valuable value ) implements Serializable
{
    /**
     * Is response type Simple.
     * @return True if the response type is a simple value type.
     */
    @Summary(value= "Is response type Simple.")
    public boolean isSimple()
    {
        return value.isSimple();
    }

    public boolean isEmpty()
    {
        return value.isEmpty();
    }

    public boolean isBoolean()
    {
        return value.isBoolean();
    }

    public boolean getAsBoolean()
    {
        return value.getAsBoolean();
    }

    public boolean isByte()
    {
        return value.isByte();
    }

    public byte getAsByte()
    {
        return value.getAsByte();
    }

    public boolean isShort()
    {
        return value.isShort();
    }

    public short getAsShort()
    {
        return value.getAsShort();
    }

    public boolean isInteger()
    {
        return value.isInteger();
    }

    public int getAsInteger()
    {
        return value.getAsInteger();
    }

    public boolean isLong()
    {
        return value.isLong();
    }

    public long getAsLong()
    {
        return value.getAsLong();
    }

    public boolean isBigInteger()
    {
        return value.isBigInteger();
    }

    public BigInteger getAsBigInteger()
    {
        return value.getAsBigInteger();
    }

    public boolean isFloat()
    {
        return value.isFloat();
    }

    public float getAsFloat()
    {
        return value.getAsFloat();
    }

    public boolean isDouble()
    {
        return value.isDouble();
    }

    public double getAsDouble()
    {
        return value.getAsDouble();
    }

    public boolean isBigDecimal()
    {
        return value.isBigDecimal();
    }

    public BigDecimal getAsBigDecimal()
    {
        return value.getAsBigDecimal();
    }

    public boolean isString()
    {
        return value.isString();
    }

    public String getAsString()
    {
        return value.getAsString();
    }

    public boolean isDuration()
    {
        return value.isDuration();
    }

    public Duration getAsDuration()
    {
        return value.getAsDuration();
    }

    public boolean isTime()
    {
        return value.isTime();
    }

    public LocalTime getAsTime()
    {
        return value.getAsTime();
    }

    public boolean isDate()
    {
        return value.isDate();
    }

    public LocalDate getAsDate()
    {
        return value.getAsDate();
    }

    public boolean isDateTime()
    {
        return value.isDateTime();
    }

    public LocalDateTime getAsDateTime()
    {
        return value.getAsDateTime();
    }

    public boolean isBinary()
    {
        return value.isBinary();
    }

    public InputStream getAsBinary()
    {
        return value.getAsBinary();
    }

    public boolean isList()
    {
        return value.isList();
    }

    public List< ResponseValue > getAsList()
    {
        return value.getAsList();
    }

    public boolean isStruct()
    {
        return value.isStruct();
    }

    public Map< String, ResponseValue > getAsStruct()
    {
        return value.getAsStruct();
    }
}
