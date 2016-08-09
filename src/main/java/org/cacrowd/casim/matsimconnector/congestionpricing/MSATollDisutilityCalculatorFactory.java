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

/**
 * 
 */
package org.cacrowd.casim.matsimconnector.congestionpricing;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

/**
 * @author ikaddoura, glaemmel
 *
 */
public class MSATollDisutilityCalculatorFactory implements TravelDisutilityFactory {

	private final MSATollHandler tollHandler;
	private final PlanCalcScoreConfigGroup cnScoringGroup;

	public MSATollDisutilityCalculatorFactory(MSATollHandler tollHandler, PlanCalcScoreConfigGroup cnScoringGroup) {
		this.tollHandler = tollHandler;
		this.cnScoringGroup = cnScoringGroup;
	}

	@Override
	public TravelDisutility createTravelDisutility(TravelTime timeCalculator) {
		final MSATollTravelDisutilityCalculator ttdc = new MSATollTravelDisutilityCalculator(timeCalculator, cnScoringGroup, this.tollHandler);

		return new TravelDisutility(){

			@Override
			public double getLinkTravelDisutility(final Link link, final double time, final Person person, final Vehicle vehicle) {
				double linkTravelDisutility = ttdc.getLinkTravelDisutility(link, time, person, vehicle);
				return linkTravelDisutility;
			}
			
			@Override
			public double getLinkMinimumTravelDisutility(Link link) {
				return ttdc.getLinkMinimumTravelDisutility(link);
			}
		};
	}

}
