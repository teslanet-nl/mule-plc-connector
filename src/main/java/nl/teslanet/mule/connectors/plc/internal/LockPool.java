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


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;

import org.mule.runtime.api.lock.LockFactory;


/**
 * A pool of locks that can be allocated to restrict the number of concurrent operations.
 *
 */
public class LockPool
{
    /**
     * The locks of this pool are stored in a queue to create rotating usage of locks.
     */
    private final ArrayBlockingQueue< Lock > lockQueue;

    /**
     * Construct a lockpool.
     * @param lockFactory The lockafactory used to create locks.
     * @param lockPrefix The lock id prefix that distinguishes locks of this pool.
     * @param poolSize The number of locks this pool has.
     */
    public LockPool( LockFactory lockFactory, String lockPrefix, int poolSize )
    {
        lockQueue= new ArrayBlockingQueue< Lock >( poolSize );
        for ( int i= 0; i < poolSize; i++ )
        {
            lockQueue.add( lockFactory.createLock( lockPrefix + i ) );
        }
    }

    /**
     * Allocate a lock. A lock is chosen that is free (is not actually locked) or - when no lock s free - is chosen using the round robin pointer.
     * The method blocks until 
     * @return the chosen lock in a locked state.
     */
    public Lock getLock()
    {
        Lock lock= lockQueue.remove();
        lockQueue.add( lock );
        return lock;
    }
}
