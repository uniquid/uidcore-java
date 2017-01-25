package com.uniquid.register.user;

import java.util.List;

import com.uniquid.register.RegisterException;

public interface UserRegister {

	public List<UserChannel> getAllUserChannels() throws RegisterException;
	
	public UserChannel getChannelByName(String name) throws RegisterException;
	
	public UserChannel getChannelByProviderAddress(String name) throws RegisterException;
	
	public void insertChannel(UserChannel userChannel) throws RegisterException;
	
	public void deleteChannel(UserChannel userChannel) throws RegisterException;
	
}