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
