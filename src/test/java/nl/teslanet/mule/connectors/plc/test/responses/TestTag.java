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


import org.apache.plc4x.java.api.model.PlcTag;
import org.apache.plc4x.java.api.types.PlcValueType;


/**
 * Tag for testing.
 */
public class TestTag implements PlcTag
{
    /**
     * Address of the tag.
     */
    private String address;

    private PlcValueType valueType;

    /**
     * Constructor.
     * @param adress The address.
     * @param valueType The type.
     */
    public TestTag( String adress, PlcValueType valueType )
    {
        this.address= adress;
        this.valueType= valueType;
    }

    @Override
    public String getAddressString()
    {
        return address;
    }

    @Override
    public PlcValueType getPlcValueType()
    {
        return valueType;

    }
}
