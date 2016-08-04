# axon-Kafka-Connector
axonframework Kafka Connector POC
Axonframework Kafka POC 
Author Nishant Chauhan

This is a eclipse project. The aim is to create a proof of concept of Axonframe Kafka connector using library from GitHub located athttps://github.com/viadeo/axon-kafka-terminal.

The axon-demo-core project is downloaded from Axonframework site. It was a demo project connecting ActiveMQ with Axonframework.
In this project I have modified the code to replace ActiveMQ with Kafka. All the ActiveMQ related code is removed from this project.

org.oiavorskyi.axondemo.Application.java - This file has main method. 
It uses the above mentioned axon-kafka-terminal library to create ClusteringEventBus (Axonframework class) using KafkaTerminalFactory.
TopicStrategyFactory is created to PrefixTopicStrategy which basically uses UUID.randomUUID() to generate random prefix.
Next the kafka Producer and consumer properties are created.
A subscriber is created for this eventBus which is just to print on output (console) the eventMessage.
Next a eventMessage is created and published on the eventBus. This message is using CargoTrackingCommandMessage which is a POJO class representing the domain object. 

org.oiavorskyi.axondemo.aggregates.CargoTracking is a Axon aggregate. It extends org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot.

org.oiavorskyi.axondemo.api.CargoTrackingCommandMessage is a POJO class which represent the domain object.

To execute this project:
1) keep zookeeper and Kafka running on the machine. 
2) Command for above on windows machine is (need to be executed on kafka Home directory)
.\bin\windows\zookeeper-server-start.bat config\zookeeper.properties
.\bin\windows\kafka-server-start.bat config\server.properperties
3) Run the Application.java as Java application. 
4) Output -
a. On console one can see the message received by subscriber. 
b. In kafka logs the message can be seen containing the pojo class properties. 
