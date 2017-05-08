package com.uniquid.register.orchestrator;

import com.uniquid.register.RegisterFactory;
import com.uniquid.register.exception.RegisterException;

/**
 * Implementation of Factory Design Pattern for DAO (Data Access Object).
 * 
 * Is used to delegate to an implementor class the creation of DAO concrete objects that
 * manage data toward a data source.
 */
public interface OrchestratorRegisterFactory extends RegisterFactory {

	/**
	 * Returns a IOrchestratorRegister instance
	 * 
	 * @return a IOrchestratorRegister instance
	 * @throws RegisterException in case of problem occurs
	 */
	public IOrchestratorRegister getOrchestratorRegister() throws RegisterException;
	
}
