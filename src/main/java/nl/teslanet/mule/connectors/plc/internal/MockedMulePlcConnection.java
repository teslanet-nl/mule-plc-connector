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


import org.apache.plc4x.java.mock.connection.MockConnection;
import org.mule.runtime.api.connection.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The plcConnection instance
 *
 */
public class MockedMulePlcConnection extends DefaultMulePlcConnection
{
    /**
     * The logger of the class.
     */
    private static final Logger LOGGER= LoggerFactory.getLogger( MockedMulePlcConnection.class );

    /**
     * The logger of the class.
     */

    private final String deviceName;

    /**
     * @param mockConnection The mocked PLC Connection. 
     * @throws ConnectionException 
     */
    public MockedMulePlcConnection( MockConnection mockConnection, String deviceName ) throws ConnectionException
    {
        super( mockConnection );
        this.deviceName= deviceName;
        ( (MockConnection) plcConnection ).setDevice( MulePlcConnector.getMockDevice( deviceName ) );
        LOGGER.info( "mock added to mock connection { " + this + " }" );
    }

    /**
     * Assures that the PLC connection is in connected state.
     * @throws ConnectionException 
     */
    @Override
    public synchronized void connect() throws ConnectionException
    {
        if ( !plcConnection.isConnected() )
        {
            ( (MockConnection) plcConnection ).setDevice( MulePlcConnector.getMockDevice( deviceName ) );
            LOGGER.info( "(re)connected mock connection { " + this + " }" );
            if ( !plcConnection.isConnected() )
            {
                throw new ConnectionException( "Error on mock connection { " + this + " }" );
            }
        }
    }

    /**
     * Close the connection.
     * @throws Exception 
     */
    @Override
    public synchronized void close() throws Exception
    {
        if ( plcConnection.isConnected() )
        {
            ( (MockConnection) plcConnection ).setDevice( null );
        }

    }
}
