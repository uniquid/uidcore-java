package com.uniquid.core.impl.test;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

import java.util.ArrayList;
import java.util.List;

public class DummyUserRegister implements UserRegister {

    private ArrayList<UserChannel> channels = new ArrayList<>();

    @Override
    public void insertChannel(UserChannel userChannel) throws RegisterException {
        channels.add(userChannel);

    }

    @Override
    public UserChannel getUserChannelByRevokeTxId(String revokeTxId) throws RegisterException {

        for (UserChannel p : channels) {

            if (p.getRevokeTxId().equals(revokeTxId)) {
                return p;
            }

        }

        return null;
    }

    @Override
    public UserChannel getUserChannelByRevokeAddress(String revokeTxId) throws RegisterException {

        for (UserChannel p : channels) {

            if (p.getRevokeAddress().equals(revokeTxId)) {
                return p;
            }

        }

        return null;
    }

    @Override
    public UserChannel getChannelByProviderAddress(String name) throws RegisterException {

        for (UserChannel p : channels) {

            if (p.getProviderAddress().equals(name)) {
                return p;
            }

        }

        return null;
    }

    @Override
    public UserChannel getChannelByName(String name) throws RegisterException {

        for (UserChannel p : channels) {

            if (p.getProviderName().equals(name)) {
                return p;
            }

        }

        return null;
    }

    @Override
    public List<UserChannel> getAllUserChannels() throws RegisterException {
        return channels;
    }

    @Override
    public void deleteChannel(UserChannel userChannel) throws RegisterException {
        channels.remove(userChannel);
    }

}
