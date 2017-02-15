package com.uniquid.register.impl.android;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Beatrice Formai on 02/10/17
 *         for Uniquid Inc.
 */
public class Register implements UserRegister, ProviderRegister {

    private static SQLiteDatabase db;
    private SQLiteHelper dbHelper;

    public Register(android.content.Context context){
        dbHelper = new SQLiteHelper(context);
    }

    public List<UserChannel> getAllUserChannels(){
        db = dbHelper.getReadableDatabase();
        List<UserChannel> channels = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_USER, null);
        if(cursor.moveToFirst()){
            do{
                UserChannel userChannel = new UserChannel();
                userChannel.setProviderName(cursor.getString(0));
                userChannel.setProviderAddress(cursor.getString(1));
                userChannel.setUserAddress(cursor.getString(2));
                userChannel.setBitmask(cursor.getString(3));
                userChannel.setRevokeAddress(cursor.getString(4));
				userChannel.setRevokeTxId(cursor.getString(5));
                channels.add(userChannel);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return channels;
    }

    /**
     * Retrieve a Channel from its name
     * @param name the name of the other machine
     * @return new Channel
     * @throws RegisterException if there isn't record with the specified name
     * */
    public UserChannel getChannelByName(String name) throws RegisterException {
        UserChannel userChannel = new UserChannel();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_USER +
                " where " + SQLiteHelper.USER_CLM_PROVIDER_NAME + " = ?", new String[]{name});
        if(cursor.moveToFirst()){
            userChannel.setProviderName(cursor.getString(0));
            userChannel.setProviderAddress(cursor.getString(1));
            userChannel.setUserAddress(cursor.getString(2));
            userChannel.setBitmask(cursor.getString(3));
            userChannel.setRevokeAddress(cursor.getString(4));
			userChannel.setRevokeTxId(cursor.getString(5));
            cursor.close();
        } else {
            throw new RegisterException("Doesn't exist any record with specified name");
        }
        return userChannel;
    }

    public UserChannel getChannelByProviderAddress(String address) throws RegisterException {
        UserChannel userChannel = new UserChannel();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_USER +
                        " where " + SQLiteHelper.USER_CLM_PROVIDER_ADDRESS + " = ?",
                new String[]{address});
        if(cursor.moveToFirst()){
            userChannel.setProviderName(cursor.getString(0));
            userChannel.setProviderAddress(cursor.getString(1));
            userChannel.setUserAddress(cursor.getString(2));
            userChannel.setBitmask(cursor.getString(3));
            userChannel.setRevokeAddress(cursor.getString(4));
			userChannel.setRevokeTxId(cursor.getString(5));
            cursor.close();
        } else {
            throw new RegisterException("Doesn't exist any record with specified name");
        }
        return userChannel;
    }

    /**
     * Insert a new Channel in table
     * @param userChannel the channel to insert
     * @throws RegisterException if an error occurs
     * */
    public void insertChannel(UserChannel userChannel) throws RegisterException {
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.USER_CLM_PROVIDER_NAME, userChannel.getProviderName());
        values.put(SQLiteHelper.USER_CLM_PROVIDER_ADDRESS, userChannel.getProviderAddress());
        values.put(SQLiteHelper.USER_CLM_USER_ADDRESS, userChannel.getUserAddress());
        values.put(SQLiteHelper.USER_CLM_BITMASK, userChannel.getBitmask());
        values.put(SQLiteHelper.USER_CLM_REVOKE_ADDRESS, userChannel.getRevokeAddress());
        values.put(SQLiteHelper.USER_CLM_REVOKE_TX_ID, userChannel.getRevokeTxId());
        long db_index = db.insert(SQLiteHelper.TABLE_USER, null, values);
        if(db_index < 0)
            throw new RegisterException("Error inserting new channel");
    }

    /**
     * Delete a channel
     * @param userChannel the channel to delete from table
     * @throws RegisterException if the specified UserChannel doesn't exist into table
     * */
    public void deleteChannel(UserChannel userChannel) throws RegisterException {
        db = dbHelper.getWritableDatabase();
        int d = db.delete(SQLiteHelper.TABLE_USER, SQLiteHelper.USER_CLM_PROVIDER_NAME + " = ? " +
        		SQLiteHelper.USER_CLM_PROVIDER_ADDRESS + " = ? " + SQLiteHelper.USER_CLM_USER_ADDRESS + " = ?" ,
                new String[]{userChannel.getProviderName(), userChannel.getProviderAddress(), userChannel.getUserAddress()});
        if(d == 0)
            throw new RegisterException("Channel not present");
    }



    /************
     * PROVIDER **
     * **********/

    public List<ProviderChannel> getAllChannels(){
        List<ProviderChannel> channels = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_PROVIDER, null);
        if(cursor.moveToFirst()){
            do{
                ProviderChannel channel = new ProviderChannel();
                channel.setProviderAddress(cursor.getString(0));
                channel.setUserAddress(cursor.getString(1));
                channel.setBitmask(cursor.getString(2));
                channel.setRevokeAddress(cursor.getString(3));
                channel.setRevokeTxId(cursor.getString(4));
                channels.add(channel);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return channels;
    }

    /**
     * Retrieve Channel information
     * @param address the address of the user
     * @return a ProviderChannel object that involves the specified user
     * @throws RegisterException if there is any record with the specified user address
     * */
    public ProviderChannel getChannelByUserAddress(String address) throws RegisterException {
        ProviderChannel providerChannel = new ProviderChannel();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_PROVIDER +
                        " where " + SQLiteHelper.PROVIDER_CLM_USER_ADDRESS + " = ?",
                new String[]{address});
        if(cursor.moveToFirst()){
            providerChannel.setProviderAddress(cursor.getString(0));
            providerChannel.setUserAddress(cursor.getString(1));
            providerChannel.setBitmask(cursor.getString(2));
            providerChannel.setRevokeAddress(cursor.getString(3));
            providerChannel.setRevokeTxId(cursor.getString(4));
            cursor.close();
        } else {
            throw new RegisterException("Doesn't exist any record with specified name");
        }
        return providerChannel;
    }

    @Override
    public ProviderChannel getChannelByRevokeAddress(String address) throws RegisterException {
    		ProviderChannel providerChannel = new ProviderChannel();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_PROVIDER +
                        " where " + SQLiteHelper.PROVIDER_CLM_REVOKE_ADDRESS + " = ?",
                new String[]{address});
        if(cursor.moveToFirst()){
            providerChannel.setProviderAddress(cursor.getString(0));
            providerChannel.setUserAddress(cursor.getString(1));
            providerChannel.setBitmask(cursor.getString(2));
            providerChannel.setRevokeAddress(cursor.getString(3));
            providerChannel.setRevokeTxId(cursor.getString(4));
            cursor.close();
        } else {
            return null;
        }
        return providerChannel;
    }

    @Override
    public ProviderChannel getChannelByRevokeTxId(String revokeTxId) throws RegisterException {
    	ProviderChannel providerChannel = new ProviderChannel();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SQLiteHelper.TABLE_PROVIDER +
                        " where " + SQLiteHelper.PROVIDER_CLM_REVOKE_TX_ID + " = ?",
                new String[]{revokeTxId});
        if(cursor.moveToFirst()){
            providerChannel.setProviderAddress(cursor.getString(0));
            providerChannel.setUserAddress(cursor.getString(1));
            providerChannel.setBitmask(cursor.getString(2));
            providerChannel.setRevokeAddress(cursor.getString(3));
            providerChannel.setRevokeTxId(cursor.getString(4));
            cursor.close();
        } else {
            return null;
        }
        return providerChannel;
    }

    /**
     * Insert the specified ProviderChannel into table
     * @param providerChannel the channel to insert
     * @throws RegisterException if an error occurs
     * */
    public void insertChannel(ProviderChannel providerChannel) throws RegisterException{
        db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.PROVIDER_CLM_PROVIDER_ADDRESS, providerChannel.getProviderAddress());
        values.put(SQLiteHelper.PROVIDER_CLM_USER_ADDRESS, providerChannel.getUserAddress());
        values.put(SQLiteHelper.PROVIDER_CLM_BITMASK, providerChannel.getBitmask());
        values.put(SQLiteHelper.PROVIDER_CLM_REVOKE_ADDRESS, providerChannel.getRevokeAddress());
        values.put(SQLiteHelper.PROVIDER_CLM_REVOKE_TX_ID, providerChannel.getRevokeTxId());
        long db_index = db.insert(SQLiteHelper.TABLE_PROVIDER, null, values);
        if(db_index < 0)
            throw new RegisterException("Error inserting new channel");
    }

    /**
     * Delete the specified ProviderChannel from table
     * @param providerChannel the ProviderChannel to delete
     * @throws RegisterException if the specified ProviderChannel is not present into table
     * */
    public void deleteChannel(ProviderChannel providerChannel) throws RegisterException{
        db = dbHelper.getWritableDatabase();
        int d = db.delete(SQLiteHelper.TABLE_PROVIDER,
                SQLiteHelper.PROVIDER_CLM_PROVIDER_ADDRESS + " = ?",
                new String[]{providerChannel.getProviderAddress()});
        if(d == 0)
            throw new RegisterException("Channel not present");
    }

}
