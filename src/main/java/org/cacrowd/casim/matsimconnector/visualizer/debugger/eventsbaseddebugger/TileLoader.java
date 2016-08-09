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

package org.cacrowd.casim.matsimconnector.visualizer.debugger.eventsbaseddebugger;

import processing.core.PApplet;
import processing.core.PImage;

import java.io.File;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class TileLoader implements Runnable{

	
	
	private final BlockingDeque<Tile> tiles = new LinkedBlockingDeque<Tile>();
	private final PApplet p;

	public TileLoader(PApplet p) {
		this.p = p;

	}

	@Override
	public void run() {
		while (true) {
			Tile pTile;
			try {
				pTile = this.tiles.takeFirst();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			String url;
			synchronized (pTile) {
				url = pTile.getUrl();
			}
			
			String path = "/Users/laemmel/tmp/cache/"+url + ".png";
			if (new File(path).isFile()) {
				url = path;
			}
			PImage img = this.p.loadImage(url,"png");
			synchronized (pTile) {
				pTile.setPImage(img);
			}
			if (!new File(path).isFile()) {
				img.save(path);
			}
		}
	}

	public void addTile(Tile pTile) {
		try {
			this.tiles.putFirst(pTile);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public void loadDirectly(Tile t, String path) {
		PImage img = this.p.loadImage(path,"png");
		t.setPImage(img);
	}

}
