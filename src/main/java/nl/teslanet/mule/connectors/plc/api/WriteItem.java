package nl.teslanet.mule.connectors.plc.api;


import java.util.List;

import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;


/**
 * Definition of a PLC item to write.
 *
 */
public class WriteItem
{
    /**
     * The PLC address to be written.
     */
    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("The alias of the field to write, for reference in the response. ")
    private String alias;

    /**
     * The PLC address to be written.
     */
    @Parameter
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("The address of the field to write.")
    private String address;

    /**
     * The values to write.
     */
    @Parameter
    @Optional
    @NullSafe
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("The values to write.")
    private List< Object > values;

    /**
     * @return the alias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * @return the address
     */
    public String getAddress()
    {
        return address;
    }

    /**
     * @return the values
     */
    public List< Object > getValues()
    {
        return values;
    }
}
