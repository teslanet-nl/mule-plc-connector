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
package nl.teslanet.mule.connectors.plc.test;


import java.util.Optional;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;


@ArtifactClassLoaderRunnerConfig
(
    providedExclusions=
    {
        "org.mule.tests:*:*:*:*",
        "com.mulesoft.compatibility.tests:*:*:*:*"
    },
    applicationRuntimeLibs=
    {
        "org.apache.commons:commons-lang3",
        "com.github.jinahya:bit-io"
    }, 
    applicationSharedRuntimeLibs=
    {
        "org.apache.plc4x:plc4j-driver-mock",
        "org.apache.plc4x:plc4j-driver-simulated",
        "org.apache.plc4x:plc4j-api",
        "org.apache.plc4x:plc4j-spi",
    }, 
    exportPluginClasses=
    {
        //TODO move to api package
        nl.teslanet.mule.connectors.plc.internal.error.UnsupportedException.class,
    
        nl.teslanet.mule.connectors.plc.internal.MulePlcConnectionProvider.class
    },
    testExclusions= 
    {
        "org.mule.runtime:*:*:*:*",
        "org.mule.modules*:*:*:*:*",
        "org.mule.transports:*:*:*:*",
        "org.mule.mvel:*:*:*:*",
        "org.mule.extensions:*:*:*:*",
        "org.mule.connectors:*:*:*:*",
        "org.mule.tests.plugin:*:*:*:*",
        "com.mulesoft.mule.runtime*:*:*:*:*",
        "com.mulesoft.licm:*:*:*:*"
    },
    testInclusions=
    {
        "*:*:jar:tests:*",
        "*:*:test-jar:*:*"
    },
    testRunnerExportedRuntimeLibs=
    {
        "org.mule.tests:mule-tests-functional"
    }, 
    extraPrivilegedArtifacts=
    {
    }
)
public abstract class AbstractPlcTestCase extends MuleArtifactFunctionalTestCase
{
    private static TestClassLoaderRepository connectorClassLoaderRepository;

    protected Optional< ClassLoader > getConnectorClassLoader()
    {
        synchronized ( AbstractPlcTestCase.class )
        {
            if ( connectorClassLoaderRepository == null )
            {
                connectorClassLoaderRepository= new TestClassLoaderRepository();
            }
            return connectorClassLoaderRepository.find( "Region/plugin/nl.teslanet.mule.connectors.plc:mule-plc-connector:mule-extension:1.1.0-SNAPSHOT" );
            //return connectorClassLoaderRepository.find( "Region/plugin/nl.teslanet.mule.connectors.plc:mule-plc-connector:mule-extension" );
            //return connectorClassLoaderRepository.find( "Region/plugin/test-runner" );
            //return connectorClassLoaderRepository.find( "Region/plugin/org.mule.tests.plugin:mule-tests-component-plugin:jar:mule-plugin:4.1.1" );
        }
    }
}
