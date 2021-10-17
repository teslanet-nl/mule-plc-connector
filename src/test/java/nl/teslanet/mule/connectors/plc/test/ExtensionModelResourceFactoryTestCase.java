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
package nl.teslanet.mule.connectors.plc.test;


import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mule.runtime.module.extension.api.util.MuleExtensionUtils.loadExtension;

import javax.xml.transform.Source;

import org.junit.Before;
import org.junit.Test;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.resources.GeneratedResource;
import org.mule.runtime.extension.api.resources.spi.GeneratedResourceFactory;
import org.mule.runtime.module.extension.internal.resources.AbstractGeneratedResourceFactoryTestCase;
import org.mule.runtime.module.extension.internal.resources.documentation.ExtensionDocumentationResourceGenerator;
import org.mule.tck.size.SmallTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import nl.teslanet.mule.connectors.plc.internal.MulePlcConnector;


@SmallTest
public class ExtensionModelResourceFactoryTestCase extends AbstractGeneratedResourceFactoryTestCase
{

    private static final Logger logger= LoggerFactory.getLogger( ExtensionModelResourceFactoryTestCase.class.getCanonicalName() );

    private static final String modelResourcePath= "plc-extension-descriptions.xml";

    private static final String expectedResourcePath= "schemata/plc-extension-descriptions.xml";

    private ExtensionDocumentationResourceGenerator resourceFactory= new ExtensionDocumentationResourceGenerator();

    private ExtensionModel extensionModel;

    @Before
    public void before()
    {
        extensionModel= loadExtension( MulePlcConnector.class );
    }

    @SuppressWarnings( "unchecked" )
    @Override
    protected Class< ? extends GeneratedResourceFactory >[] getResourceFactoryTypes()
    {
        return new Class []{ ExtensionDocumentationResourceGenerator.class };
    }

    @Test
    public void generate() throws Exception
    {
        GeneratedResource resource= resourceFactory.generateResource( extensionModel ).get();
        assertEquals( resource.getPath(), modelResourcePath );
        String expected= IOUtils.toString( currentThread().getContextClassLoader().getResource( expectedResourcePath ).openStream() );
        String content= new String( resource.getContent() );
        logger.info( "\n---\n" + content + "\n---" );
        Source expectedSource= Input.from( expected ).build();
        Source contentSource= Input.from( content ).build();
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

        assertFalse( diff.toString(), diff.hasDifferences() );

    }
}
