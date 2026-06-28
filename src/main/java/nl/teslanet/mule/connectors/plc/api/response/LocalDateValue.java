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


import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * LocalDate response value.
 */
public record LocalDateValue( LocalDate value ) implements Valuable
{
    @Override
    public boolean isLong()
    {
        return true;
    }

    @Override
    public long getAsLong()
    {
        Instant instant= value.atStartOfDay( ZoneOffset.UTC ).toInstant();
        return( instant.getEpochSecond() );
    }

    @Override
    public boolean isBigInteger()
    {
        return true;
    }

    @Override
    public BigInteger getAsBigInteger()
    {
        return BigInteger.valueOf( getAsLong() ).multiply( BigInteger.valueOf( 1_000_000_000L ) );
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

    @Override
    public boolean isDate()
    {
        return true;
    }

    @Override
    public LocalDate getAsDate()
    {
        return value;
    }

    @Override
    public boolean isDateTime()
    {
        return true;
    }

    @Override
    public LocalDateTime getAsDateTime()
    {
        return value.atStartOfDay();
    }
}