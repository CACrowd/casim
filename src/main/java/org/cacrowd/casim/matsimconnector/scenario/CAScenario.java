/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 */

package org.cacrowd.casim.matsimconnector.scenario;

import org.apache.log4j.Logger;
import org.cacrowd.casim.matsimconnector.network.HybridNetworkBuilder;
import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.cacrowd.casim.matsimconnector.utility.IdUtility;
import org.cacrowd.casim.matsimconnector.utility.LinkUtility;
import org.cacrowd.casim.matsimconnector.utility.MathUtility;
import org.cacrowd.casim.pedca.context.Context;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CAScenario {

    private static final Logger log = Logger.getLogger(CAScenario.class);
    private final Map<Id<CAEnvironment>, CAEnvironment> environments;
    private final Map<Link, CAEnvironment> linkToEnvironment;
    private boolean connected;
    private Scenario matsimScenario;

    public CAScenario() {
        this.environments = new HashMap<Id<CAEnvironment>, CAEnvironment>();
        this.linkToEnvironment = new HashMap<Link, CAEnvironment>();
    }

    public CAScenario(String path, int nEnvironments) {
        this();
        for (int i = 0; i < nEnvironments; i++)
            loadConfiguration(path + "/input" + i);
    }

    public CAScenario(String path) {
        this();
        loadConfiguration(path);
    }

    public CAScenario(Context contextCA) {
        this();
        addCAEnvironment(new CAEnvironment("0", contextCA));
    }

    public void initNetworks() {
        int index = 0;
        for (CAEnvironment environmentCA : environments.values())
            HybridNetworkBuilder.buildNetwork(environmentCA, this, index++);
    }

    public void connect(Scenario matsimScenario) {
        if (this.connected) {
            log.warn("CA Scenario already connected!");
            return;
        }
        log.debug("Connecting CA scenario.");
        matsimScenario.addScenarioElement(Constants.CASCENARIO_NAME, this);
        this.matsimScenario = matsimScenario;
        Network scNet = matsimScenario.getNetwork();
        for (CAEnvironment environmentCA : environments.values())
            connect(environmentCA, scNet);
        this.connected = true;
    }

    private void loadConfiguration(String path) {
        Context context;
        try {
            context = new Context(path);
            addCAEnvironment(new CAEnvironment("" + environments.size(), context));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void connect(CAEnvironment environmentCA, Network scNet) {
        Network envNet = environmentCA.getNetwork();
        for (Node nodeCA : envNet.getNodes().values()) {
            Node nodeMatsim = scNet.getNodes().get(nodeCA.getId());
            if (nodeMatsim == null) {

//                Map<Id<Link>, ? extends Link> tmp = new HashMap<>(nodeCA.getInLinks());
//                for (Link l : tmp.values()) {
//                    nodeCA.removeInLink(l.getId());
//                }
//                tmp = new HashMap<>(nodeCA.getOutLinks());
//                for (Link l : tmp.values()) {
//                    nodeCA.removeOutLink(l.getId());
//                }
            	nodeCA.getInLinks().clear();
				nodeCA.getOutLinks().clear();
                scNet.addNode(nodeCA);
                plugNode(nodeCA, scNet, environmentCA);
            } else {
                log.warn("Node already present in the network!");
            }
        }
        for (Link link : envNet.getLinks().values()) {
            if (scNet.getLinks().get(link.getId()) != null) {
                //don't create links that already exist
                continue;
            }
            Node nFrom = scNet.getNodes().get(link.getFromNode().getId());
            Node nTo = scNet.getNodes().get(link.getToNode().getId());
            if (link.getFromNode() != nFrom) {
                link.setFromNode(nFrom);
            }
            if (link.getToNode() != nTo) {
                link.setToNode(nTo);
            }
            scNet.addLink(link);
        }
    }

    private void plugNode(Node n, Network scNet, CAEnvironment environmentCA) {
        Node pivot = null;
        double radius = .4;
        Set<String> modesToCA = new HashSet<String>();
        modesToCA.add("car");
        modesToCA.add("walk");
        modesToCA.add(Constants.TO_CA_LINK_MODE);
        Set<String> modesToQ = new HashSet<String>();
        modesToQ.add("car");
        modesToQ.add("walk");
        modesToQ.add(Constants.TO_Q_LINK_MODE);
        for (Node node : scNet.getNodes().values()) {
            if (node != n && MathUtility.EuclideanDistance(n.getCoord(), node.getCoord()) <= radius) {
                log.debug("plugging nodes in the network: " + n.getCoord() + " " + node.getCoord());
                pivot = node;
                break;
            }
        }
        if (pivot == null)
            return;

        Id<Node> fromId = pivot.getId();
        Id<Node> toId = n.getId();

        for (Link link : pivot.getOutLinks().values()) {
            scNet.removeLink(link.getId());
            link.setFromNode(n);
            link.setLength(MathUtility.EuclideanDistance(link.getFromNode().getCoord(), link.getToNode().getCoord()));//link.getLength()+Constants.TRANSITION_LINK_LENGTH);
            link.setAllowedModes(modesToQ);
            scNet.addLink(link);
            mapLinkToEnvironment(link, environmentCA);
        }

        //Set<String> modesToQ = new HashSet<String>();
        //modesToQ.add("car");
        //modesToQ.add("walk");
        //modesToQ.add(Constants.TO_Q_LINK_MODE);
        //Id <Link> toQId = IdUtility.createLinkId(toId, fromId);
        //Link toQ = scNet.getFactory().createLink(toQId, n, pivot);
        //LinkUtility.initLink(toQ, Constants.TRANSITION_LINK_LENGTH, modesToQ);
        //scNet.addLink(toQ);


        Id<Link> toCAId = IdUtility.createLinkId(fromId, toId);
        Link toCA = scNet.getFactory().createLink(toCAId, pivot, n);
        LinkUtility.initLink(toCA, MathUtility.EuclideanDistance(toCA.getFromNode().getCoord(), toCA.getToNode().getCoord()), 10, modesToCA);
        scNet.addLink(toCA);
        mapLinkToEnvironment(toCA, environmentCA);
    }

    public Map<Id<CAEnvironment>, CAEnvironment> getEnvironments() {
        return environments;
    }

    public void addCAEnvironment(CAEnvironment environment) {
        this.environments.put(environment.getId(), environment);
    }

    public void mapLinkToEnvironment(Link link, CAEnvironment environmentCA) {
        this.linkToEnvironment.put(link, environmentCA);
    }

    public CAEnvironment getCAEnvironment(Id<CAEnvironment> id) {
        return this.environments.get(id);
    }

    public Scenario getMATSimScenario() {
        return matsimScenario;
    }

    public CAEnvironment getCAEnvironment(Link link) {
        return linkToEnvironment.get(link);
    }


}
