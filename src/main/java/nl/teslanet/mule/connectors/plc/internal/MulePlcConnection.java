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


import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.plc4x.java.api.messages.PlcReadResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;
import org.mule.runtime.api.connection.ConnectionException;

import nl.teslanet.mule.connectors.plc.api.ReadField;
import nl.teslanet.mule.connectors.plc.api.WriteField;


/**
 * The Mule PLC Connection interface
 *
 */
public interface MulePlcConnection
{
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

    /**
     * @return {@code true}connection is active
     */
    public boolean isConnected();

    /**
     * Ping the PLC using this connection.
     * @return {@code true} when the PLC is reachable, otherwise {@code false}
     */
    public Boolean ping();

    /**
     * @return {@code true} when the connection can be used to read, otherwise {@code false}.
     */
    public boolean canRead();

    /**
     * Read fields from PLC using this connection.
     * @param fields to read.
     * @param timeout Read response must be received within the timout.
     * @param timeUnit Unit of the timeout parameter.
     * @return the read response
     * @throws TimeoutException when timeOut occurs before the response is received.
     * @throws ExecutionException When the read could not be executed.
     * @throws InterruptedException When the read operation is interrupted.
     * @throws ConnectionException when connection failed.
     */
    public PlcReadResponse read( List< ReadField > fields, long timeout, TimeUnit timeUnit ) throws InterruptedException, ExecutionException, TimeoutException, ConnectionException;

    /**
     * @return {@code true} when the connection can be used to write, otherwise {@code false}.
     */
    public boolean canWrite();

    /**
      * Read fields from PLC using this connection.
     * @param fields to write.
     * @param timeout Read response must be received within the timout.
     * @param timeoutUnit Unit of the timeout parameter.
     * @return the write response.
    * @throws TimeoutException when timeOut occurs before the response is received.
     * @throws ExecutionException When the write could not be executed.
     * @throws InterruptedException When the write operation is interrupted.
     * @throws ConnectionException when connection failed.
     */
    public PlcWriteResponse write( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException, ExecutionException, TimeoutException, ConnectionException;
}
