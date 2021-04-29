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


import org.apache.plc4x.java.PlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.mock.connection.MockConnection;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.connection.PoolingConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class (as it's name implies) provides connection instances and the funcionality to disconnect and validate those
 * connections.
 * <p>
 * All connection related parameters (values required in order to create a connection) must be
 * declared in the connection providers.
 * <p>
 * This particular example is a {@link PoolingConnectionProvider} which declares that connections resolved by this provider
 * will be pooled and reused. There are other implementations like {@link CachedConnectionProvider} which lazily creates and
 * caches connections or simply {@link ConnectionProvider} if you want a new connection each time something requires one.
 */
@Alias("connection")
public class MulePlcConnectionProvider implements CachedConnectionProvider< MulePlcConnection >
{
    /**
     * The logger of the class.
     */
    private static final Logger LOGGER= LoggerFactory.getLogger( MulePlcConnectionProvider.class );

    /**
     * The connection uri of the plc.
     */
    @Parameter
    @Summary("The uri of the PLC to connect to.")
    private String connectionUri;

    private final static PlcDriverManager driverManager= new PlcDriverManager( MulePlcConnectionProvider.class.getClassLoader() );

    /**
     * Connect using the connction uri.
     */
    @Override
    public MulePlcConnection connect() throws ConnectionException
    {
        LOGGER.info( "start connect: " + connectionUri );
        MulePlcConnection connection= null;
        try ( PlcConnection plcConnnection= driverManager.getConnection( connectionUri ) )
        {
            //if ( driverManager.getDriver( connectionUri ).getProtocolCode().equals( "mock" )  )
            if ( plcConnnection instanceof MockConnection )
            {
                int prefixLength= 1 + driverManager.getDriver( connectionUri ).getProtocolCode().length();
                connection= new MockedMulePlcConnection( (MockConnection) plcConnnection, connectionUri.substring( prefixLength ) );
            }
            else
            {
                connection= new DefaultMulePlcConnection( plcConnnection );
            }
            connection.connect();
        }
        catch ( Exception e )
        {
            throw new ConnectionException( "Cannot connect to { " + connectionUri + " }", e );
        }
        LOGGER.info( "connected: " + connectionUri + ":" + connection );
        return connection;
    }

    @Override
    public void disconnect( MulePlcConnection connection )
    {
        LOGGER.info( "start disconnect { " + connectionUri + " }" );
        try
        {
            connection.close();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while disconnecting { " + connectionUri + " }", e );
        }
        LOGGER.info( "disconnected { " + connectionUri + " }" );
    }

    @Override
    public ConnectionValidationResult validate( MulePlcConnection connection )
    {
        LOGGER.info( "start validation { " + connectionUri + " }" );
        if ( connection == null )
        {
            LOGGER.warn( "invalid null connection { " + connectionUri + " }" );
            return ConnectionValidationResult.failure( "Not connected to { " + connectionUri + " }", new Exception( "nullconnection" ) );
        }
        else
        {
            try
            {
                @SuppressWarnings("unused")
                PlcConnection plcConnection= connection.getPlcConnection();
            }
            catch ( ConnectionException e1 )
            {
                LOGGER.info( "validation failed, no connection { " + connectionUri + " }" );
                return ConnectionValidationResult.failure( "Not connected to { " + connectionUri + " }", new Exception( "not connected: " + connection ) );
            }
            LOGGER.info( "validation success { " + connectionUri + " }" );
            return ConnectionValidationResult.success();
        }
    }
}
