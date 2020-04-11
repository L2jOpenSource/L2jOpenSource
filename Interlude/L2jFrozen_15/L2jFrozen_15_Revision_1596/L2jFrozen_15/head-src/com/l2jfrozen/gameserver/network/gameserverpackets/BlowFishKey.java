package com.l2jfrozen.gameserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends GameServerBasePacket
{
	private static Logger LOGGER = Logger.getLogger(BlowFishKey.class);
	
	/**
	 * @param blowfishKey
	 * @param publicKey
	 */
	public BlowFishKey(final byte[] blowfishKey, final RSAPublicKey publicKey)
	{
		writeC(0x00);
		try
		{
			final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
			final byte[] encrypted = rsaCipher.doFinal(blowfishKey);
			writeD(encrypted.length);
			writeB(encrypted);
		}
		catch (final GeneralSecurityException e)
		{
			LOGGER.warn("Error While encrypting blowfish key for transmision (Crypt error)");
			e.printStackTrace();
		}
		
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}
