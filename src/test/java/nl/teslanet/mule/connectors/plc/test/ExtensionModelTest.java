/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mule.runtime.module.extension.api.util.MuleExtensionUtils.loadExtension;

import javax.xml.transform.Source;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.IOUtils;
import org.mule.runtime.extension.api.resources.GeneratedResource;
import org.mule.runtime.module.extension.internal.resources.documentation.ExtensionDocumentationResourceGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import nl.teslanet.mule.connectors.plc.internal.MulePlcConnector;


/**
 * Test the Mule extension model.
 */
public class ExtensionModelTest
{

    private static final Logger logger= LoggerFactory.getLogger( ExtensionModelTest.class.getCanonicalName() );

    private static final String modelResourcePath= "META-INF/plc-extension-descriptions.xml";

    private static final String expectedResourcePath= "schemata/plc-extension-descriptions.xml";

    private ExtensionDocumentationResourceGenerator resourceFactory= new ExtensionDocumentationResourceGenerator();

    private ExtensionModel extensionModel;

    @BeforeEach
    public void before()
    {
        extensionModel= loadExtension( MulePlcConnector.class );
    }

    @Test
    public void generate() throws Exception
    {
        GeneratedResource resource= resourceFactory.generateResource( extensionModel ).get();
        assertEquals( modelResourcePath, resource.getPath() );
        byte[] expected= IOUtils.toByteArray( currentThread().getContextClassLoader().getResource( expectedResourcePath ).openStream() );
        String content= new String( resource.getContent() );
        Source expectedSource= Input.from( expected ).build();
        Source contentSource= Input.from( content ).build();
        Diff diff= DiffBuilder
            .compare( expectedSource )
            .withTest( contentSource )
            .checkForSimilar()
            //.checkForIdentical() 
            .ignoreComments()
            .ignoreWhitespace()
            .normalizeWhitespace()
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
            .ignoreElementContentWhitespace()
            .build();

        if ( diff.hasDifferences() )
        {
            logger.warn( "\n---\n{}\n---", content );
        }
        assertFalse( diff.hasDifferences(), diff.toString() );
    }
}
