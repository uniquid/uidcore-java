package com.uniquid.uniquid_core;

public abstract class AbstractConnector implements Connector {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getServer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setServer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getPort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHost() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getHost() {
		// TODO Auto-generated method stub

	}
	
	protected abstract void accept();

}
