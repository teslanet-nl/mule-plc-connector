/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2021 - 2026 (teslanet.nl) Rogier Cobben
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
class ExtensionModelTest
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
    void generate() throws Exception
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
