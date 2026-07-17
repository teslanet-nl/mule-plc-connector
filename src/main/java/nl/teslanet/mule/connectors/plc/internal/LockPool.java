/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 (teslanet.nl) Rogier Cobben
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


import java.util.ArrayDeque;
import java.util.concurrent.locks.Lock;

import org.mule.runtime.api.lock.LockFactory;


/**
 * A pool of locks that can be allocated to restrict the number of concurrent operations.
 *
 */
public class LockPool
{
    /**
     * The locks of this pool are stored in a deque to create rotating usage of locks.
     */
    private final ArrayDeque< Lock > locks;

    /**
     * Construct a lockpool.
     * @param lockFactory The lock factory used to create locks.
     * @param lockPrefix The lock id prefix that distinguishes locks of this pool.
     * @param poolSize The number of locks this pool has.
     */
    public LockPool( LockFactory lockFactory, String lockPrefix, int poolSize )
    {
        locks= new ArrayDeque<>();
        for ( int i= 0; i < poolSize; i++ )
        {
            locks.add( lockFactory.createLock( lockPrefix + i ) );
        }
    }

    /**
     * Allocate a lock. The lock is chosen using the round robin pointer.
     * The method blocks until 
     * @return the chosen lock in a locked state.
     */
    public synchronized Lock getLock()
    {
        Lock lock= locks.removeFirst();
        locks.addLast( lock );
        return lock;
    }
}
