/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node;

import com.uniquid.messages.FunctionRequestMessage;
import com.uniquid.messages.FunctionResponseMessage;
import com.uniquid.node.exception.NodeException;
import com.uniquid.node.listeners.UniquidNodeEventListener;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.user.UserChannel;
import org.bitcoinj.core.Transaction;

import java.util.List;

/**
 * Represents an entity that owns an ID-based cryptography that uses the BlockChain to manage trust relationship with
 * other entities. It is capable to open a connection to the peer to peer network of the BlockChain and:
 * <ul>
 *  <li>synchronize/update the local BlockChain files</li>
 *  <li>listen, verify and re-broadcast valid Transactions inside the peer to peer network</li>
 * </ul>
 */
public interface UniquidNode {

    /**
     * Returns the imprinting address of this node
     *
     * @return imprinting address of this node
     */
    String getImprintingAddress();

    /**
     * Returns the public key of this node
     *
     * @return public key of this node
     */
    String getPublicKey();

    /**
     * Returns the node name
     *
     * @return the name of this node
     */
    String getNodeName();

    /**
     * Returns the creation time of this node
     *
     * @return creation time of this node
     */
    long getCreationTime();

    /**
     * Return the spendable balance of this node
     *
     * @return the spendable balance of this node
     */
    String getSpendableBalance();

    /**
     * Initialize this node
     */
    void initNode() throws NodeException;

    /**
     * Synchronize the node against the BlockChain.
     */
    void updateNode() throws NodeException;

    /**
     * Returns the current state of this node.
     *
     * @return the {@code UniquidNodeState} representing the current state of this node.
     */
    UniquidNodeState getNodeState();

    /**
     * Register an event listener
     *
     * @param uniquidNodeEventListener the event listener that will receive callbacks
     */
    void addUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener);

    /**
     * Unregister an event listener
     *
     * @param uniquidNodeEventListener the event listener that will be removed
     */
    void removeUniquidNodeEventListener(final UniquidNodeEventListener uniquidNodeEventListener);

    /**
     * Allow to sign an unsigned serialized blockchain transaction.
     *
     * @param tx the unsigned transaction to sign
     * @param paths the bip32 path to use to sign
     * @return the serialized signed transaction
     * @throws NodeException in case a problem occurs.
     */
    Transaction signTransaction(final Transaction tx, final List<String> paths) throws NodeException;

    /**
     * Create transaction for current node
     *
     * @param serializedTx transaction in binary format
     * @return
     * @throws NodeException in case a problem occurs
     */
    Transaction createTransaction(final String serializedTx) throws NodeException;

    void recoverUnspent(final Transaction tx, final List<String> paths) throws NodeException;

    /**
     * Sign the input message with the key derived from path specified
     *
     * @param message the message to sign
     * @param path the path to use to derive HD key
     * @return the message signed with the derived HD key
     * @throws NodeException in case a problem occurs.
     */
    String signMessage(String message, String path) throws NodeException;

    /**
     * Generates a new address representing the derived key from the requested path
     *
     * @param path the path to use to derive the HD key
     * @return the address representing the derived key from the requested path
     */
    String getAddressAtPath(String path) throws NodeException;

    /**
     * Allow to propagate a serialized Tx on the peer2peer network
     * @param serializedTx transaction serialized
     * @return result of the broadcast
     * @throws NodeException in case a problem occurs
     */
    String broadCastTransaction(final String serializedTx) throws NodeException;

    /**
     * Create a new UniquidCapability
     * @param providerName name of the device
     * @param userPublicKey public key of the user
     * @param rights permissions to grant
     * @param since timestamp to start
     * @param until expiration timestamp
     * @return the capability
     * @throws NodeException in case a problem occurs
     */
    UniquidCapability createCapability(String providerName, String userPublicKey, byte[] rights,
                                       long since, long until)	throws NodeException;

    /**
     * Allow a node to receive a Capability generated by one of its owners
     * @param uniquidCapability capability to manage
     * @throws NodeException in case a problem occurs
     */
    void receiveProviderCapability(UniquidCapability uniquidCapability) throws NodeException;

    /**
     * Allow a user node to insert the Capability generated by an owner
     *
     * @param uniquidCapability capability to manage
     * @throws NodeException in case a problem occurs
     */
    void receiveUserCapability(UniquidCapability uniquidCapability, String providerName, String path) throws NodeException;

    boolean isNodeReady();

    /**
     * Return the ProviderChannel from the received {@code FunctionRequestMessage}
     * @param requestMessage the {@code FunctionRequestMessage} received
     * @return the {@code ProviderChannel} using the message sender
     * */
    ProviderChannel getProviderChannel(FunctionRequestMessage requestMessage) throws Exception;

    /**
     * Return the UserChannel from the received response message
     * @param responseMessage the {@code FunctionResponseMessage} received
     * @return the {@code UserChannel} using the the message sender
     * */
    UserChannel getUserChannel(FunctionResponseMessage responseMessage) throws Exception;
}