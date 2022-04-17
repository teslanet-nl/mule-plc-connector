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
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.plc4x.java.api.messages.PlcSubscriptionEvent;
import org.apache.plc4x.java.api.messages.PlcSubscriptionResponse;
import org.apache.plc4x.java.api.model.PlcSubscriptionHandle;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.teslanet.mule.connectors.plc.api.ReceivedResponseAttributes;
import nl.teslanet.mule.connectors.plc.api.Subscription;
import nl.teslanet.mule.connectors.plc.api.UnSubscription;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidHandlerNameException;
import nl.teslanet.mule.connectors.plc.internal.exception.InternalInvalidSubscriptionException;
import nl.teslanet.mule.connectors.plc.internal.exception.StartException;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer;
import nl.teslanet.mule.connectors.plc.internal.serialize.XmlSerializer.XmlSerializerResult;


/**
 * The EventHandler message source receives PLC events.
 * The received PLC messages are delivered to the handlers mule-flow.
 */
@org.mule.runtime.extension.api.annotation.param.MediaType( value= org.mule.runtime.extension.api.annotation.param.MediaType.APPLICATION_XML, strict= true )
public class EventHandler extends Source< InputStream, ReceivedResponseAttributes > implements Consumer< PlcSubscriptionEvent >
{
    /**
     * The logger of this class.
     */
    private static final Logger logger= LoggerFactory.getLogger( EventHandler.class.getCanonicalName() );

    /**
     * The config that owns the handler.
     */
    @Config
    private MulePlcConfig config;

    /**
     * The name of the handler by which it is referenced by observers and async requestst.
     */
    @Parameter
    @Expression( ExpressionSupport.NOT_SUPPORTED )
    private String handlerName;

    /**
     * Source callback to deliver messages to Mule.
     */
    private SourceCallback< InputStream, ReceivedResponseAttributes > sourceCallback= null;

    /**
     * Subscriptions that are assigned to this handler.
     */
    private ConcurrentHashMap< String, PlcSubscriptionResponse > subscriptions= new ConcurrentHashMap<>();

    /**
    * Default constructor
    * Creates and configures transformerfactory instance.
    */
    public EventHandler()
    {
        //NOOP
    }

    /**
     * Start the handler
     */
    @Override
    public void onStart( SourceCallback< InputStream, ReceivedResponseAttributes > sourceCallback ) throws MuleException
    {
        try
        {
            config.addHandler( handlerName, this );
        }
        catch ( InternalInvalidHandlerNameException e )
        {
            throw new StartException( this + " failed to start, invalid handler name." );
        }
        this.sourceCallback= sourceCallback;
        logger.info( this + " started." );
    }

    /**
     * Stop the handler.
     */
    @Override
    public void onStop()
    {
        //TODO unregister
        subscriptions.clear();
        config.removeHandler( handlerName );
        
        sourceCallback= null;
        logger.info( this + " stopped." );

    }

    /**
     * Get String representation.
     */
    @Override
    public String toString()
    {
        return "PLC ResponseHandler { " + config.getConfigName() + "::" + handlerName + " }";
    }

    /**
     * Add an subscription to the handler so that events can get handled..
     * @param subscription The subscription.
     * @param response The subscriptionResponse containing handles.
     * @throws InternalInvalidSubscriptionException The subscription is not valid.
     */
    synchronized void addSubscription( Subscription subscription, PlcSubscriptionResponse response ) throws InternalInvalidSubscriptionException
    {
        if ( subscription == null || subscription.getSubscriptionName().isEmpty() )
        {
            throw new InternalInvalidSubscriptionException( "Empty subscription name is invalid: { " + subscription.getSubscriptionName() + " }" );
        }
        if ( subscriptions.get( subscription.getSubscriptionName() ) != null )
        {
            throw new InternalInvalidSubscriptionException( "Subscription already exists: { " + subscription.getSubscriptionName() + " }" );
        }
        subscriptions.put( subscription.getSubscriptionName(), response );
        //TODO atomic flag
        if ( sourceCallback != null ) register( subscription.getSubscriptionName() );
    }
    
    /**
     * Remove the subscription.
     * @param unSubscription The unsubscribe request.
     * @throws InternalInvalidSubscriptionException
     */
    public void removeSubscription( UnSubscription unSubscription ) throws InternalInvalidSubscriptionException
    {
        if ( unSubscription == null || unSubscription.getSubscriptionName().isEmpty() )
        {
            throw new InternalInvalidSubscriptionException( "Empty subscription name is invalid: { " + unSubscription.getSubscriptionName() + " }" );
        }
        PlcSubscriptionResponse response= subscriptions.get( unSubscription.getSubscriptionName());
        if ( response == null )
        {
            throw new InternalInvalidSubscriptionException( "Subscription does not exist: { " + unSubscription.getSubscriptionName() + " }" );
        }
        subscriptions.remove( unSubscription.getSubscriptionName(), response );
        //TODO does not exist
        //if ( sourceCallback != null ) unregister( subscription.getSubscriptionName() );
    }

    /**
     * Get the field handles of the subscription.
     * @param subscriptionName The name of the subscription concerned.
     * @return The collection of handles.
     * @throws InternalInvalidSubscriptionException When no subscription with given name exists.
     */
    public Collection<PlcSubscriptionHandle> getHandles( String subscriptionName ) throws InternalInvalidSubscriptionException
    {
        if ( subscriptionName == null || subscriptionName.isEmpty() )
        {
            throw new InternalInvalidSubscriptionException( "Empty subscription name is invalid: { " + subscriptionName + " }" );
        }
        PlcSubscriptionResponse response= subscriptions.get( subscriptionName );
        if ( response == null )
        {
            throw new InternalInvalidSubscriptionException( "Subscription does not exist: { " + subscriptionName + " }" );
        }
        return response.getSubscriptionHandles();
    }

    /**
     * Register this as handler for the subscription.
     * @param subscriptionName the name of the subscription.
     */
    private synchronized void register( String subscriptionName )
    {
        PlcSubscriptionResponse subscriptionResponse= subscriptions.get( subscriptionName );
        for ( PlcSubscriptionHandle handle : subscriptionResponse.getSubscriptionHandles() )
        {
            handle.register( this );
        }
    }

    /**
     * Accept PLC event, serialize to XML and hand over to the Mule flow.
     */
    @Override
    public void accept( PlcSubscriptionEvent event )
    {
        XmlSerializerResult serializedContent;
        try
        {
            serializedContent= XmlSerializer.xmlSerialize( event );
        }
        catch ( ParserConfigurationException e )
        {
            logger.error( "Handler { " + handlerName + " } cannot process event.", e );
            return;
        }
        //hand over to Mule
        sourceCallback.handle( XmlSerializer.createMuleResult( serializedContent ) );
    }
}
