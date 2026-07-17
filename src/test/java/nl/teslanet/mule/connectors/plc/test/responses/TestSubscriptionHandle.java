/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.test.responses;


import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.model.PlcConsumerRegistration;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;


class TestSubscriptionHandle implements PlcSubscriptionHandle
{
    public class TestConsumerRegistration implements PlcConsumerRegistration
    {

        @Override
        public Integer getConsumerId()
        {
            return 1;
        }

        @Override
        public List< PlcSubscriptionHandle > getSubscriptionHandles()
        {
            return Collections.emptyList();
        }

        @Override
        public void unregister()
        {
       }
        
    }
    
    @Override
    public PlcConsumerRegistration register( Consumer< PlcSubscriptionEvent > consumer )
    {
        return new TestConsumerRegistration();
    }

}
