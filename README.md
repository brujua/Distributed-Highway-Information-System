# Distributed-Highway-Information-System
System to trace a fleet of vehicles and manage the communication between them, in a way that its decentralized and fault tolerant.  

### About the protocol
The protocol seeks to form [P2P](https://en.wikipedia.org/wiki/Peer-to-peer) networks among the vehicles that are "near" to each other. It also provides a platform of nodes (that know each other through a coordinator) for the vehicles to report to that allow them to send and receive global messages. This nodes are the gateway of the vehicles that permit them to start the peer discovery.
The current specification can be found [here](/Protocol-Specification.pdf)


### Demo application
This its a simple visual application to demonstrate some capabilities of the core system.  
You can download it from [here](/demo-application-V1.2.rar)
![Demo App Gif](/demo-app.gif)
Note: Every instance could run on a different machine.
  
##### How to use it?
 * You need to have Java 8 o greater installed on your system.
 * Extract the rar in any folder you want.  
 * Be sure that the *resources* folder its on the same directory that the *.jar*.  
 * Use the *.jar* as an executable to launch a node as many times as nodes you want.  
 * In the resources folder there are three configuration files you cant tweak at your wish : *config-cars.properties*, *config-hwnodes.properties* and *hw-config.json*.  


### Peer-Discovery example
<p align="center">
  <img src="https://i.imgur.com/NKDB2J0.png" width="500" alt="peer discovery example part1">

  <img src="https://i.imgur.com/l43ayoW.png" width="500" alt="peer discovery example part2">

  <img src="https://i.imgur.com/4hbL56o.png" width="500" alt="peer discovery example part3">

  <img src="https://i.imgur.com/sCdxSZd.png" width="500" alt="peer discovery example part4">

  <img src="https://i.imgur.com/HX3gXsX.png" width="500" alt="peer discovery example part5">

  <img src="https://i.imgur.com/f8Aadtz.png" width="500" alt="peer discovery example part6">

  <img src="https://i.imgur.com/QVm2tY0.png" width="500" alt="peer discovery example part7">
  
  <img src="https://i.imgur.com/JrBUGvu.png" width="500" alt="peer discovery example part8">
 </p>
