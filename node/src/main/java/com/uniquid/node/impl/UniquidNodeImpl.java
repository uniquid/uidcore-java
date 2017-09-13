package com.uniquid.node.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.LocalTransactionSigner;
import org.bitcoinj.signers.MissingSigResolutionSigner;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.SendRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import com.google.common.collect.ImmutableList;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.impl.utils.NodeUtils;
import com.uniquid.node.impl.utils.WalletUtils;

/**
 * Implementation of an Uniquid Node with BitcoinJ library
 */
public class UniquidNodeImpl extends UniquidWatchingNodeImpl<UniquidNodeConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniquidNodeImpl.class.getName());

	private final DeterministicSeed deterministicSeed;

	/**
	 * Creates a new instance
	 * 
	 * @param uniquidNodeConfiguration
	 * @throws NodeException
	 */
	protected UniquidNodeImpl(UniquidNodeConfiguration uniquidNodeConfiguration, DeterministicSeed deterministicSeed) throws NodeException {

		super(uniquidNodeConfiguration);
		
		this.deterministicSeed = deterministicSeed;

	}

	/*
	 * 
	 * Begin of public part for implementing UniquidNode
	 *
	 */
	@Override
	public synchronized String getHexSeed() {
		return deterministicSeed.toHexString();
	}

	@Override
	public synchronized String signTransaction(final String s_tx, final String path) throws NodeException {

		try {
			ImmutableList<ChildNumber> list = listFromPath(path);

			Transaction originalTransaction = uniquidNodeConfiguration.getNetworkParameters().getDefaultSerializer()
					.makeTransaction(Hex.decode(s_tx));

			SendRequest req = SendRequest.forTx(originalTransaction);

			DeterministicKey deterministicKey = NodeUtils
					.createDeterministicKeyFromByteArray(deterministicSeed.getSeedBytes());

			DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

			DeterministicKey imprintingKey = deterministicHierarchy.get(list, true, true);

			Transaction tx = req.tx;

			KeyBag maybeDecryptingKeyBag = new UniquidKeyBag(imprintingKey);

			int numInputs = tx.getInputs().size();
			for (int i = 0; i < numInputs; i++) {
				TransactionInput txIn = tx.getInput(i);

				// Fetch input tx from wallet
				Transaction inputTransaction = providerWallet.getTransaction(txIn.getOutpoint().getHash());

				if (inputTransaction == null) {

					throw new NodeException("Input TX not found in wallet!");

				}

				TransactionOutput outputToUse = inputTransaction.getOutput(txIn.getOutpoint().getIndex());

				originalTransaction.getInput(0).connect(outputToUse);
				Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
				RedeemData redeemData = txIn.getConnectedRedeemData(maybeDecryptingKeyBag);
				txIn.setScriptSig(scriptPubKey.createEmptyInputScript(redeemData.keys.get(0), redeemData.redeemScript));
			}

			TransactionSigner.ProposedTransaction proposal = new TransactionSigner.ProposedTransaction(tx);
			TransactionSigner signer = new LocalTransactionSigner();
			if (!signer.signInputs(proposal, maybeDecryptingKeyBag)) {
				LOGGER.info("{} returned false for the tx", signer.getClass().getName());
				throw new NodeException("Cannot sign TX!");
			}

			// resolve missing sigs if any
			new MissingSigResolutionSigner(req.missingSigsMode).signInputs(proposal, maybeDecryptingKeyBag);

			return Hex.toHexString(originalTransaction.bitcoinSerialize());

		} catch (Exception ex) {

			throw new NodeException("Exce", ex);
		}
	}
	
	public synchronized String oldsignTransaction(final String s_tx, final String path) throws NodeException {

		LOGGER.info("Signing TX");
		LOGGER.trace("Signing TX {} at path {}", s_tx, path);

		try {
			Transaction originalTransaction = uniquidNodeConfiguration.getNetworkParameters().getDefaultSerializer()
					.makeTransaction(Hex.decode(s_tx));

			String transactionToString = Hex.toHexString(originalTransaction.bitcoinSerialize());
			LOGGER.trace("Serialized unsigned transaction: " + transactionToString);

			SendRequest send = SendRequest.forTx(originalTransaction);

			if (path.startsWith("0")) {

				// fix our tx
				WalletUtils.newCompleteTransaction(send, providerWallet,
						uniquidNodeConfiguration.getNetworkParameters());

				// delegate to walled the signing
				providerWallet.signTransaction(send);

				return Hex.toHexString(originalTransaction.bitcoinSerialize());

			} else if (path.startsWith("1")) {

				// fix our tx
				WalletUtils.newCompleteTransaction(send, userWallet, uniquidNodeConfiguration.getNetworkParameters());

				// delegate to walled the signing
				userWallet.signTransaction(send);

				return Hex.toHexString(originalTransaction.bitcoinSerialize());

			} else {

				throw new NodeException("Unknown path");

			}

		} catch (Exception ex) {

			throw new NodeException("Exception while signing", ex);
		}
	}
	
	private static ImmutableList<ChildNumber> listFromPath(String path) {
			
			StringTokenizer tokenizer = new StringTokenizer(path, "/");
			
			List<ChildNumber> start = new ArrayList<ChildNumber>();
			
			start.add(new ChildNumber(44, true));
			start.add(new ChildNumber(0, true));
			start.add(new ChildNumber(0, false));
			
			while (tokenizer.hasMoreTokens()) {
				
				String next = tokenizer.nextToken();
				
				start.add(new ChildNumber(Integer.valueOf(next), false));
				
			}
			
			return ImmutableList.copyOf(start);
			
		}

	/**
	 * Builder for UniquidNodeImpl
	 */
	public static class UniquidNodeBuilder extends UniquidWatchingNodeImpl.WatchingNodeBuilder<UniquidNodeBuilder, UniquidNodeConfiguration, UniquidNodeConfigurationImpl> {
		
		/**
		 * Build a new instance
		 * 
		 * @return
		 * @throws Exception
		 */
		public UniquidNodeImpl build() throws NodeException {

			SecureRandom random = new SecureRandom();
			byte[] entropy = new byte[32];
			random.nextBytes(entropy);
			long creationTime = System.currentTimeMillis() / 1000;

			DeterministicSeed detSeed = new DeterministicSeed(entropy, "", creationTime);

			_uniquidNodeConfiguration.setCreationTime(creationTime);
			_uniquidNodeConfiguration.setPublicKey(deriveXpub(_uniquidNodeConfiguration.getNetworkParameters(), detSeed));

			return createUniquidNode(_uniquidNodeConfiguration, detSeed);

		}
		
		@Deprecated
		public UniquidNodeImpl buildFromHexSeed(final String hexSeed, final long creationTime) throws NodeException {

			try {
				
				DeterministicSeed detSeed = new DeterministicSeed("", org.bitcoinj.core.Utils.HEX.decode(hexSeed), "", creationTime);
	
				_uniquidNodeConfiguration.setCreationTime(creationTime);
				_uniquidNodeConfiguration.setPublicKey(deriveXpub(_uniquidNodeConfiguration.getNetworkParameters(), detSeed));
	
				return createUniquidNode(_uniquidNodeConfiguration, detSeed);
				
			} catch (Exception ex) {
				
				throw new NodeException("Exception while building node from hex seed", ex);
				
			}

		}

		public UniquidNodeImpl buildFromMnemonic(final String mnemonic, final long creationTime) throws NodeException {

			try {
				DeterministicSeed detSeed = new DeterministicSeed(mnemonic, null, "", creationTime);
	
				_uniquidNodeConfiguration.setCreationTime(creationTime);
				_uniquidNodeConfiguration.setPublicKey(deriveXpub(_uniquidNodeConfiguration.getNetworkParameters(), detSeed));
	
				return createUniquidNode(_uniquidNodeConfiguration, detSeed);
			
			} catch (Exception ex) {
				
				throw new NodeException("Exception while building node from mnemonic", ex);
				
			}

		}

		protected UniquidNodeImpl createUniquidNode(UniquidNodeConfiguration uniquidNodeConfiguration, DeterministicSeed deterministicSeed) throws NodeException {
			return new UniquidNodeImpl(uniquidNodeConfiguration, deterministicSeed);
		}
		
		private static final String deriveXpub(NetworkParameters networkParameters, DeterministicSeed detSeed) {
			
			DeterministicKey deterministicKey = NodeUtils.createDeterministicKeyFromByteArray(detSeed.getSeedBytes());

			DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(deterministicKey);

			ImmutableList<ChildNumber> IMPRINTING_PATH = ImmutableList.of(new ChildNumber(44, true),
					new ChildNumber(0, true));

			DeterministicKey imprintingKey = deterministicHierarchy.get(IMPRINTING_PATH, true, true);

			return imprintingKey.serializePubB58(networkParameters);
			
		}

	}

}