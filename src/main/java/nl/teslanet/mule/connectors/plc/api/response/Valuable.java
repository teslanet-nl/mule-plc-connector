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
 * Interface of response value types.
 */
public sealed interface Valuable extends Serializable
    permits
        BooleanValue,
        ByteValue,
        BinaryValue,
        ShortValue,
        IntegerValue,
        LongValue,
        BigIntegerValue,
        FloatValue,
        DoubleValue,
        CharacterValue,
        StringValue,
        LocalDateValue,
        LocalDateTimeValue,
        LocalTimeValue,
        DurationValue,
        EmptyValue,
        ListValue,
        StructValue
{
    public default boolean isSimple()
    {
        return false;
    }

    public default boolean isEmpty()
    {
        return false;
    }

    public default boolean isBoolean()
    {
        return false;
    }

    public default boolean getAsBoolean()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isByte()
    {
        return false;
    }

    public default byte getAsByte()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isShort()
    {
        return false;
    }

    public default short getAsShort()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isInteger()
    {
        return false;
    }

    public default int getAsInteger()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isLong()
    {
        return false;
    }

    public default long getAsLong()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isBigInteger()
    {
        return false;
    }

    public default BigInteger getAsBigInteger()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isFloat()
    {
        return false;
    }

    public default float getAsFloat()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isDouble()
    {
        return false;
    }

    public default double getAsDouble()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isBigDecimal()
    {
        return false;
    }

    public default BigDecimal getAsBigDecimal()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isString()
    {
        return false;
    }

    public default String getAsString()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isDuration()
    {
        return false;
    }

    public default Duration getAsDuration()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isTime()
    {
        return false;
    }

    public default LocalTime getAsTime()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isDate()
    {
        return false;
    }

    public default LocalDate getAsDate()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isDateTime()
    {
        return false;
    }

    public default LocalDateTime getAsDateTime()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isBinary()
    {
        return false;
    }

    public default InputStream getAsBinary()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isList()
    {
        return false;
    }

    public default List< ResponseValue > getAsList()
    {
        throw new UnsupportedOperationException();
    }

    public default boolean isStruct()
    {
        return false;
    }

    public default Map< String, ResponseValue > getAsStruct()
    {
        throw new UnsupportedOperationException();
    }

}
