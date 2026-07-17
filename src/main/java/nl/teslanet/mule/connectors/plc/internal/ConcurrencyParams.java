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


import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.Summary;


/**
 * Concurrency parameters describes the allowed concurrent operations on a connection.
 *
 */
public class ConcurrencyParams
{
    /**
     * The maximum number of concurrent IO per connection.
     * The total number of concurrent ping, read, write and (un)subscribe operations is limited to this number.
     * Default is 1, resulting in serialization of all operations on a connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "1" )
    @Summary(
                    value= "The maximum number of concurrent IO per connection."
                        + "\nThe total number of concurrent ping, read, write and (un)subscribe operations is limited to this number."
                        + "\nDefault is 1, resulting in serialization of all operations on a connection." + "\nWhen negative the number is unlimited."
    )
    private int concurrentIo;

    /**
     * The maximum number of concurrent ping operations per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( value= "The maximum number of concurrent ping operations per connection." + "\nWhen negative the number is unlimited." )
    private int concurrentPings;

    /**
     * The maximum number of concurrent reads per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( value= "The maximum number of concurrent ping operations per connection." + "\nWhen negative the number is unlimited." )
    private int concurrentReads;

    /**
     * The maximum number of concurrent writes per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( value= "The maximum number of concurrent writes per connection." + "\nWhen negative the number is unlimited." )
    private int concurrentWrites;

    /**
     * The maximum number of concurrent subscribe and unsubscribe operations per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( value= "The maximum number of concurrent subscribe and unsubscribe operations per connection." + "\nWhen negative the number is unlimited." )
    private int concurrentSubscribes;

    /**
     * @return the concurrentIo
     */
    public int getConcurrentIo()
    {
        return concurrentIo;
    }

    /**
     * @param concurrentIo the concurrentIo to set
     */
    public void setConcurrentIo( int concurrentIo )
    {
        this.concurrentIo= concurrentIo;
    }

    /**
     * @return the concurrentPings
     */
    public int getConcurrentPings()
    {
        return concurrentPings;
    }

    /**
     * @param concurrentPings the concurrentPings to set
     */
    public void setConcurrentPings( int concurrentPings )
    {
        this.concurrentPings= concurrentPings;
    }

    /**
     * @return the concurrentReads
     */
    public int getConcurrentReads()
    {
        return concurrentReads;
    }

    /**
     * @param concurrentReads the concurrentReads to set
     */
    public void setConcurrentReads( int concurrentReads )
    {
        this.concurrentReads= concurrentReads;
    }

    /**
     * @return the concurrentWrites
     */
    public int getConcurrentWrites()
    {
        return concurrentWrites;
    }

    /**
     * @param concurrentWrites the concurrentWrites to set
     */
    public void setConcurrentWrites( int concurrentWrites )
    {
        this.concurrentWrites= concurrentWrites;
    }

    /**
     * @return the concurrentSubscribes
     */
    public int getConcurrentSubscribes()
    {
        return concurrentSubscribes;
    }

    /**
     * @param concurrentSubscribes the concurrentSubscribes to set
     */
    public void setConcurrentSubscribes( int concurrentSubscribes )
    {
        this.concurrentSubscribes= concurrentSubscribes;
    }
}
