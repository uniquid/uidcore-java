package com.uniquid.register.impl.android.orchestrator;

import java.util.Map;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.impl.android.AndroidRegisterFactory;
import com.uniquid.register.impl.android.Register;
import com.uniquid.register.orchestrator.IOrchestratorRegister;
import com.uniquid.register.orchestrator.OrchestratorRegisterFactory;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserRegister;

import android.content.Context;

public class AndroidOrchestratorRegisterFactory extends OrchestratorRegisterFactory {
	
	private static final String PREFIX = "AndroidOrchestratorRegisterFactory";
    public static final String CONTEXT = PREFIX + ".context";
    
    private OrchestratorRegister orchestratorInstance;
    private Register registerInstance;

	public AndroidOrchestratorRegisterFactory(Map<String, Object> configuration) throws RegisterException {
		super(configuration);
		
	    Context context = (Context) factoryConfiguration.get(CONTEXT);

	    orchestratorInstance = new OrchestratorRegister(context);
	    registerInstance = new Register(context);
	}
	
	public IOrchestratorRegister getOrchestratorRegister(){
		return orchestratorInstance;
	}

	@Override
	public ProviderRegister getProviderRegister() throws RegisterException {
		return registerInstance;
	}

	@Override
	public UserRegister getUserRegister() throws RegisterException {
		return registerInstance;
	}
	
}