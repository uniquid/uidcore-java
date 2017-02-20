package com.uniquid.register.impl.android.orchestrator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.impl.android.SQLiteHelper;
import com.uniquid.register.orchestrator.Context;
import com.uniquid.register.orchestrator.Contract;
import com.uniquid.register.orchestrator.ImprintedNode;
import com.uniquid.register.orchestrator.Node;
import com.uniquid.register.orchestrator.IOrchestratorRegister;

import static com.uniquid.register.impl.android.orchestrator.SQLiteOrchestratorHelper.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Beatrice Formai 
 * */
public class OrchestratorRegister implements IOrchestratorRegister {
	
	private static SQLiteDatabase db;
    private SQLiteOrchestratorHelper dbHelper;
    
    public OrchestratorRegister(android.content.Context context){
        dbHelper = new SQLiteOrchestratorHelper(context);
    }

	@Override
	public void insertContext(Context context) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CONTEXT_CLM_NAME, context.getName());
        values.put(CONTEXT_CLM_XPUB, context.getXpub());
        long db_index = db.insert(SQLiteOrchestratorHelper.TABLE_CONTEXT, null, values);
        if(db_index < 0)
            throw new RegisterException("Error inserting new Context");
	}

	@Override
	public void updateContext(Context context) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CONTEXT_CLM_NAME, context.getName());
        values.put(CONTEXT_CLM_XPUB, context.getXpub());
        int result = db.update(TABLE_CONTEXT, values, CONTEXT_CLM_XPUB + " = ?", new String[]{context.getXpub()});
        if(result < 0)
            throw new RegisterException("error while updating context " + context.getName());
	}

	@Override
	public void deleteContextByName(String name) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        int d = db.delete(TABLE_CONTEXT, CONTEXT_CLM_NAME + "where = ?", new String[]{name});
        if (d == 0)
            throw new RegisterException("There is no Context " + name + "to delete");
	}

	@Override
	public void deleteContextByXpub(String xpub) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        int d = db.delete(TABLE_CONTEXT, CONTEXT_CLM_XPUB + "where = ?", new String[]{xpub});
        if(d == 0)
            throw new RegisterException("There is no Context with this xpub");
	}

	@Override
	public List<Context> getAllContexts() {
		db = dbHelper.getReadableDatabase();
        List<Context> contexts = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from " + TABLE_CONTEXT, null);
        if(cursor.moveToFirst()){
            do{
                Context context = new Context();
                context.setName(cursor.getString(0));
                context.setXpub(cursor.getString(1));
                contexts.add(context);
            } while (cursor.moveToNext());

        }
        cursor.close();
        Collections.sort(contexts);
        return contexts;
	}

	@Override
	public Context getContextByName(String name) throws RegisterException {
		db = dbHelper.getReadableDatabase();
        Context context = new Context();
        Cursor cursor = db.rawQuery("select * from " + TABLE_CONTEXT + " where " +
                CONTEXT_CLM_NAME + " = ? ", new String[]{name});
        if(cursor.moveToFirst()){
            context.setName(cursor.getString(0));
            context.setXpub(cursor.getString(1));
        } else {
            throw new RegisterException("Context " + name + "does not exist into register");
        }
        cursor.close();
        return context;
	}

	@Override
	public Context getContextByXpub(String xpub) throws RegisterException {
		db = dbHelper.getReadableDatabase();
        Context context = new Context();
        Cursor cursor = db.rawQuery("select * from " + TABLE_CONTEXT +
                " where " + CONTEXT_CLM_XPUB + " = ? ", new String[]{xpub});
        if(cursor.moveToFirst()){
            context.setName(cursor.getString(0));
            context.setXpub(cursor.getString(1));
        } else {
            throw new RegisterException("Context " + xpub + "does not exist into register");
        }
        cursor.close();
        return context;
	}

	@Override
	public void insertNode(Node node) throws RegisterException {
		db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            boolean exist;

            try{
                getNodeByXpub(node.getXpub());
                exist = true;
            } catch (RegisterException exception){
                exist = false;
            }

            if(!exist){
                ContentValues values = new ContentValues();
                values.put(NODES_CLM_NAME, node.getName());
                values.put(NODES_CLM_XPUB, node.getXpub());
                values.put(NODES_CLM_TS, node.getTimestamp());
                values.put(NODES_CLM_RECIPE, node.getRecipe());
                values.put(NODES_CLM_PATH, node.getPath());
                long index = db.insert(TABLE_NODES, null, values);
                if(index == -1){
                    throw new RegisterException("error inserting node");
                }
            } else {
                throw new RegisterException("node already present");
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
	}

	@Override
	public void deleteNode(Node node) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        int d = db.delete(TABLE_NODES, NODES_CLM_NAME + " = ?", new String[]{node.getName()});
        if(d == 0)
            throw new RegisterException("There is no Node " + node.getName() + " to delete");
	}

	@Override
	public List<Node> getAllNodes() {
		List<Node> nodes = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NODES, null);
        if(cursor.moveToFirst()){
            do{
                Node node = new Node();
                node.setName(cursor.getString(0));
                node.setXpub(cursor.getString(1));
                node.setTimestamp(cursor.getLong(2));
                node.setRecipe(cursor.getString(3));
                node.setPath(cursor.getString(4));

                nodes.add(node);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return nodes;
	}

	@Override
	public Node getNodeByXpub(String xpub) throws RegisterException {
		db = dbHelper.getReadableDatabase();
        Node node = new Node();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NODES + " where " + NODES_CLM_XPUB + " = ?",
                new String[]{xpub});
        if(cursor.moveToFirst()){
            node.setName(cursor.getString(0));
            node.setXpub(cursor.getString(1));
            node.setTimestamp(cursor.getLong(2));
            node.setRecipe(cursor.getString(3));
            node.setPath(cursor.getString(4));
        } else {
            throw new RegisterException("xpub not found");
        }
        cursor.close();
        return node;
	}

	@Override
	public Node getNodeByName(String name) throws RegisterException {
		db = dbHelper.getReadableDatabase();
        Node node = new Node();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NODES + " where " + NODES_CLM_NAME + " = ?",
                new String[]{name});
        if(cursor.moveToFirst()){
            node.setName(cursor.getString(0));
            node.setXpub(cursor.getString(1));
            node.setTimestamp(cursor.getLong(2));
            node.setRecipe(cursor.getString(3));
            node.setPath(cursor.getString(4));
        } else {
            throw new RegisterException("xpub not found");
        }
        cursor.close();
        return node;
	}

	@Override
	public List<Node> getNodesByContext(String context_name) throws RegisterException {
		db = dbHelper.getReadableDatabase();
        List<Node> nodes = new ArrayList<>();

        Set<Node> set = new HashSet<>();

        String query = "SELECT nodes.* " +
                "FROM nodes " +
                "WHERE nodes.xpub in (" +
                "SELECT contracts.user_id " +
                "FROM contexts " +
                "INNER JOIN contracts " +
                "ON contexts.xpub=contracts.context_id " +
                "WHERE contexts.name = ? " +
                "UNION " +
                "SELECT contracts.provider_id " +
                "FROM contexts " +
                "INNER JOIN contracts " +
                "ON contexts.xpub=contracts.context_id " +
                "WHERE contexts.name = ?" +
                ")";

        Cursor cursor = db.rawQuery(query, new String[]{context_name, context_name});
        if(cursor.moveToFirst()){
            do {
                Node node = new Node();
                node.setName(cursor.getString(0));
                node.setXpub(cursor.getString(1));
                node.setTimestamp(cursor.getLong(2));
                node.setRecipe(cursor.getString(3));
                node.setPath(cursor.getString(4));
                set.add(node);
            } while (cursor.moveToNext());
        }

        nodes.addAll(set);
        Collections.sort(nodes);
        return nodes;
	}

	@Override
	public void updateNode(Node node) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NODES_CLM_NAME, node.getName());
        values.put(NODES_CLM_XPUB, node.getXpub());
        values.put(NODES_CLM_TS, node.getTimestamp());
        values.put(NODES_CLM_RECIPE, node.getRecipe());
        values.put(NODES_CLM_PATH, node.getPath());
        int result = db.update(TABLE_CONTEXT, values, CONTEXT_CLM_XPUB + " = ?", new String[]{node.getXpub()});
        if(result < 0)
            throw new RegisterException("error while updating node " + node.getName());
	}

	@Override
	public List<ImprintedNode> getAllImprinted() {
		List<ImprintedNode> nodes = new ArrayList<>();
        db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_IMPRINTED, null);
        if(cursor.moveToFirst()){
            do{
                ImprintedNode node = new ImprintedNode();
                node.setXpub(cursor.getString(0));
                node.setName(cursor.getString(1));
                node.setOwner(cursor.getString(2));
                node.setTxid(cursor.getString(3));
                nodes.add(node);
            } while (cursor.moveToNext());
        }
        Collections.sort(nodes);
        return nodes;
	}

	@Override
	public ImprintedNode getImprintedByXpub(String xpub) throws RegisterException {
		db = dbHelper.getReadableDatabase();
        ImprintedNode node = new ImprintedNode();
        Cursor cursor = db.rawQuery("select * from " + TABLE_IMPRINTED + " where " +
                IMPRINTED_CLM_XPUB + " = ?",
                new String[]{xpub});
        if(cursor.moveToFirst()){
            node.setXpub(cursor.getString(0));
            node.setName(cursor.getString(1));
            node.setOwner(cursor.getString(2));
            node.setTxid(cursor.getString(3));
        } else {
            throw new RegisterException("xpub not found");
        }
        cursor.close();
        return node;
	}

	@Override
	public void insertImprinted(ImprintedNode node) throws RegisterException {
		db = dbHelper.getWritableDatabase();

        try {
            db.beginTransaction();

            boolean exist;

            try{
                getNodeByXpub(node.getXpub());
                exist = true;
            } catch (RegisterException exception){
                exist = false;
            }

            if(!exist){
                ContentValues values = new ContentValues();
                values.put(IMPRINTED_CLM_XPUB, node.getXpub());
                values.put(IMPRINTED_CLM_NAME, node.getName());
                values.put(IMPRINTED_CLM_OWNER, node.getOwner());
                values.put(IMPRINTED_CLM_TXID, node.getTxid());

                long index = db.insert(TABLE_IMPRINTED, null, values);
                if(index == -1){
                    throw new RegisterException("error inserting node");
                }
            } else {
                throw new RegisterException("node already present");
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
	}

	@Override
	public void deleteImprinted(ImprintedNode node) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        int d = db.delete(TABLE_IMPRINTED, IMPRINTED_CLM_XPUB + " = ?", new String[]{node.getXpub()});
        if(d == 0)
            throw new RegisterException("There is no Node " + node.getName() + " to delete");
	}

	@Override
	public void insertContract(Contract contract) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        getContextByXpub(contract.getContext().getXpub());
        Node user = getNodeByXpub(contract.getUser().getXpub());
        Node provider = getNodeByXpub(contract.getProvider().getXpub());

        ContentValues values = new ContentValues();
        values.put(CONTRACT_CLM_CONTEXT, contract.getContext().getXpub());
        values.put(CONTRACT_CLM_USER, user.getXpub());
        values.put(CONTRACT_CLM_PROVIDER, provider.getXpub());
        values.put(CONTRACT_CLM_TS_BORN, contract.getTimestamp_born());
        values.put(CONTRACT_CLM_TS_EXPIRATION, contract.getTimestamp_expiration());
        values.put(CONTRACT_CLM_RECIPE, contract.getRecipe());
        values.put(CONTRACT_CLM_TXID, contract.getTxid());
        values.put(CONTRACT_CLM_ANNULMENT, contract.getAnnulment());
        values.put(CONTRACT_CLM_REVOCATED, contract.isRevocated());
        long db_index = db.insert(TABLE_CONTRACTS, null, values);
        if(db_index < 0)
            throw new RegisterException("error inserting new contract");
	}

	@Override
	public void deleteContract(Contract contract) throws RegisterException {
		db = dbHelper.getWritableDatabase();
        int d = db.delete(TABLE_CONTRACTS, CONTRACT_CLM_TXID + " = ?", new String[]{contract.getTxid()});
        if(d == 0)
            throw new RegisterException("The specified contract doesn't exist");
	}

	@Override
	public List<Contract> getAllContracts() {
		db = dbHelper.getReadableDatabase();
        List<Contract> contracts = new ArrayList<>();

        String query = "SELECT " + TABLE_CONTRACTS + ".*, " + TABLE_NODES + ".*, n2.*, " + TABLE_CONTEXT + ".* " +
                " FROM " + TABLE_CONTEXT +
                " INNER JOIN " + TABLE_CONTRACTS +
                " ON " + TABLE_CONTEXT + "." + CONTEXT_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_CONTEXT +
                " INNER JOIN " + TABLE_NODES +
                " ON " + TABLE_NODES + "." + NODES_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_USER +
                " INNER JOIN " + TABLE_NODES + " n2 " +
                " ON n2." + NODES_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_PROVIDER;

        Cursor cursor = db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                Node user = new Node();
                user.setName(cursor.getString(9));
                user.setXpub(cursor.getString(10));
                user.setTimestamp(cursor.getLong(11));
                user.setRecipe(cursor.getString(12));
                user.setPath(cursor.getString(13));

                Node provider = new Node();
                provider.setName(cursor.getString(14));
                provider.setXpub(cursor.getString(15));
                provider.setTimestamp(cursor.getLong(16));
                provider.setRecipe(cursor.getString(17));
                provider.setPath(cursor.getString(18));

                Context context = new Context();
                context.setName(cursor.getString(19));
                context.setXpub(cursor.getString(20));

                Contract contract = new Contract();
                contract.setContext(context);
                contract.setUser(user);
                contract.setProvider(provider);

                contract.setTimestamp_born(cursor.getLong(3));
                contract.setTimestamp_expiration(cursor.getLong(4));
                contract.setRecipe(cursor.getString(5));
                contract.setTxid(cursor.getString(6));
                contract.setAnnulment(cursor.getString(7));
                contract.setRevocated(cursor.getInt(8) == 1);
                contracts.add(contract);

            } while (cursor.moveToNext());
        }
        cursor.close();
        return contracts;
	}

	@Override
	public List<Contract> getContractsByContextName(String context_name) {
		List<Contract> contracts = new ArrayList<>();

        db = dbHelper.getReadableDatabase();

        String query = "SELECT " + TABLE_CONTRACTS + ".*, " + TABLE_NODES + ".*, n2.*, " + TABLE_CONTEXT + ".* " +
                " FROM " + TABLE_CONTEXT +
                " INNER JOIN " + TABLE_CONTRACTS +
                " ON " + TABLE_CONTEXT + "." + CONTEXT_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_CONTEXT +
                " INNER JOIN " + TABLE_NODES +
                " ON " + TABLE_NODES + "." + NODES_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_USER +
                " INNER JOIN " + TABLE_NODES + " n2 " +
                " ON n2." + NODES_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_PROVIDER +
                " WHERE " + TABLE_CONTEXT + "." + CONTEXT_CLM_NAME + " = ?";

        Cursor cursor = db.rawQuery(query,
                new String[]{context_name});
        if(cursor.moveToFirst()){
            do {
                Node user = new Node();
                user.setName(cursor.getString(9));
                user.setXpub(cursor.getString(10));
                user.setTimestamp(cursor.getLong(11));
                user.setRecipe(cursor.getString(12));
                user.setPath(cursor.getString(13));

                Node provider = new Node();
                provider.setName(cursor.getString(14));
                provider.setXpub(cursor.getString(15));
                provider.setTimestamp(cursor.getLong(16));
                provider.setRecipe(cursor.getString(17));
                provider.setPath(cursor.getString(18));

                Context context = new Context();
                context.setName(cursor.getString(19));
                context.setXpub(cursor.getString(20));

                Contract contract = new Contract();
                contract.setContext(context);
                contract.setUser(user);
                contract.setProvider(provider);

                contract.setTimestamp_born(cursor.getLong(3));
                contract.setTimestamp_expiration(cursor.getLong(4));
                contract.setRecipe(cursor.getString(5));
                contract.setTxid(cursor.getString(6));
                contract.setAnnulment(cursor.getString(7));
                contract.setRevocated(cursor.getInt(8) == 1);
                contracts.add(contract);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contracts;
	}

	@Override
	public Contract getContractByTxid(String txid) {
		db = dbHelper.getReadableDatabase();
        Contract contract = new Contract();

        String query = "SELECT " + TABLE_CONTRACTS + ".*, " + TABLE_NODES + ".*, n2.*, " + TABLE_CONTEXT + ".* " +
                " FROM " + TABLE_CONTEXT +
                " INNER JOIN " + TABLE_CONTRACTS +
                " ON " + TABLE_CONTEXT + "." + CONTEXT_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_CONTEXT +
                " INNER JOIN " + TABLE_NODES +
                " ON " + TABLE_NODES + "." + NODES_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_USER +
                " INNER JOIN " + TABLE_NODES + " n2 " +
                " ON n2." + NODES_CLM_XPUB + "=" + TABLE_CONTRACTS + "." + CONTRACT_CLM_PROVIDER +
                " WHERE " + TABLE_CONTRACTS + "." + CONTRACT_CLM_TXID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{txid});

        if(cursor.moveToFirst()){
            Node user = new Node();
            user.setName(cursor.getString(9));
            user.setXpub(cursor.getString(10));
            user.setTimestamp(cursor.getLong(11));
            user.setRecipe(cursor.getString(12));
            user.setPath(cursor.getString(13));

            Node provider = new Node();
            provider.setName(cursor.getString(14));
            provider.setXpub(cursor.getString(15));
            provider.setTimestamp(cursor.getLong(16));
            provider.setRecipe(cursor.getString(17));
            provider.setPath(cursor.getString(18));

            Context context = new Context();
            context.setName(cursor.getString(19));
            context.setXpub(cursor.getString(20));

            contract.setContext(context);
            contract.setUser(user);
            contract.setProvider(provider);

            contract.setTimestamp_born(cursor.getLong(3));
            contract.setTimestamp_expiration(cursor.getLong(4));
            contract.setRecipe(cursor.getString(5));
            contract.setTxid(cursor.getString(6));
            contract.setAnnulment(cursor.getString(7));
            contract.setRevocated(cursor.getInt(8) == 1);
        }
        cursor.close();
        return contract;
	}

	@Override
	public void updateContract(String txid) throws RegisterException {
		db = dbHelper.getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_CONTRACTS + " WHERE " +
                CONTRACT_CLM_TXID + " = ? ";

        Cursor cursor = db.rawQuery(query, new String[]{txid});
        if(cursor.moveToFirst()){
            ContentValues values = new ContentValues();
            values.put(CONTRACT_CLM_CONTEXT, cursor.getString(0));
            values.put(CONTRACT_CLM_USER, cursor.getString(1));
            values.put(CONTRACT_CLM_PROVIDER, cursor.getString(2));
            values.put(CONTRACT_CLM_TS_BORN, cursor.getLong(3));
            values.put(CONTRACT_CLM_TS_EXPIRATION, cursor.getLong(4));
            values.put(CONTRACT_CLM_RECIPE, cursor.getString(5));
            values.put(CONTRACT_CLM_TXID, cursor.getString(6));
            values.put(CONTRACT_CLM_ANNULMENT, cursor.getString(7));
            values.put(CONTRACT_CLM_REVOCATED, 1);

            int result = db.update(TABLE_CONTRACTS, values, CONTRACT_CLM_TXID + " = ?", new String[]{txid});
            if(result < 0)
                throw new RegisterException("error while updating contract");
        } else {
            throw new RegisterException("there is no contract with the specified txid");
        }
    }

}
