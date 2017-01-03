package com.uniquid.uniquid_core;

import static org.bitcoinj.core.Utils.HEX;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.uniquid.register.Channel;
import com.uniquid.register.Contract;
import com.uniquid.register.Node;
import com.uniquid.register.Register;
import com.uniquid.register.RegisterFactory;
import com.uniquid.register.UContext;
import com.uniquid.spv_node.SpvNode;
import com.uniquid.uniquid_core.message.MessageRequest;
import com.uniquid.uniquid_core.message.MessageResponse;
import com.uniquid.uniquid_core.message.MessageService;
import com.uniquid.uniquid_core.message.MessageServiceFactory;

/**
 * This is the core of Uniquid library. It contains all the functionalities
 * needed by the machines to send and receive request.
 */

public final class Core {

	private static final Logger LOGGER = LoggerFactory.getLogger(Core.class.getName());

	public static final int RESULT_OK = 0;
	public static final int RESULT_NO_PERMISSION = 2;
	public static final int RESULT_NO_FUNCTION = 3;

	private RegisterFactory registerFactory;
	private MessageServiceFactory messageServiceFactory;
	private SpvNode spvNode;
	
	private Thread thread;

	private final Map<Integer, Function> functionsMap = new HashMap<>();
	private final Map<Integer, FunctionFilter> filtersMap = new HashMap<>();

	public Core(RegisterFactory registerFactory, MessageServiceFactory messageServiceFactory, SpvNode spvNode) {

		Function[] coreFunctions = new Function[] {

				new Function() {

					@Override
					public void performFunction(MessageRequest request, StringBuilder response) throws Exception {

						// intent.putExtra("txid", request.getParams());
						//// broadcastManager.sendBroadcast(intent);
					}

					@Override
					public int getValue() {
						return 0;
					}

					@Override
					public String getName() {
						return "CONTRACT";
					}
					
				},
				
				new Function() {

					@Override
					public void performFunction(MessageRequest request, StringBuilder response) throws Exception {
						Gson gson = new GsonBuilder().create();

						LOGGER.info("UNODE " + request.getParams());
						Register registerNode = registerFactory.createRegister();
						List<Node> nodes = (List<Node>) registerNode.getRegistry(Node.class, (String) request.getParams());
						JsonArray res = gson.toJsonTree(nodes).getAsJsonArray();
						LOGGER.info("UNODES", res.toString());
						response.append(res.toString());
					}

					@Override
					public int getValue() {
						return 4;
					}

					@Override
					public String getName() {
						return "UNODE";
					}
					
				}, new Function() {

					@Override
					public void performFunction(MessageRequest request, StringBuilder response) throws Exception {
						Gson gson = new GsonBuilder().create();

						LOGGER.info("UCONTEXT", request.getParams());
						Register register = registerFactory.createRegister();
						List<UContext> uContext = (List<UContext>) register.getRegistry(UContext.class, null);
						JsonArray resp = gson.toJsonTree(uContext).getAsJsonArray();
						response.append(resp.toString());
					}

					@Override
					public int getValue() {
						return 5;
					}

					@Override
					public String getName() {
						return "UCONTEXT";
					}
				},

				new Function() {

					@Override
					public void performFunction(MessageRequest request, StringBuilder response) throws Exception {
						Gson gson = new GsonBuilder().create();

						LOGGER.info("UCONTRACT", request.getParams());
						Register registerContract = registerFactory.createRegister();
						List<Contract> contracts = (List<Contract>) registerContract.getRegistry(Contract.class,
								(String) request.getParams());
						JsonArray respo = gson.toJsonTree(contracts).getAsJsonArray();
						response.append(respo.toString());
					}

					@Override
					public int getValue() {
						return 6;
					}

					@Override
					public String getName() {
						return "UCONTRACT";
					}
					
				},
				
				new Function() {
					
					@Override
					public void performFunction(MessageRequest request, StringBuilder response) throws Exception {
						Gson gson = new GsonBuilder().create();

						LOGGER.info("UID_signAndSendContract: " + request.getParams());
						
						JSONObject jMessage = new JSONObject(request.getParams());
						
						JSONArray paths = jMessage.getJSONArray("paths");
						
						String pathToSign = paths.getString(0);
						String pathToSend = paths.getString(1);
						
						//Check max path to be <3
						
						String rawtx = jMessage.getString("tx");
						
						
						byte[] sigProgBytes = HEX.decode(rawtx);
						
						Register registerContract = registerFactory.createRegister();
						List<Contract> contracts = (List<Contract>) registerContract.getRegistry(Contract.class,
								(String) request.getParams());
						JsonArray respo = gson.toJsonTree(contracts).getAsJsonArray();
						response.append(respo.toString());
					}
					
					@Override
					public int getValue() {
						return 30;
					}
					
					@Override
					public String getName() {
						return "UID_signAndSendContract";
					}
				},
				
				new Function() {

					@Override
					public void performFunction(MessageRequest request, StringBuilder response) {
						response.append("UID_echo: " + request.getParams());
					}

					@Override
					public int getValue() {
						return 31;
					}

					@Override
					public String getName() {
						return "UID_echo";
					}
				}

		};

		for (Function function : coreFunctions) {

			functionsMap.put(function.getValue(), function);

		}
		
		this.registerFactory = registerFactory;
		this.messageServiceFactory = messageServiceFactory;
		this.spvNode = spvNode;

	}

	public Function getFunction(int value) {

		return functionsMap.get(value);

	}
	
	public FunctionFilter getFilter(int value) {

		return filtersMap.get(value);

	}

	public void registerFunction(Function function) {

		if (function.getValue() >= 32) {

			functionsMap.put(function.getValue(), function);

		}

	}
	
	public void registerFilter(int functionNumber, FunctionFilter functionFilter) {

//		if (functionNumber >= 0 && functionNumber <= 31) {

			filtersMap.put(functionNumber, functionFilter);

//		}

	}

	/**
	 * Create a communication channel between two machines
	 * 
	 * @param name
	 *            the name of the other machine
	 * @param channel
	 * @return
	 * @throws ClassNotFoundException
	 */
	public int createChannel(String name, Channel channel) throws Exception {
		Register register = registerFactory.createRegister();
		register.getChannel(name);
		return 0;
	}


	/**
	 * Execute a command in range 0-31
	 * 
	 * @param request
	 *            message parsed in {@link #parseReqMessage}
	 * @param response
	 *            object to fill with the execution result
	 * @throws ClassNotFoundException
	 */
	public int performRequest(MessageRequest request, StringBuilder response) {

		Function function = getFunction(request.getMethod());

		if (function != null) {

			try {
				
				// call filter if exists
				FunctionFilter functionFilter = getFilter(request.getMethod());
				
				if (functionFilter != null) {
				
					functionFilter.doFilter(request);
				
				}

				function.performFunction(request, response);
				return RESULT_OK;

			} catch (Exception ex) {

				LOGGER.error("Exception", ex);

			}

		} else {

			response.append("Function not implemented");
			return RESULT_NO_FUNCTION;

		}

		return RESULT_OK;

	}

	/**
	 * Parse a message of type "response"
	 * 
	 * @param message
	 *            the message to parse
	 * @param messageResponse
	 *            the message parsed
	 * @return 0 if no errors, an errore code otherwise
	 */
//	public int parseRespMessage(byte[] message, MessageResponse messageResponse) {
//		String m = new String(message, StandardCharsets.UTF_8);
//		try {
//			JSONObject jMessage = new JSONObject(m);
//			String address = jMessage.getString("sender");
//			messageResponse.setSender(address);
//			LOGGER.info("MESSAGE_UTILS", address);
//			String body = jMessage.getString("body");
//			if (isRpcResp(body)) {
//				LOGGER.info("MESSAGE", "is a jsonRpc");
//				JSONObject jsonObject = new JSONObject(body);
//				messageResponse.setResult(jsonObject.getString("result"));
//				messageResponse.setError(jsonObject.getInt("error"));
//				messageResponse.setMsg_id(jsonObject.getLong("id"));
//			} else {
//				return -1;
//			}
//		} catch (JSONException ex) {
//
//			LOGGER.error("Exception catched during JSON manipulation", ex);
//			return -1;
//
//		}
//		return 0;
//	}

	/**
	 * Check if exist a contract between this node and the requester
	 * 
	 * @param address
	 *            the address of the sender client
	 * @throws ClassNotFoundException
	 */
	public Channel checkSender(String address) throws Exception {
		/*
		 * do we need to create two separate TABLE for client/provider side ?
		 * p_name | p_address | c_address c_address | p_address | op_return
		 */

		Register register = registerFactory.createRegister();
		return register.getChannelByAddress(address);
	}

	// TODO
	/*
	 * Check if the sender have the right permissions to execute the request
	 */
	public int checkPermission(String contract) {
		return 0;
	}

	/**
	 * Create a MessageResponse object
	 * 
	 * @param channel
	 *            channel
	 * @param method
	 *            method to perform
	 * @param params
	 *            parameters to pass to the {@param method}
	 * @param messageRequest
	 *            object to fill with datas
	 */
	public int formatReqMessage(Channel channel, int method, String params, MessageRequest messageRequest) {
		messageRequest.setSender(channel.getClientAddress());
		messageRequest.setMethod(method);
		messageRequest.setParams(params);
		messageRequest.setId(100);
		return 0;
	}

	/**
	 * Create a MessageResponse object
	 * 
	 * @param response
	 *            response of an execution
	 * @param error
	 *            flow error
	 * @param msg_id
	 *            id of received message
	 * @param messageResponse
	 *            object to fill with datas
	 */
	public int formatRespMessage(StringBuilder response, int error, int msg_id, MessageResponse messageResponse) {
		messageResponse.setResult(response.toString());
		messageResponse.setError(String.valueOf(error));
		messageResponse.setId(msg_id);
		return 0;
	}


	/**
	 * Check if {@param json} is a JSON-RPC response
	 * 
	 * @return true if the string a rpc response, false otherwise
	 */
	public boolean isRpcResp(String json) {
		try {
			JSONObject jobj = new JSONObject(json);
			return jobj.has("result") && jobj.has("error") && jobj.has("id");
		} catch (JSONException e) {
			return false;
		}
	}
	
	public void initialize() {
		
		// Init node
		spvNode.startNode();
		
		// Create a thread to wait for messages
		thread = new Thread() {
			
			@Override
			public void run() {
				
				// until not interrupted
				while (!Thread.currentThread().isInterrupted()) {
				
					try {

						MessageService messageService = messageServiceFactory.createMessageService();
						
						// this will block until a message is received
						MessageRequest messageRequest = messageService.receiveRequest();
						
						// message received
						//if (checkSender(messageRequest.getSender()) != null) {
							
		                		StringBuilder result = new StringBuilder();
		                		
		                    int error = performRequest(messageRequest, result);
	
		                    MessageResponse messageResponse = new MessageResponse();
		                    
		                    formatRespMessage(result, error, messageRequest.getId(), messageResponse);
		                    
		                    messageResponse.setSender(spvNode.getWallet().currentReceiveAddress().toBase58());
		                    
		                    messageService.sendResponse(messageResponse);
		                    
		            		//}
						
					} catch (Exception ex) {
						
						LOGGER.error("Exception catched", ex);

					}
				
				}
				
			}
			
		};
		
		// Start thread
		thread.start();
		
	}
	
	public void shutdown() {
		
		thread.interrupt();
		
		spvNode.stopNode();
	}

}
