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
import org.mule.runtime.extension.api.annotation.param.display.Summary;


/**
 * Concurrency parameters describes the allowed concurrent operations on a connection.
 *
 */
public class ConcurrencyParams
{
    /**
     * The number of concurrent IO per connection.
     * The total number of concurrent read, write and subscribe operations is limited to this number.
     * When negative the number is unlimited.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary(
        "The number of concurrent IO per connection. \nThe total number of concurrent read, write and subscribe operations is limited to this number.\nWhen negative the number is unlimited."
    )
    private int concurrentIo;

    /**
     * Number of concurrent reads per connection.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( "The number of concurrent reads per connection. \nWhen negative the number is unlimited." )
    private int concurrentReads;

    /**
     * Number of concurrent writes per connection.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( "The number of concurrent writes per connection. \nWhen negative the number is unlimited." )
    private int concurrentWrites;

    /**
     * Number of concurrent subscribes per connection.
     */
    @Parameter
    @Optional( defaultValue= "-1" )
    @Summary( "The number of concurrent subscribes per connection. \nWhen negative the number is unlimited." )
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
