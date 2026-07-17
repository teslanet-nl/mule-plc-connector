/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.internal;


import javax.inject.Inject;

import org.apache.plc4x.java.DefaultPlcDriverManager;
import org.apache.plc4x.java.api.PlcConnection;
import org.apache.plc4x.java.api.PlcDriver;
import org.apache.plc4x.java.api.PlcDriverManager;
import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.lock.LockFactory;
import org.mule.sdk.api.annotation.Alias;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.ParameterGroup;
import org.mule.sdk.api.annotation.param.display.Summary;
import org.mule.sdk.api.connectivity.CachedConnectionProvider;
import org.mule.sdk.api.connectivity.ConnectionValidationResult;
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
     * Mule's lockfactory 
     */
    @Inject
    private LockFactory lockFactory;

    /**
     * The connection string of the plc.
     */
    @Parameter
    @Summary( "The connection string of the PLC." )
    private String connectionString;

    /**
     * The concurrency parameters controlling the number of allowed concurrent IO on the connection.
     */
    @ParameterGroup( name= "Concurrency" )
    private ConcurrencyParams concurrencyParams;

    /**
     * The PLC driver manager.
     */
    private static final PlcDriverManager driverManager= new DefaultPlcDriverManager();

    /**
     * Connect to PLC using the connection string.
     */
    @Override
    public MulePlcConnection connect() throws ConnectionException
    {
        logger.info( "Start connect {}", connectionString );
        MulePlcConnection connection= null;
        try
        {
            PlcDriver driver= driverManager.getDriverForUrl( connectionString );
            PlcConnection plcConnnection= driver.getConnection( connectionString );
            connection= new DefaultMulePlcConnection( plcConnnection, lockFactory, concurrencyParams );
            connection.connect();
        }
        catch ( InternalConnectionException | PlcConnectionException e )
        {
            throw new ConnectionException( "Cannot connect { " + connection + "::" + connectionString + " }", e );
        }
        logger.info( "Connected { {}::{} }", connection, connectionString );
        return connection;
    }

    /**
     * Disconnect from PLC
     */
    @Override
    public void disconnect( MulePlcConnection connection )
    {
        logger.info( "Start disconnect { {}::{} }", connection, connectionString );
        connection.close();
        logger.info( "Disconnected { {}::{} }", connection, connectionString );
    }

    /**
     * Validate the connection.
     */
    @Override
    public ConnectionValidationResult validate( MulePlcConnection connection )
    {
        logger.info( "Start validation { {}::{} }", connection, connectionString );
        if ( connection == null || !connection.isConnected() )
        {
            logger.warn( "No connection { {}::{} }", connection, connectionString );
            return ConnectionValidationResult.failure( "Not connected { " + connection + "::" + connectionString + " }", new Exception( "no connection" ) );
        }
        else
        {
            logger.info( "Validation success { {}::{} }", connection, connectionString );
            return ConnectionValidationResult.success();
        }
    }
}
