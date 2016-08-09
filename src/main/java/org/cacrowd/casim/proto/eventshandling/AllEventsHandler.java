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

package org.cacrowd.casim.proto.eventshandling;


import org.apache.log4j.Logger;
import org.matsim.api.core.v01.events.Event;
import org.matsim.core.events.handler.BasicEventHandler;

/**
 * Created by laemmel on 09/08/16.
 */
public class AllEventsHandler implements BasicEventHandler {

    private static final Logger log = Logger.getLogger(AllEventsHandler.class);

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(Event event) {
        log.info(event);

    }
}
