/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl.contract;

import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.script.ScriptPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.uniquid.node.impl.utils.NodeUtils.getAddressFromTransactionOutput;

/**
 * Class that manage imprinting contracts
 *
 * @author giuseppe
 *
 */
@SuppressWarnings("rawtypes")
public class ImprintingContract extends AbstractContract {

    public static final String CONTRACT_FUNCTION = "00000000400000000000000000000000000000";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImprintingContract.class);

    @SuppressWarnings("unchecked")
    public ImprintingContract(UniquidNodeStateContext uniquidNodeStateContext) {
        super(uniquidNodeStateContext);
    }

    @Override
    public void doRealContract(final Transaction tx) throws Exception {

        LOGGER.info("Making imprint contract from TX {}", tx.getHashAsString());

        LegacyAddress legacyAddress = LegacyAddress.fromPubKeyHash(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters(),
                org.bitcoinj.core.Utils.sha256hash160(ScriptPattern.extractHashFromPayToScriptHash(tx.getInput(0).getScriptSig())));

        // Retrieve sender
        String sender = legacyAddress.toBase58();

        // Check output
        List<TransactionOutput> transactionOutputs = tx.getOutputs();
        for (TransactionOutput to : transactionOutputs) {

            Address address = getAddressFromTransactionOutput(to, uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());
            if (address != null && address.equals(uniquidNodeStateContext.getImprintingAddress())) {

                // This is our imprinter!!!

                LOGGER.info("Received imprint contract from {}!", sender);

                ProviderRegister providerRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getProviderRegister();

                ECKey key = uniquidNodeStateContext.getProviderWallet().findKeyFromPubHash(uniquidNodeStateContext.getImprintingAddress().getHash());
                String path = null;
                if (key != null) {
                    path = ((DeterministicKey) key).getPathAsString();
                }

                // Create provider channel
                final ProviderChannel providerChannel = new ProviderChannel();
                providerChannel.setUserAddress(sender);
                providerChannel.setProviderAddress(uniquidNodeStateContext.getImprintingAddress().toBase58());
                providerChannel.setBitmask(CONTRACT_FUNCTION);
                providerChannel.setRevokeAddress("IMPRINTING");
                providerChannel.setRevokeTxId(tx.getHashAsString());
                providerChannel.setCreationTime(tx.getUpdateTime().getTime()/1000);
                providerChannel.setSince(0);
                providerChannel.setUntil(Long.MAX_VALUE);
                providerChannel.setPath(path);

                uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().startTransaction();

                try {

                    // persist channel
                    providerRegister.insertChannel(providerChannel);

                    uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().commitTransaction();

                } catch (RegisterException ex) {

                    LOGGER.error("Error while inserting channel", ex);

                    uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().rollbackTransaction();

                    // ReThrow
                    throw ex;

                }

                // send event
                uniquidNodeStateContext.getUniquidNodeEventService().onProviderContractCreated(providerChannel);

                break;

            }

        }

    }

    @Override
    public void revokeRealContract(final Transaction tx) throws Exception {
        // DO NOTHING
    }

}
