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


import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;


/**
 * Concurrency parameters describes the allowed concurrent operations on a connection.
 *
 */
public class ConcurrencyParams
{
    /**
     * The number of concurrent IO per connection.
     * The total number of concurrent ping, read, write and (un)subscribe operations is limited to this number.
     * Default is 1, resulting in serialization of all operations on a connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "1" )
    private int concurrentIo;

    /**
     * Number of concurrent ping operations per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    private int concurrentPings;

    /**
     * Number of concurrent reads per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    private int concurrentReads;

    /**
     * Number of concurrent writes per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    private int concurrentWrites;

    /**
     * Number of concurrent subscribe and unsubscribe operations per connection.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
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
