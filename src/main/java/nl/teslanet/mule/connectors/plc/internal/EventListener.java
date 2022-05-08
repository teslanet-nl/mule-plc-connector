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
package nl.teslanet.mule.connectors.plc.internal;


import java.io.InputStream;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.api.EventHandler;
import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidHandlerNameException;
import nl.teslanet.mule.connectors.plc.internal.exception.StartException;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer.XmlSerializerResult;


/**
 * The EventListener message source receives PLC events.
 * The received PLC messages are delivered to the listeners mule-flow.
 */
@org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
public class EventListener extends Source< InputStream, ReceivedResponseAttributes >
{
    /**
     * The logger of this class.
     */
    private static final Logger logger= LoggerFactory.getLogger( EventListener.class );

    /**
     * The handler that will deliver the events produced by the PLC on this subscription.
     */
    @Parameter
    @Expression( ExpressionSupport.NOT_SUPPORTED )
    private EventHandler eventHandler;

    /**
     * Source callback to deliver messages to Mule.
     */
    private SourceCallback< InputStream, ReceivedResponseAttributes > sourceCallback= null;

    /**
    * Default constructor
    */
    public EventListener()
    {
        //NOOP
    }

    /**
     * Start the listener
     */
    @Override
    public void onStart( SourceCallback< InputStream, ReceivedResponseAttributes > sourceCallback ) throws MuleException
    {
        this.sourceCallback= sourceCallback;
        try
        {
            EventProcessor processor= MulePlcConnector.getEventHandlerRegistry().getEventProcessor( eventHandler.getHandlerName() );
            processor.addListener( this );
        }
        catch ( InternalInvalidHandlerNameException e )
        {
            throw new StartException( this + " listener has invalid handler { " + eventHandler.getHandlerName() + " }" );
        }
        logger.info( this + " started." );
    }

    /**
     * Stop the handler.
     */
    @Override
    public void onStop()
    {
        EventProcessor processor;
        try
        {
            processor= MulePlcConnector.getEventHandlerRegistry().getEventProcessor( eventHandler.getHandlerName() );
            processor.removeListener( this );
        }
        catch ( InternalInvalidHandlerNameException e )
        {
            logger.error( this + " cannot remove handler { " + eventHandler.getHandlerName() + " }" );
        }
        sourceCallback= null;
        logger.info( this + " stopped." );
    }

    /**
     * Accept serialized PLC event and hand over to the Mule flow.
     */
    public void accept( XmlSerializerResult serializedContent )
    {
        //hand over to Mule
        sourceCallback.handle( XmlSerializer.createMuleResult( serializedContent ) );
    }
}
