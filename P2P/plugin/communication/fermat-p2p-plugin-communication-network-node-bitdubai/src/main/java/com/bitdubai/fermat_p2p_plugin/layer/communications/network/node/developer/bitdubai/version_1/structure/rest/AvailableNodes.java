/*
* @#AvailableNodes.java - 2016
* Copyright bitDubai.com., All rights reserved.
 * You may not modify, use, reproduce or distribute this software.
* BITDUBAI/CONFIDENTIAL
*/
package com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest;

import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;
import com.bitdubai.fermat_api.layer.osa_android.location_system.LocationSource;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.NodeProfile;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.DistanceCalculator;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContext;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContextItem;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.daos.DaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.entities.NodesCatalog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.AvailableNodes</code>
 * <p/>
 * Created by Hendry Rodriguez - (elnegroevaristo@gmail.com) on 26/04/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@Path("/available/nodes")
public class AvailableNodes implements RestFulServices {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(AvailableNodes.class));

    /**
     * Represent the daoFactory
     */
    private DaoFactory daoFactory;

    /**
     * Represent the gson
     */
    private Gson gson;

    /**
     * Constructor
     */
    public AvailableNodes(){
        daoFactory = (DaoFactory) NodeContext.get(NodeContextItem.DAO_FACTORY);
        gson = new Gson();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String listAvailableNodesProfile(@FormParam("latitude") String latitudeString, @FormParam("longitude") String longitudeString){

        JsonObject jsonObject = new JsonObject();

        LOG.info("Executing listAvailableNodesProfile");
        LOG.info("latitude = " + latitudeString + " longitude = " + longitudeString);

        try {

            /*
             * Cast to Double the String Receive
             */

            Double latitudeSource = Double.parseDouble(latitudeString);
            Double longitudeSource = Double.parseDouble(longitudeString);

            /*
             * Get the locationSource to do the filter of Geolocation
             */
            Location locationSource = new NetworkNodeCommunicationDeviceLocation(
                    latitudeSource ,
                    longitudeSource,
                    null     ,
                    null     ,
                    null     ,
                    System.currentTimeMillis(),
                    LocationSource.UNKNOWN
            );

            /*
             * Get the node catalog list
             */
            List<NodesCatalog> nodesCatalogs = daoFactory.getNodesCatalogDao().findAll();

            /*
             * Filter and order
             */
            List<NodesCatalog> nodesCatalogsFiltered = applyGeoLocationFilter(locationSource, nodesCatalogs);

            if(nodesCatalogsFiltered != null) {

                List<NodeProfile> listNodeProfile = new ArrayList<>();

                for (NodesCatalog nodesCatalog : nodesCatalogsFiltered.subList(0,50)) {

                    NodeProfile nodeProfile = new NodeProfile();
                    nodeProfile.setName((nodesCatalog.getName() != null ? nodesCatalog.getName() : null));
                    nodeProfile.setIp(nodesCatalog.getIp());
                    nodeProfile.setDefaultPort(nodesCatalog.getDefaultPort());
                    nodeProfile.setIdentityPublicKey(nodesCatalog.getIdentityPublicKey());

                    if(nodesCatalog.getLastLatitude() != null && nodesCatalog.getLastLongitude() != null &&
                            nodesCatalog.getLastLatitude() != 0 && nodesCatalog.getLastLongitude() != 0){

                        Location location = new NetworkNodeCommunicationDeviceLocation(
                                nodesCatalog.getLastLatitude() ,
                                nodesCatalog.getLastLongitude(),
                                null     ,
                                null     ,
                                null     ,
                                System.currentTimeMillis(),
                                LocationSource.UNKNOWN
                        );

                        nodeProfile.setLocation(location);

                    }

                    listNodeProfile.add(nodeProfile);

                }

                jsonObject.addProperty("success", Boolean.TRUE);
                jsonObject.addProperty("data", gson.toJson(listNodeProfile));

            }else{

                jsonObject.addProperty("success", Boolean.FALSE);
                jsonObject.addProperty("message", "There are content in the Table");

            }

        } catch (Exception e) {
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("message", gson.toJson(e));
        }

       return gson.toJson(jsonObject);
    }


    /**
     *  Method that apply geo location filter to the list
     *
     * @param clientLocation
     * @param nodesCatalogs
     * @return List<NodesCatalog>
     */
    private List<NodesCatalog> applyGeoLocationFilter(Location clientLocation, List<NodesCatalog> nodesCatalogs) {

        /*
         * Hold the data ordered by distance
         */
        Map<Double, NodesCatalog> orderedByDistance = new TreeMap<>();

        /*
         * For each node
         */
        for (final NodesCatalog node: nodesCatalogs) {

            /*
             * If component has a geo location
             */
            if (node.getLastLatitude() != null &&
                    node.getLastLongitude() != null){


                Location nodeLocation = new Location() {
                    @Override
                    public Double getAccuracy() {
                        return null;
                    }

                    @Override
                    public Double getAltitudeAccuracy() {
                        return null;
                    }

                    @Override
                    public Double getLatitude() {
                        return node.getLastLatitude();
                    }

                    @Override
                    public Double getLongitude() {
                        return node.getLastLongitude();
                    }

                    @Override
                    public Double getAltitude() {
                        return null;
                    }

                    @Override
                    public Long getTime() {
                        return null;
                    }

                    @Override
                    public LocationSource getSource() {
                        return null;
                    }
                };

                /*
                 * Calculate the distance between the two points
                 */
                Double componentDistance = DistanceCalculator.distance(clientLocation, nodeLocation, DistanceCalculator.KILOMETERS);

                /*
                 * Add to the list
                 */
                orderedByDistance.put(componentDistance, node);

            }

        }

        return new ArrayList<>(orderedByDistance.values());
    }


}
