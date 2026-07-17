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


/**
 * Canonical write field response.
 * In fact an interface but Mule extension API needs concrete classes. 
 */
public class WriteFieldResponse
{
    /**
     * Get the alias of the field.
     * @return The alias of the field.
     */
    public String getAlias()
    {
        return null;
    }

    /**
     * Get the result of the field write operation.
     * @return The result of the field write.
     */
    public String getResult()
    {
        return null;
    }

    /**
     * Get the type of the field.
     * @return The field type.
     */
    public String getFieldType()
    {
        return null;
    }

    /**
     * Get the value of the write field.
     * @return The value of the field.
     */
    public ResponseValue getValue()
    {
        return null;
    }
}
