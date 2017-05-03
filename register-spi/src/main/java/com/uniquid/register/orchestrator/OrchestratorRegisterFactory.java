package com.uniquid.register.orchestrator;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;

public interface OrchestratorRegisterFactory extends RegisterFactory {

	public IOrchestratorRegister getOrchestratorRegister() throws RegisterException;
	
}
