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
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The plcConnection instance
 *
 */
public class DefaultMulePlcConnection implements MulePlcConnection
{
    /**
     * The logger of the class.
     */
    private static final Logger LOGGER= LoggerFactory.getLogger( DefaultMulePlcConnection.class );

    /**
     * The underlying PLC plcConnection.
     */
    protected final PlcConnection plcConnection;

    /**
     * @param plcConnection The PLC Connection. 
     * @throws ConnectionException 
     */
    public DefaultMulePlcConnection( PlcConnection plcConnection ) throws ConnectionException
    {
        this.plcConnection= plcConnection;
        LOGGER.info( "connection created { " + this + " }" );
    }

    /**
     * @return the plcConnection
     * @throws ConnectionException 
     */
    @Override
    public PlcConnection getPlcConnection() throws ConnectionException
    {
        connect();
        return plcConnection;
    }

    /**
     * Close the connection.
     * @throws Exception 
     */
    @Override
    public void close() throws Exception
    {
        if ( plcConnection.isConnected() ) plcConnection.close();
    }

    @Override
    public void connect() throws ConnectionException
    {
        if ( !plcConnection.isConnected() )
        {
            try
            {
                plcConnection.connect();
                LOGGER.info( "(re)connected connection { " + this + " }" );
            }
            catch ( PlcConnectionException e )
            {
                LOGGER.error( "failed reconnecting { " + this + " }" );
            }
            if ( !plcConnection.isConnected() )
            {
                throw new ConnectionException( "Error on connection { " + this + " }" );
            }
        }
    }
}
