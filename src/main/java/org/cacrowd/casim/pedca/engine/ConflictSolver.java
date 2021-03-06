/*
 * casim, cellular automaton simulation for multi-destination pedestrian
 * crowds; see www.cacrowd.org
 * Copyright (C) 2016-2017 CACrowd and contributors
 *
 * This file is part of casim.
 * casim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 *
 */

package org.cacrowd.casim.pedca.engine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.cacrowd.casim.pedca.agents.Agent;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.environment.grid.GridPoint;
import org.cacrowd.casim.pedca.environment.grid.PedestrianGrid;
import org.cacrowd.casim.pedca.utility.CASimRandom;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.pedca.utility.Lottery;
import org.cacrowd.casim.pedca.utility.MathUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Singleton
public class ConflictSolver {
    private Context context;

    @Inject
    public ConflictSolver(Context context) {
        this.context = context;
    }

    public void step() {
        solveBidirectionalConflicts();
        solveConflicts();
    }

    private void solveBidirectionalConflicts() {
        Iterable<Agent> pedsIt = getPedestrians();
        for (Agent agent1 : pedsIt) {
            if (agent1.isWillingToSwap()) {
                GridPoint chosenPosition = agent1.getNewPosition();
                PedestrianGrid pedestrianGrid = agent1.getUsedPedestrianGrid();
                Agent agent2 = pedestrianGrid.getPedestrian(chosenPosition);

                if (agent1 != agent2 && agent2.isWillingToSwap() && agent2.getNewPosition().equals(agent1.getPosition())) {
                    startPedestrianSwitching(agent1, agent2);
                } else {
                    agent1.revertWillingToSwap();
                }
            }
        }
    }

    private void startPedestrianSwitching(Agent agent1, Agent agent2) {
        int stepToPerformSwap = (int) Math.round(MathUtility.average(agent1.calculateStepToPerformSwap(), agent2.calculateStepToPerformSwap()));
        agent1.startBidirectionalSwitch(stepToPerformSwap);
        agent2.startBidirectionalSwitch(stepToPerformSwap);
    }

    private void solveConflicts() {

//		Ottengo tutti i pedestrian nel context
        Iterable<Agent> pedsIt = getPedestrians();
//		Lista contenente tutti i pedoni con destinazione COMUNE
        ArrayList<Agent> pedsList = new ArrayList<Agent>();
//		Lista che user� per riempire la lista di 
        ArrayList<GridPoint> nextPosList = new ArrayList<GridPoint>();
        //---HashSet<GridPoint> nextPosList = new HashSet<GridPoint>();

//		ArrayList<Agent> listaCompletaPedoni = new ArrayList<Agent>();
//		HashSet per ottenere le destinazioni UNIVOCHE dei pedoni
        HashSet<GridPoint> uniqueGP = new HashSet<GridPoint>();
//		Vecchia dimensione della hashMap
        int oldSize;

        HashMap<GridPoint, Agent> multipleGP = new HashMap<GridPoint, Agent>();

//      Ottengo una lista di tutte le destinazioni UNICHE dei pedoni
//      Inizializzo la lista di pedoni che avranno un conflitto (anche la lista della destinazioni con conflitti)
        for (Agent p : pedsIt) {
            oldSize = uniqueGP.size();
            GridPoint agentNewPosition = p.getNewPosition();
            uniqueGP.add(agentNewPosition);

//			listaCompletaPedoni.add(p);
            if (oldSize == uniqueGP.size()) {
                pedsList.add(p);
                if (!nextPosList.contains(agentNewPosition)) {
                    nextPosList.add(agentNewPosition);
                }
            } else {
                multipleGP.put(agentNewPosition, p);
            }
        }

//		Se non esiste alcun pedone con conflitto, posso anche evitare!
        if (pedsList.size() == 0)
            return;

//		Per ogni destinazione conflittuale, creo una lista temporanea di pedoni da cui estrarr� un vincitore, gli altri verranno riposizionati
//		Poi elimino dalla lista dei pedoni conflittuali i pedoni con conflitto risolto

        for (GridPoint gp : nextPosList) {
            ArrayList<Agent> sameGPPedList = new ArrayList<Agent>();

            for (Agent p : pedsList) {
                if (p.getNewPosition().equals(gp)) {
                    sameGPPedList.add(p);
                }
            }

            sameGPPedList.add(multipleGP.get(gp));

            if (!frictionCondition()) {
                int randomWinner = CASimRandom.nextInt(sameGPPedList.size());
                sameGPPedList.remove(randomWinner);
            }

            for (Agent p : sameGPPedList) {
                p.revertChoice();
                pedsList.remove(p);
            }
        }

		/* THE COURSED TEST 
		for(int i = 0; i < listaCompletaPedoni.size(); i++){
			for(int j = i+1; j < listaCompletaPedoni.size(); j++)
				if(getNewAgentPosition(listaCompletaPedoni.get(i)).equals(getNewAgentPosition(listaCompletaPedoni.get(j)))){
					Log.error("Error in Conflict Solving!!");
			}
		}*/
    }

//	public GridPoint getNewAgentPosition(Agent p) {
//		return ((Pedestrian)p).getRealNewPosition();
//	}

    private boolean frictionCondition() {
        return Lottery.simpleExtraction(Constants.FRICTION_PROBABILITY);
    }

    private Iterable<Agent> getPedestrians() {
        return context.getPopulation().getPedestrians();
    }
}
