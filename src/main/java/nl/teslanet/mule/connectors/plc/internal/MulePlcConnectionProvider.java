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


import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.internal.exception.InternalConnectionException;


/**
 * This class (as it's name implies) provides connection instances and the functionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) are
 * declared in the connection providers.
 * <p>
 * It implements {@link CachedConnectionProvider} which lazily creates and caches connections .
 */
@Alias( "connection" )
public class MulePlcConnectionProvider implements CachedConnectionProvider< MulePlcConnection >
{
    /**
     * The logger of the class.
     */
    private static final Logger logger= LoggerFactory.getLogger( MulePlcConnectionProvider.class );

    /**
     * The connection string of the plc.
     */
    @Parameter
    @Summary( "The connection string of the PLC." )
    private String connectionString;

    private static final PlcDriverManager driverManager= new PlcDriverManager( MulePlcConnectionProvider.class.getClassLoader() );

    /**
     * Connect to PLC using the connection string.
     */
    @Override
    public MulePlcConnection connect() throws ConnectionException
    {
        logger.info( "Start connect { " + connectionString + " }" );
        MulePlcConnection connection= null;
        try
        {
            PlcConnection plcConnnection= driverManager.getConnection( connectionString );
            connection= new DefaultMulePlcConnection( connectionString, plcConnnection );
            connection.connect();
        }
        catch ( InternalConnectionException | PlcConnectionException e )
        {
            throw new ConnectionException( "Cannot connect { " + connection + "::" + connectionString + " }", e );
        }
        logger.info( "Connected { " + connection + "::" + connectionString + " }" );
        return connection;
    }

    /**
     * Disconnect from PLC
     */
    @Override
    public void disconnect( MulePlcConnection connection )
    {
        logger.info( "Start disconnect { " + connection + "::" + connectionString + " }" );
        connection.close();
        logger.info( "Disconnected { " + connection + "::" + connectionString + " }" );
    }

    @Override
    public ConnectionValidationResult validate( MulePlcConnection connection )
    {
        logger.info( "Start validation { " + connection + "::" + connectionString + " }" );
        if ( connection == null || !connection.isConnected() )
        {
            logger.warn( "No connection { " + connection + "::" + connectionString + " }" );
            return ConnectionValidationResult.failure( "Not connected { " + connection + "::" + connectionString + " }", new Exception( "no connection" ) );
        }
        else
        {
            logger.info( "Validation success { " + connection + "::" + connectionString + " }" );
            return ConnectionValidationResult.success();
        }
    }
}
