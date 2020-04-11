package com.l2jfrozen.loginserver.network.gameserverpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

import org.apache.log4j.Logger;

import com.l2jfrozen.loginserver.network.clientpackets.ClientBasePacket;

/**
 * @author -Wooden-
 */
public class BlowFishKey extends ClientBasePacket
{
	byte[] key;
	protected static final Logger LOGGER = Logger.getLogger(BlowFishKey.class);
	
	/**
	 * @param decrypt
	 * @param privateKey
	 */
	public BlowFishKey(final byte[] decrypt, final RSAPrivateKey privateKey)
	{
		super(decrypt);
		final int size = readD();
		
		byte[] tempKey = readB(size);
		
		try
		{
			byte[] tempDecryptKey;
			
			Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
			rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
			tempDecryptKey = rsaCipher.doFinal(tempKey);
			
			// there are nulls before the key we must remove them
			int i = 0;
			final int len = tempDecryptKey.length;
			
			for (; i < len; i++)
			{
				if (tempDecryptKey[i] != 0)
				{
					break;
				}
			}
			
			key = new byte[len - i];
			System.arraycopy(tempDecryptKey, i, key, 0, len - i);
			
			rsaCipher = null;
		}
		catch (final GeneralSecurityException e)
		{
			LOGGER.error("Error While decrypting blowfish key (RSA)", e);
			e.printStackTrace();
		}
		/*
		 * catch(IOException ioe) { //TODO: manage }
		 */
		
		tempKey = null;
	}
	
	public byte[] getKey()
	{
		return key;
	}
	
}
