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


import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertFalse;
import static org.mule.runtime.module.extension.api.util.MuleExtensionUtils.loadExtension;

import java.io.IOException;
import java.util.HashSet;

import javax.xml.transform.Source;

import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.dsl.DslResolvingContext;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.internal.dsl.DefaultDslResolvingContext;
import org.mule.runtime.module.extension.internal.capability.xml.schema.DefaultExtensionSchemaGenerator;
//import org.mule.runtime.module.extension.internal.capability.xml.schema.ClasspathBasedDslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import nl.teslanet.mule.connectors.plc.internal.MulePlcConnector;


public class ExtensionXsdTest
{
    private static final Logger logger= LoggerFactory.getLogger( ExtensionXsdTest.class.getCanonicalName() );

    private static final String SCHEMA_PATH= "schemata/mule-plc.xsd";

    private ExtensionModel extensionModel;

    private DslResolvingContext dslContext;

    @Before
    public void before()
    {
        extensionModel= loadExtension( MulePlcConnector.class );
        HashSet< ExtensionModel > models= new HashSet< ExtensionModel >();
        models.add( extensionModel );
        dslContext= new DefaultDslResolvingContext( models );
        //dslContext= new ClasspathBasedDslContext( ExtensionXsdTest.class.getClassLoader());
    }

    @Test
    public void xsdTest() throws IOException
    {
        DefaultExtensionSchemaGenerator generator= new DefaultExtensionSchemaGenerator();
        String schema= generator.generate( extensionModel, dslContext );

        String expected= IOUtils.toString( currentThread().getContextClassLoader().getResource( SCHEMA_PATH ).openStream() );

        Source expectedSource= Input.from( expected ).build();
        Source contentSource= Input.from( schema ).build();
        Diff diff= DiffBuilder.compare( expectedSource ).withTest( contentSource ).checkForSimilar()
                //.checkForIdentical() 
                .ignoreComments().ignoreWhitespace().normalizeWhitespace()
                //.withComparisonController(ComparisonController) 
                //.withComparisonFormatter(comparisonFormatter)
                //.withComparisonListeners(comparisonListeners) 
                //.withDifferenceEvaluator(differenceEvaluator) 
                //.withDifferenceListeners(comparisonListeners)
                //.withNodeMatcher(nodeMatcher) 
                //.withAttributeFilter(attributeFilter) 
                //.withNodeFilter(nodeFilter) 
                //.withNamespaceContext(map)
                //.withDocumentBuilerFactory(factory)
                .ignoreElementContentWhitespace().build();

        if ( diff.hasDifferences() )
        {
            logger.warn( "\n---\n" + schema + "\n---" );
        }
        assertFalse( diff.toString(), diff.hasDifferences() );

    }
}
