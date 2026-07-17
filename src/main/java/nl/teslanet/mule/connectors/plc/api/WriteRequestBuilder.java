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
package nl.teslanet.mule.connectors.plc.api;


import java.util.List;

import org.mule.sdk.api.meta.ExpressionSupport;
import org.mule.sdk.api.annotation.Expression;
import org.mule.sdk.api.annotation.param.NullSafe;
import org.mule.sdk.api.annotation.param.Optional;
import org.mule.sdk.api.annotation.param.Parameter;
import org.mule.sdk.api.annotation.param.display.Summary;


/**
 * The builder for a write request.
 *
 */
public class WriteRequestBuilder extends IoRequestBuilder
{
    /**
     * The alias and plc-name of the items to request.
     */
    @Parameter
    @Optional
    @NullSafe
    @Expression(ExpressionSupport.SUPPORTED)
    @Summary("The fields to write to.")
    private List< WriteField > writeFields;

    /**
     * @return the writeFields
     */
    public List< WriteField > getWriteFields()
    {
        return writeFields;
    }

    /**
     * @param writeFields the writeFields to set
     */
    public void setWriteFields( List< WriteField > writeFields )
    {
        this.writeFields= writeFields;
    }
}
