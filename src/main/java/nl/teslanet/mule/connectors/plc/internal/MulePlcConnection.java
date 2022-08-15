/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2022 (teslanet.nl) Rogier Cobben
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
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcUnsubscriptionResponse;
import org.apache.plc4x.java.api.messages.PlcWriteResponse;

import nl.teslanet.mule.connectors.plc.api.ReadField;
import nl.teslanet.mule.connectors.plc.api.SubscribeField;
import nl.teslanet.mule.connectors.plc.api.UnsubscribeField;
import nl.teslanet.mule.connectors.plc.api.WriteField;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalConcurrencyException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalConnectionException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalUnsupportedException;


/**
 * The Mule PLC Connection interface
 *
 */
public interface MulePlcConnection
{
    /**
    * Close the connection.
    */
    public void close();

    /**
     * (re) Connect to the PLC.
     * @throws InternalConnectionException When connection could not be established.
     */
    public void connect() throws InternalConnectionException;

    /**
     * @return {@code true}connection is active
     */
    public boolean isConnected();

    /**
     * Ping the PLC using this connection.
     * @return {@code true} when the PLC is reachable, otherwise {@code false}
     * @throws InterruptedException When the operation was interrupted.
     * @throws InternalUnsupportedException When the operation is not supported by protocol used.
     */
    public Boolean ping() throws InterruptedException, InternalUnsupportedException;

    /**
     * @return {@code true} when the connection can be used to read, otherwise {@code false}.
     */
    public boolean canRead();

    /**
    * Read fields from PLC using this connection.
    * @param fields to read.
    * @param timeout Read response must be received within the timout.
    * @param timeUnit Unit of the timeout parameter.
    * @return The read response
    * @throws TimeoutException when timeOut occurs before the response is received.
    * @throws ExecutionException When the read could not be executed.
    * @throws InterruptedException When the read operation is interrupted.
    * @throws InternalConnectionException when connection failed.
     * @throws InternalConcurrencyException When a read operation is not allowed.
     */
    public PlcReadResponse read( List< ReadField > fields, long timeout, TimeUnit timeUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException;

    /**
     * @return {@code true} when the connection can be used to write, otherwise {@code false}.
     */
    public boolean canWrite();

    /**
    * Write fields from PLC using this connection.
    * @param fields to write.
    * @param timeout Write response must be received within the timeout.
    * @param timeoutUnit Unit of the timeout parameter.
    * @return The write response.
    * @throws TimeoutException when timeOut occurs before the response is received.
    * @throws ExecutionException When the write could not be executed.
    * @throws InterruptedException When the write operation is interrupted.
    * @throws InternalConnectionException when connection failed.
    * @throws InternalConcurrencyException  When the number of concurrent write operation is not allowed.
    */
    public PlcWriteResponse write( List< WriteField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException;

    /**
     * @return {@code true} when the connection can be used to subscribe, otherwise {@code false}.
     */
    boolean canSubscribe();

    /**
    * Subscribe to fields from PLC using this connection.
    * @param fields to subscribe to.
    * @param timeout Subscribe response must be received within the timeout.
    * @param timeoutUnit Unit of the timeout parameter.
    * @return The subscription response.
    * @throws TimeoutException when timeOut occurs before the response is received.
    * @throws ExecutionException When the subscribe request could not be executed.
    * @throws InterruptedException When the subscribe request is interrupted.
    * @throws InternalConnectionException when connection failed.
    * @throws InternalConcurrencyException When the number of concurrent subscribe operation is not allowed.
    */
    public PlcSubscriptionResponse subscribe( List< SubscribeField > fields, long timeout, TimeUnit timeOutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException;

    /**
    * Unsubscribe to fields from PLC using this connection.
    * @param fields to unsubscribe to.
    * @param timeout Subscribe response must be received within the timeout.
    * @param timeoutUnit Unit of the timeout parameter.
    * @return The unSubscribe response containing the results of the operation.
    * @throws TimeoutException when timeOut occurs before the response is received.
    * @throws ExecutionException When the subscribe request could not be executed.
    * @throws InterruptedException When the subscribe request is interrupted.
    * @throws InternalConnectionException when connection failed.
    * @throws InternalConcurrencyException When the number of concurrent subscribe operation is not allowed.
    */
    public PlcUnsubscriptionResponse unSubscribe( List< UnsubscribeField > fields, long timeout, TimeUnit timeoutUnit ) throws InterruptedException,
        ExecutionException,
        TimeoutException,
        InternalConnectionException,
        InternalConcurrencyException;

    /**
     * @return the connection string
     */
    public String getConnectionString();
}
