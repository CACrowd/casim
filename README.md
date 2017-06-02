[![Build Status](https://travis-ci.org/CACrowd/casim.svg?branch=master)](https://travis-ci.org/CACrowd)

# casim
This project provides CA pedestrian simulation that is based on the article

Crociani, L. and Lämmel, G. (2016). Multidestination Pedestrian Flows in 
Equilibrium: A Cellular Automaton-Based Approach. Computer-Aided Civil 
and Infrastructure Engineering, doi: [10.1111/mice.12209](http://onlinelibrary.wiley.com/doi/10.1111/mice.12209/abstract)

# Getting started
## Build
The easiest way to build **casim** is using Maven in the commandline. A jar file with all dependencies can be generated by invoking:

    mvn test
    mvn assembly:single
    
This will generate a jar file with all dependencies, which will be located in the subdirectory *target* 

## Run as stand-alone application
Even though **casim** is designed to be coupled to other transport simulation frameworks such as [MATSim](http://matsim.org) 
or [SUMO](http://sumo.dlr.de) it can also be used as a stand-alone application. An entry point to setup your own stand-alone scenario
is the class:
    
    org.cacrowd.casim.run.MyTestRunner
     
Some sample environment layouts can be found in the resources folder.

## Run as hybrid simulation
**casim** offers an interface to be coupled with other simulation frameworks using [protobuf](https://github.com/google/protobuf) 
over [GRPC](http://www.grpc.io). In this setup **casim** acts as a server where other client simulation frameworks can connect to.
A demo client-server setup is provided by the class:

    org.cacrowd.casim.hybridsim.run.MyHybridSimTestRunner
    
## Run hybrid simulation server in DIY docker container
**casim** server can also be run in a docker container. You can build and run the docker container by invoking

    docker build --rm -t casim .
    docker run -p 9000:9000 casim
   
## Run hybrid simulation server in provided docker container
Docker builds are available via [docker hub](https://hub.docker.com/r/grgrlmml/casim/). To run **casim** in a provided docker container enter

    docker pull grgrlmml/casim
    docker run -p 9000:9000 grgrlmml/casim
    
## Repeating experiments discussed in the CACAIE article
Thus far the Nash equilibrium and marginal social cost based approach experiments (see, sec 4.1 in the article) are available.

First you have to build the package

    mvn test
    mvn assembly:single

Then you can run the Nash equilibrium experiments by invoking

    java -cp casim-0.1-SNAPSHOT-jar-with-dependencies.jar org.cacrowd.casim.matsimintegration.DaganzoExperimentRunner Nash <bottleneck width>

and the marginal social cost based approach experiments by invoking

    java -cp casim-0.1-SNAPSHOT-jar-with-dependencies.jar org.cacrowd.casim.matsimintegration.DaganzoExperimentRunner MSCB <bottleneck width>

where <bottleneck width> must be one of {0.4, 0.8, 1.2}. Unlike in the article where 100 learning iterations with 2000 agents have been performed, the current setup
 runs the 100 iteration with only 500 agents. You can change this values in the code (see `org.cacrowd.casim.matsimintegration.scenarios.DaganzoScenarioGenerator`).

Per default every 10th iteration of the simulation runs will be visualized in real time in the visualizer gui. You can accelerate/decelerate the visualization with the +/- keys on your keyboard.
