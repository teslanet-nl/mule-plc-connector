/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.internal;


import org.apache.plc4x.java.api.PlcConnection;
import org.mule.runtime.api.connection.ConnectionException;


/**
 * The Mule PLC Connection interface
 *
 */
public interface MulePlcConnection
{
    /**
     * @return the plcConnection
     * @throws ConnectionException 
     */
    public PlcConnection getPlcConnection() throws ConnectionException;

    /**
    * Close the connection.
    * @throws Exception 
    */
    public void close() throws Exception;

    /**
     * (re) Connect to the PLC.
     * @throws ConnectionException 
     */
    public void connect() throws ConnectionException;
}
