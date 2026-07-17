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


/**
 * The response codes. The codes mirror PlcResponseCode
 */
public enum ResponseCode
{
    /**
     * Everything went ok.
     */
    OK,
    /**
     * The requested ressource could not be found on the target device.
     * (The address was syntactically correct, but the item it addressed simply didn't exist)
     */
    NOT_FOUND,
    /**
     * The remote device denied access to the requested resource.
     * (Possibly remote access is disabled, or an authentication is required)
     */
    ACCESS_DENIED,
    /**
     * The resource address was syntactically invalid.
     */
    INVALID_ADDRESS,
    /**
     * The requested datatype does not exist or was not compatible with the requested ressource.
     */
    INVALID_DATATYPE,
    INVALID_DATA,
    /**
     * Something went wrong internally in the driver logic.
     * (This is most probably a PLC4X bug)
     */
    INTERNAL_ERROR,
    /**
     * The remote device is currently unable to process the request due to overload.
     */
    REMOTE_BUSY,
    /**
     * Something went wrong on the remote side.
     */
    REMOTE_ERROR,
    /**
     * The requested resource uses a feature of the driver which has not been implemented.
     */
    UNSUPPORTED,
    /**
     * Indicates a response is pending.
     */
    RESPONSE_PENDING;
}
