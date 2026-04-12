/*-
 * #%L
 * Mule PLC Connector
 * %%
 * Copyright (C) 2025 - 2026 (teslanet.nl) Rogier Cobben
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
package nl.teslanet.mule.connectors.plc.api.config;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.mule.sdk.api.meta.ExpressionSupport;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.RefName;
import org.mule.sdk.api.annotation.param.display.Example;
import org.mule.sdk.api.annotation.param.display.Summary;


/**
 * Configuration of tags. 
 */
public class TagConfig
{
    /**
     * The local name of the tag.
     */
    @RefName
    private String alias;

    /**
     * The address of the tag as known to the PLC.
     */
    @Parameter
    @Expression( ExpressionSupport.NOT_SUPPORTED )
    @Summary( "The address of the tag as known to the PLC." )
    @Example( "holding-register:20:UINT[10]" )
    private String address;

    /**
     * @return The alias.
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * @param alias The alias to set.
     */
    public void setAlias( String alias )
    {
        this.alias= alias;
    }

    /**
     * @return the address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress( String address )
    {
        this.address= address;
    }

    /**
     * Default constructor.
     */
    public TagConfig()
    {
        super();
        this.alias= null;
        this.address= null;
    }

    /**
     * Constructor.
     * @param alias The alias of the tag.
     * @param address The alias of the tag.
     */
    public TagConfig( String alias, String address )
    {
        super();
        this.alias= alias;
        this.address= address;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == null )
        {
            return false;
        }
        if ( obj == this )
        {
            return true;
        }
        if ( obj.getClass() != getClass() )
        {
            return false;
        }
        TagConfig rhs= (TagConfig) obj;
        EqualsBuilder builder= new EqualsBuilder();
        builder.append( alias, rhs.alias );
        builder.append( address, rhs.address );
        return builder.isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder= new HashCodeBuilder( 9, 29 );
        builder.append( alias );
        builder.append( address );
        return builder.toHashCode();
    }
}
