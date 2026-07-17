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


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.codec.binary.Hex;


/**
 * Binary response value.
 */
public record BinaryValue( byte[] value ) implements Valuable
{

    @Override
    public boolean isSimple()
    {
        return true;
    }

    @Override
    public boolean isBinary()
    {
        return true;
    }

    @Override
    public InputStream getAsBinary()
    {
        return new ByteArrayInputStream( value );
    }

    @Override
    public boolean isString()
    {
        return true;
    }

    @Override
    public String getAsString()
    {
        return Hex.encodeHexString( value );
    }
}
