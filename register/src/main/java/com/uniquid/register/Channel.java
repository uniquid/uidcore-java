package com.uniquid.register;

public class Channel {

    String name;
    String providerAddress;
    String clientAddress;

    public Channel(){

    }

    public Channel(String name, String providerAddress, String clientAddress){
        this.name = name;
        this.providerAddress = providerAddress;
        this.clientAddress = clientAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProviderAddress(String providerAddress) {
        this.providerAddress = providerAddress;
    }

    public String getProviderAddress() {
        return providerAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getClientAddress() {
        return clientAddress;
    }

}
