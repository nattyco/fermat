/*
* @#ClientsConnectionsManager.java - 2016
* Copyright bitDubai.com., All rights reserved.
 * You may not modify, use, reproduce or distribute this software.
* BITDUBAI/CONFIDENTIAL
*/
package com.bitdubai.fermat_p2p_plugin.layer.communications.network.client.developer.bitdubai.version_1.structure;

import com.bitdubai.fermat_api.layer.all_definition.common.system.utils.PluginVersionReference;
import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.ECCKeyPair;
import com.bitdubai.fermat_api.layer.all_definition.util.Version;
import com.bitdubai.fermat_api.layer.osa_android.location_system.LocationManager;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.client.developer.bitdubai.version_1.context.ClientContext;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.client.developer.bitdubai.version_1.context.ClientContextItem;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.client.developer.bitdubai.version_1.util.HardcodeConstants;
import com.bitdubai.fermat_pip_api.layer.platform_service.error_manager.interfaces.ErrorManager;
import com.bitdubai.fermat_pip_api.layer.platform_service.event_manager.interfaces.EventManager;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.client.developer.bitdubai.version_1.structure.ClientsConnectionsManager</code>
 * <p/>
 * Created by Hendry Rodriguez - (elnegroevaristo@gmail.com) on 05/05/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class ClientsConnectionsManager {

    /*
     * Represent the listActorConnectIntoNode
     */
    private Map<String, String> listActorConnectIntoNode;

    /*
     * Represent the listConnectionActiveToNode
     */
    private Map<String, NetworkClientCommunicationConnection> listConnectionActiveToNode;

    /*
     * Represent the node identity
     */
    private ECCKeyPair identity;

    private ErrorManager errorManager;

    private EventManager eventManager;

    private LocationManager locationManager;

    public ClientsConnectionsManager(){
        this.listActorConnectIntoNode = new HashMap<>();
        this.listConnectionActiveToNode = new HashMap<>();
        this.identity = (ECCKeyPair) ClientContext.get(ClientContextItem.CLIENT_IDENTITY);
        this.errorManager = (ErrorManager) ClientContext.get(ClientContextItem.ERROR_MANAGER);
        this.eventManager = (EventManager) ClientContext.get(ClientContextItem.EVENT_MANAGER);
        this.locationManager = (LocationManager) ClientContext.get(ClientContextItem.LOCATION_MANAGER);
    }

    public Map<String, String> getListActorConnectIntoNode() {
        return listActorConnectIntoNode;
    }

    public void setListActorConnectIntoNode(Map<String, String> listActorConnectIntoNode) {
        this.listActorConnectIntoNode = listActorConnectIntoNode;
    }

    public Map<String, NetworkClientCommunicationConnection> getListConnectionActiveToNode() {
        return listConnectionActiveToNode;
    }

    public void setListConnectionActiveToNode(Map<String, NetworkClientCommunicationConnection> listConnectionActiveToNode) {
        this.listConnectionActiveToNode = listConnectionActiveToNode;
    }

    public synchronized void requestConnectionToNodeExtern(String identityPublicKey, String hostPath){

        try {

            URI uri = new URI(HardcodeConstants.WS_PROTOCOL +  hostPath + "/fermat/ws/client-channel");

            final NetworkClientCommunicationConnection networkClientCommunicationConnection = new NetworkClientCommunicationConnection(
                    uri,
                    errorManager,
                    eventManager,
                    locationManager,
                    identity,
                    new PluginVersionReference(new Version()),
                    null,
                    -1,
                    Boolean.TRUE
                    );

            listActorConnectIntoNode.put(identityPublicKey, hostPath);
            listConnectionActiveToNode.put(hostPath, networkClientCommunicationConnection);

            new Thread(){
                @Override
                public void run(){
                    networkClientCommunicationConnection.initializeAndConnect();
                }
            }.start();


        } catch (Exception e) {
            e.printStackTrace();
        }




    }

}