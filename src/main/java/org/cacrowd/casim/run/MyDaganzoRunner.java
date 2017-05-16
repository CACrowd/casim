package org.cacrowd.casim.run;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cacrowd.casim.pedca.context.Context;
import org.cacrowd.casim.pedca.engine.AgentMover;
import org.cacrowd.casim.pedca.engine.CAAgentMover;
import org.cacrowd.casim.pedca.environment.grid.EnvironmentGrid;
import org.cacrowd.casim.pedca.environment.markers.MarkerConfiguration;
import org.cacrowd.casim.pedca.utility.Constants;
import org.cacrowd.casim.scenarios.ContextGenerator;
import org.cacrowd.casim.scenarios.EnvironmentGenerator;
import org.cacrowd.casim.utility.SimulationObserver;
import org.cacrowd.casim.visualizer.VisualizerEngine;

import java.io.File;
import java.io.IOException;

/**
 * Created by Gregor Laemmel on 16.05.2017.
 */
public class MyDaganzoRunner {

    public static void main(String [] args) throws IOException, ClassNotFoundException {
        Context context = createContextWithResourceEnvironmentFileV2("src/main/resources/environmentGrid_Dag12.csv");
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Context.class).toInstance(context);
                bind(AgentMover.class).to(CAAgentMover.class);
                bind(SimulationObserver.class).to(VisualizerEngine.class);
            }
        });
        SimulationObserver observer = injector.getInstance(SimulationObserver.class);
        observer.observerEnvironmentGrid();


    }
    public static Context createContextWithResourceEnvironmentFileV2(String envFileName){
        EnvironmentGrid environmentGrid = null;
        MarkerConfiguration markerConfiguration = null;
        try {
            File environmentFile = new File(envFileName);
            environmentGrid = new EnvironmentGrid(environmentFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        markerConfiguration = EnvironmentGenerator.searchFinalDestinations(environmentGrid);
        EnvironmentGenerator.addTacticalDestinations(markerConfiguration, environmentGrid);
        Context context = new Context(environmentGrid, markerConfiguration);
        return context;
    }


}
