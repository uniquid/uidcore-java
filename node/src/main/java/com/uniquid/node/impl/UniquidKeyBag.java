/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.impl;

        import org.bitcoinj.core.ECKey;
        import org.bitcoinj.crypto.DeterministicKey;
        import org.bitcoinj.script.Script;
        import org.bitcoinj.wallet.KeyBag;
        import org.bitcoinj.wallet.RedeemData;

        import javax.annotation.Nullable;
        import java.nio.ByteBuffer;
        import java.util.HashMap;

public class UniquidKeyBag implements KeyBag {

    HashMap<ByteBuffer, DeterministicKey> pubKeys = new HashMap<>();
    HashMap<ByteBuffer, DeterministicKey> pubKeyhashes = new HashMap<>();

    public void addDeterministicKey(DeterministicKey deterministicKey) {
        pubKeys.put(ByteBuffer.wrap(deterministicKey.getPubKey()), deterministicKey);
        pubKeyhashes.put(ByteBuffer.wrap(deterministicKey.getPubKeyHash()), deterministicKey);
    }

    @Nullable
    @Override
    public ECKey findKeyFromPubKeyHash(byte[] pubKeyHash, @Nullable Script.ScriptType scriptType) {
        return pubKeyhashes.get(ByteBuffer.wrap(pubKeyHash));
    }

    @Override
    public ECKey findKeyFromPubKey(byte[] pubkey) {
        return pubKeys.get(ByteBuffer.wrap(pubkey));
    }

    @Override
    public RedeemData findRedeemDataFromScriptHash(byte[] scriptHash) {
        return null;
    }

}