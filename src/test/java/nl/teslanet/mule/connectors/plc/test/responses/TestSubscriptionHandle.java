/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2026 (teslanet.nl) Rogier Cobben
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
