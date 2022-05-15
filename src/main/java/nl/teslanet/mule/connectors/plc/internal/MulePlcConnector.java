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


import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;

import nl.teslanet.mule.connectors.plc.internal.error.Errors;


/**
 * This is the main class of an extension, is the entry point from which configurations, connection providers, operations
 * and sources are going to be declared.
 */
@Xml( prefix= "plc", namespace= "http://www.teslanet.nl/schema/mule/plc" )
@Extension( name= "PLC", vendor= "Teslanet.nl" )
@Configurations( { MulePlcConfig.class } )
@Sources( { EventListener.class })
@ErrorTypes( Errors.class )
public class MulePlcConnector
{
    /**
     * The registry of event handlers.
     */
    private static EventHandlerRegistry eventHandlerRegistry= new EventHandlerRegistry();

    /**
     * @return the eventHandlerRegistry
     */
    public static EventHandlerRegistry getEventHandlerRegistry()
    {
        return eventHandlerRegistry;
    }
}
