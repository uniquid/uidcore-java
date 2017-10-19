# FAQ

## I've received a lot of seeds already imprinted and orchestrated. How I can use? There is an example that I can use?

In order to create an Uniquid Node from a known seed you can use the following example:

```java
// Create new Uniquid Node from a known seed
UniquidNodeImpl.UniquidNodeBuilder builder = new UniquidNodeImpl.UniquidNodeBuilder();

builder.setNetworkParameters(networkParameters).
	setProviderFile(providerWalletFile).
	setUserFile(userWalletFile).
	setProviderChainFile(chainFile).
	setUserChainFile(userChainFile).
	setRegisterFactory(registerFactory).
	setNodeName(machineName);

UniquidNode uniquidNode = builder.buildFromHexSeed("PUTHEREYOURHEXSEED", 1495534782);
```

Basically, you have to use the buildFromHexSeed method from the Builder: the first parameter is the hex seed and the second is the node's creation time.

## Please, can you provide a better custom function example?

The function registration mechanism allows an user of the library to register a piece of code that will be executed by the system when somebody with the correct permission will ask for it.

Basically, a function can be any kind of object but the only requirement is that the object implements the com.uniquid.core.provider.Function interface.

For simplicity, the user can extend the com.uniquid.core.provider.impl.GenericFunction class and override the service() method.

For example, here is presented a simple "echo" function: it will reply with input received:

```java
package com.uniquid.function;

import java.io.IOException;

import com.uniquid.core.ProviderRequest;
import com.uniquid.core.ProviderResponse;
import com.uniquid.core.provider.exception.FunctionException;
import com.uniquid.core.provider.impl.GenericFunction;

public class EchoFunction extends GenericFunction {

	@Override
	public void service(FunctionRequestMessage inputMessage, FunctionResponseMessage outputMessage, byte[] payload)
			throws FunctionException, IOException {
		
		outputMessage.setResult("UID_echo: " + inputMessage.getParameters());
		
	}

}
```

The user can register this function into the slot 33 with the following code snippet:

```java
simplifier.addFunction(new EchoFunction(), 33);
```
