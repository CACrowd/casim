# casim
This project provides CA pedestrian simulation that is based on the article

Crociani, L. and Lämmel, G. (2016). Multidestination Pedestrian Flows in 
Equilibrium: A Cellular Automaton-Based Approach. Computer-Aided Civil 
and Infrastructure Engineering, doi: [10.1111/mice.12209](http://onlinelibrary.wiley.com/doi/10.1111/mice.12209/abstract)

# Getting started
## Build
The easiest way to build **casim** is using Maven in the commandline. A jar file with all dependencies can be generated by invoking:

    mvn assembly:single
    
This will generate a jar file with all dependencies, which will be located in the subdirectory *target* 

## Run as stand-alone application
Even though **casim** is designed to be coupled to other transport simulation frameworks such as [MATSim](http://matsim.org) 
or [SUMO](http://sumo.dlr.de) it can also be used as a stand-alone application. An entry point to setup your own stand-alone scenario
is the class:
    
    org.cacrowd.casim.run.MyDaganzoRunner
     
Some sample environment layouts can be found in the resources folder.

## Run as hybrid simulation
**casim** offers an interface to be coupled with other simulation frameworks using [protobuf](https://github.com/google/protobuf) 
over [GRPC](http://www.grpc.io). In this setup **casim** acts as a server where other client simulation frameworks can connect to.
A demo client-server setup is provided by the class:

    org.cacrowd.casim.hybridsim.run.MyHybridsimRunner
    


