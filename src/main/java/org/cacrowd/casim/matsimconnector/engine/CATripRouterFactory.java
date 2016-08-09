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

package org.cacrowd.casim.matsimconnector.engine;

import org.cacrowd.casim.matsimconnector.utility.Constants;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.router.DefaultRoutingModules;
import org.matsim.core.router.RoutingModule;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeAndDisutility;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelTime;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

public class CATripRouterFactory implements  Provider<RoutingModule>{
	private Scenario scenario;
	private Map<String, TravelTime> travelTimes;
	private Map<String, TravelDisutilityFactory> travelDisutilities;

	@Inject
	CATripRouterFactory(Scenario scenario, Map<String, TravelTime> travelTimes, Map<String, TravelDisutilityFactory> travelDisutilities) {
		this.scenario = scenario;
		this.travelTimes = travelTimes;
		this.travelDisutilities = travelDisutilities;
	}

	@Override
	public RoutingModule get() {
		return DefaultRoutingModules.createPureNetworkRouter(Constants.CAR_LINK_MODE, scenario.getPopulation()
				.getFactory(), scenario.getNetwork(), createRoutingAlgo());
	}

	private LeastCostPathCalculator createRoutingAlgo() {
		return new AStarLandmarksFactory(
				scenario.getNetwork(),
				new FreespeedTravelTimeAndDisutility(scenario.getConfig().planCalcScore()),
				scenario.getConfig().global().getNumberOfThreads()).createPathCalculator(scenario.getNetwork(),
				travelDisutilities.get("car").createTravelDisutility(travelTimes.get("car")),
				travelTimes.get("car"));
	}
}
