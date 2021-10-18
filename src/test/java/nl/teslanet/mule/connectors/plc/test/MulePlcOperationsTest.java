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


import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.plc4x.java.api.exceptions.PlcConnectionException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mule.runtime.core.api.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;


public class MulePlcOperationsTest extends AbstractPlcTestCase
{
    @SuppressWarnings( "unused" )
    private final Logger logger= LoggerFactory.getLogger( MulePlcOperationsTest.class );

    /**
     * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
     * @throws PlcConnectionException when connection failed
     */
    @Override
    protected String getConfigFile()
    {
        return "testapps/basic.xml";
    }

    @Test
    public void executePingOperation() throws Exception
    {
        Boolean payloadValue= (Boolean) flowRunner( "basic-ping" ).run().getMessage().getPayload().getValue();
        assertNotNull( "ping returned no response", payloadValue );
        //simulated does not support ping
        assertFalse( "ping should not be supported", payloadValue );
    }

    @Test
    public void executeReadOperation() throws Exception
    {

        String payloadValue= TestUtils.toString( flowRunner( "basic-read" ).run().getMessage().getPayload().getValue() );
        assertNotNull( payloadValue );
        Diff diff= DiffBuilder.compare( readResourceAsString( "testpayloads/read_response_1.xml" ) ).withTest( payloadValue ).ignoreComments().ignoreWhitespace().build();
        for ( Difference difference : diff.getDifferences() )
        {
            assertThat(
                difference.toString(),
                difference.getComparison().getControlDetails().getXPath(),
                Matchers.either( Matchers.is( "/plcReadResponse[1]/field[1]/value[1]/text()[1]" ) ).or( Matchers.is( "/plcReadResponse[1]/field[2]/value[1]/text()[1]" ) )
            );
        }

    }

    @Test
    public void executeWriteOperation() throws Exception
    {
        String payloadValue= TestUtils.toString( flowRunner( "basic-write" ).run().getMessage().getPayload().getValue() );
        Diff diff= DiffBuilder.compare( readResourceAsString( "testpayloads/write_response_1.xml" ) ).withTest( payloadValue ).ignoreComments().ignoreWhitespace().build();
        assertFalse( diff.toString(), diff.hasDifferences() );
    }

    @Test
    public void executeWriteAndReadOperation() throws Exception
    {
        String payloadWriteValue= TestUtils.toString( flowRunner( "basic-writestate" ).run().getMessage().getPayload().getValue() );
        Diff writeDiff= DiffBuilder.compare( readResourceAsString( "testpayloads/writestate_response_1.xml" ) ).withTest(
            payloadWriteValue
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( writeDiff.toString(), writeDiff.hasDifferences() );

        String payloadReadValue= (String) flowRunner( "basic-readstate" ).run().getMessage().getPayload().getValue();
        assertNotNull( payloadReadValue );
        Diff readDiff= DiffBuilder.compare( readResourceAsString( "testpayloads/readstate_response_1.xml" ) ).withTest(
            payloadReadValue
        ).ignoreComments().ignoreWhitespace().build();
        assertFalse( readDiff.toString(), readDiff.hasDifferences() );
    }

    /**
     * Read resource as string.
     *
     * @param resourcePath the resource path
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private String readResourceAsString( String resourcePath ) throws IOException
    {
        return IOUtils.getResourceAsString( resourcePath, this.getClass() );
    }
}
