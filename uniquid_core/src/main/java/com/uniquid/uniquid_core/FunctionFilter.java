package com.uniquid.uniquid_core;

import com.uniquid.uniquid_core.message.MessageRequest;

public interface FunctionFilter {
	
	void doFilter(MessageRequest messageRequest);

}
