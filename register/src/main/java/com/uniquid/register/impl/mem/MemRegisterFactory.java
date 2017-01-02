package com.uniquid.register.impl.mem;

import java.util.Map;

import com.uniquid.register.Register;
import com.uniquid.register.RegisterFactory;

public class MemRegisterFactory extends RegisterFactory {

	private static MemRegister INSTANCE;

	public MemRegisterFactory(Map<String, Object> configuration) {
		super(configuration);
	}

	@Override
	public synchronized Register createRegister() throws Exception {
		
		if (INSTANCE == null) {
			
			INSTANCE = new MemRegister();
			
		}
		
		return INSTANCE;

	}

}