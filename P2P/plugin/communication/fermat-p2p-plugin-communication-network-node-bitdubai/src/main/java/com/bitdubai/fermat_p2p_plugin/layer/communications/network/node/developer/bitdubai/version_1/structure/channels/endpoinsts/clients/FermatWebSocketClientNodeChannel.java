/*
 * @#WebSocketClientNodeChannel.java - 2016
 * Copyright bitDubai.com., All rights reserved.
 * You may not modify, use, reproduce or distribute this software.
 * BITDUBAI/CONFIDENTIAL
 */
package com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.endpoinsts.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.exception.PackageTypeNotSupportedException;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.conf.ClientNodeChannelConfigurator;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.processors.nodes.ReceivedActorCatalogTransactionsRespondProcessor;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.processors.nodes.ReceivedNodeCatalogTransactionsRespondProcessor;

import org.jboss.logging.Logger;

import java.io.IOException;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.FermatWebSocketClientNodeChannel.FermatWebSocketClientNodeChannel</code>
 * is the client to communicate nodes by the node client channel<p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 04/04/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@ClientEndpoint(configurator = ClientNodeChannelConfigurator.class )
public class FermatWebSocketClientNodeChannel extends FermatWebSocketChannelEndpoint {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(FermatWebSocketClientNodeChannel.class.getName());

    /**
     * Represent the clientConnection
     */
    private Session clientConnection;

    /**
     * (non-javadoc)
     *
     * @see FermatWebSocketChannelEndpoint#initPackageProcessorsRegistration()
     */
    @Override
    protected void initPackageProcessorsRegistration() {
        /*
         * Register all messages processor for this
         * channel
         */
        registerMessageProcessor(new ReceivedNodeCatalogTransactionsRespondProcessor(this));
        registerMessageProcessor(new ReceivedActorCatalogTransactionsRespondProcessor(this));
    }

    /**
     *  Method called to handle a new connection
     *
     * @param session connected
     * @param endpointConfig created
     * @throws IOException
     */
    @OnOpen
    public void onConnect(final Session session, EndpointConfig endpointConfig) {

        System.out.println(" --------------------------------------------------------------------- ");
        System.out.println(" Starting method onOpen");
        System.out.println(" id = "+session.getId());
        System.out.println(" url = "+session.getRequestURI());

        this.clientConnection = session;
    }

    /**
     * Method called to handle a new message received
     *
     * @param packageReceived new
     * @param session sender
     */
    @OnMessage
    public void newPackageReceived(Package packageReceived, Session session) {

        LOG.info("New message Received");
        LOG.info("Session: " + session.getId() + " packageReceived = " + packageReceived + "");

        try {

            /*
             * Process the new package received
             */
            processMessage(packageReceived, session);

        }catch (PackageTypeNotSupportedException p){
            LOG.warn(p.getMessage());
        }

    }

    /**
     * Method called to handle a connection close
     *
     * @param closeReason message
     * @param session closed
     */
    @OnClose
    public void onClose(CloseReason closeReason, Session session) {

        LOG.info("Closed session : " + session.getId() + " Code: (" + closeReason.getCloseCode() + ") - reason: "+ closeReason.getReasonPhrase());

    }

}
