/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2025 - 2026 (teslanet.nl) Rogier Cobben
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


/**
 * Canonical response value.
 * In fact an interface but Mule extension API needs concrete classes. 
 */
public record ResponseValue( Valuable value ) implements Serializable
{
    public boolean isSimple()
    {
        return value.isSimple();
    }

    public boolean isNullable()
    {
        return value.isNullable();
    }

    public boolean isNull()
    {
        return value.isNull();
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