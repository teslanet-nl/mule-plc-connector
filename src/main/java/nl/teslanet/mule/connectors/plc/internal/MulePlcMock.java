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


import java.util.List;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.api.MockedField;
import nl.teslanet.mule.connectors.plc.internal.mock.PlcMockDevice;


/**
 * This class represents an extension configuration, values set in this class are commonly used across multiple
 * operations since they represent something core from the extension.
 */
@Configuration(name= "mock")
public class MulePlcMock implements Startable, Stoppable
{
    private static final Logger LOGGER= LoggerFactory.getLogger( MulePlcMock.class );

    @RefName
    private String configName= null;

    /**
     * The alias and plc-name of the items to request.
     */
    @Parameter
    @Optional
    @NullSafe
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @Summary("The fields of the PLC mock.")
    @DisplayName("Requestitems")
    private List< MockedField > mockedFields;
    /**
     * @return The configuration name.
     */
    public String getConfigName()
    {
        return configName;
    }

    /**
     * Start the PLC mock
     */
    @Override
    public void start() throws MuleException
    {
        MulePlcConnector.addMockDevice( configName );
        PlcMockDevice device= MulePlcConnector.getMockDevice( configName );
        for ( MockedField field : mockedFields )
        {
            device.write( field.getAddress(), field.getInitialValues());
        }
        LOGGER.warn( "started mock { " + configName + " }" );
    }

    /**
     * Stop the PLC mock.
     */
    @Override
    public void stop() throws MuleException
    {
        MulePlcConnector.removeMockDevice( configName );
        LOGGER.warn( "stopped mock { " + configName + " }" );
    }
}
