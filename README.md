Uniquid java library 
======================================

The Uniquid java library is a Java implementation of the Uniquid protocol.

It allows to create an Uniquid node and manage smart contracts among nodes.

### Technologies

* Java 7
* [Gradle 3.4+](https://gradle.org/) - for building the project

### Getting started

To get started, it is best to have the latest JDK and Maven installed. The HEAD of the `master` branch contains the latest development code and various production releases are provided on feature branches.

#### Building from the command line

To perform a full build use
```
gradle clean build
```
The outputs are under the `build` directory.

To move jar package in your /m2/repository
```
gradle install
```

#### Usage Example

```java
// Create a Register Factory that uses SQLite
final SQLiteRegisterFactory registerFactory = new SQLiteRegisterFactory("jdbc:sqlite:/tmp/register.db");

// Create new Uniquid Node from a random seed
UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();
		builder.setNetworkParameters(networkParameters)
		.setProviderFile(providerWalletFile)
		.setUserFile(userWalletFile)
		.setProviderChainFile(chainFile)
		.setUserChainFile(userChainFile)
		.setRegisterFactory(registerFactory)
		.setNodeName(machineName);

// ask the builder to create a node with a random seed
uniquidNode = builder.build();
	
// Register a custom event listener
uniquidNode.addUniquidNodeEventListener(eventListener);

// Create a connector that uses MQTT
Connector connectorServiceFactory = new MQTTConnector.Builder()
	.set_broker("tcp://broker.example.com:1883")
	.set_topic("example")
	.build();

// Create Uniquid Simplifier
UniquidSimplifier simplifier = new UniquidSimplifier(registerFactory, connectorServiceFactory, uniquidNode);

// Register custom functions
simplifier.addFunction(new CustomFunction1(), 33);
simplifier.addFunction(new CustomFunction2(), 40);

// start Uniquid core library
simplifier.start();

```
