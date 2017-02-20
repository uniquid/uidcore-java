package com.uniquid.register.orchestrator;

import java.util.Map;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;

public abstract class OrchestratorRegisterFactory extends RegisterFactory {

	public OrchestratorRegisterFactory(Map<String, Object> configuration) throws RegisterException {
		super(configuration);
	}

	public abstract IOrchestratorRegister getOrchestratorRegister() throws RegisterException;
	
}
