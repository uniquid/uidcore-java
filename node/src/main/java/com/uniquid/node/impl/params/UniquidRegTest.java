package com.uniquid.node.impl.params;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bitcoinj.params.RegTestParams;
import org.spongycastle.util.Arrays;

/**
 * Parameters for Uniquid Internal RegTestNet
 */
public class UniquidRegTest extends RegTestParams {
	
	private UniquidRegTest() {
		port = 19000;
		
		try {
			
			InetAddress inetAddress1 = InetAddress.getByAddress(new byte[] {52, (byte) 225, (byte) 217, (byte) 168});
			InetAddress inetAddress2 = InetAddress.getByAddress(new byte[] {52, (byte) 167, (byte) 211, (byte) 151});
			
			int int1 = covertInetAddressToInt(inetAddress1);
			int int2 = covertInetAddressToInt(inetAddress2);
			
			addrSeeds = new int[] { int1, int2 };
			
		} catch (UnknownHostException e) {
			// IMPOSSIBLE
		} catch (Exception e) {
			// IMPOSSIBLE
		}
		
	}
	
	private static UniquidRegTest instance;
	
	public static synchronized UniquidRegTest get() {
        if (instance == null) {
            instance = new UniquidRegTest();
        }
        return instance;
    }
	
	private static InetAddress convertIntToInetAddress(int seed) throws UnknownHostException {
        byte[] v4addr = new byte[4];
        v4addr[0] = (byte) (0xFF & (seed));
        v4addr[1] = (byte) (0xFF & (seed >> 8));
        v4addr[2] = (byte) (0xFF & (seed >> 16));
        v4addr[3] = (byte) (0xFF & (seed >> 24));
        return InetAddress.getByAddress(v4addr);
    }
	
	private static int covertInetAddressToInt(InetAddress inetAddress) throws Exception {
		
		byte[] addressAsBytesArray = inetAddress.getAddress();
		
		addressAsBytesArray = Arrays.reverse(addressAsBytesArray);
		
		int result = 0;  
		for (byte b: addressAsBytesArray)  
		{  
		    result = result << 8 | (b & 0xFF);  
		}
		
		return result;
		
	}
	
}