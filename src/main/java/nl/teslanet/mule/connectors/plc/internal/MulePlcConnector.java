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


import java.util.concurrent.ConcurrentHashMap;

import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;

import nl.teslanet.mule.connectors.plc.internal.mock.PlcMockDevice;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml(prefix= "plc", namespace= "http://www.teslanet.nl/schema/mule/plc")
@Extension(name= "PLC", vendor= "Teslanet.nl")
@Configurations({ MulePlcConfig.class, MulePlcMock.class })
public class MulePlcConnector
{
    /**
     * The mockdevices that are configured.
     */
    private static final ConcurrentHashMap< String, PlcMockDevice > mockDevices= new ConcurrentHashMap<>();

    /**
     * @param deviceName The name of the mocked PLC.
     * @return The PLC mock that is found, otherwise null.
     */
    public static PlcMockDevice getMockDevice( String deviceName )
    {
        return mockDevices.get( deviceName );
    }

    /**
     * @param deviceName The name of the mocked PLC.
     * @return The PLC mock that is added.
     */
    public static PlcMockDevice addMockDevice( String deviceName )
    {
        return mockDevices.computeIfAbsent( deviceName, newDevice -> new PlcMockDevice() );
    }

    /**
     * Remove a PLC mock.
     * @param deviceName  The name of the mocked PLC.
     */
    public static void removeMockDevice( String deviceName )
    {
        mockDevices.remove( deviceName );
    }
}
