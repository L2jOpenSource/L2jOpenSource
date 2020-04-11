package com.l2jfrozen.crypt;

import java.io.IOException;

import com.l2jfrozen.util.random.Rnd;

/**
 * @author KenM
 */
public class LoginCrypt
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b,
		(byte) 0x60,
		(byte) 0xcb,
		(byte) 0x5b,
		(byte) 0x82,
		(byte) 0xce,
		(byte) 0x90,
		(byte) 0xb1,
		(byte) 0xcc,
		(byte) 0x2b,
		(byte) 0x6c,
		(byte) 0x55,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c
	};
	
	private final NewCrypt staticCrypt = new NewCrypt(STATIC_BLOWFISH_KEY);
	private NewCrypt crypt;
	private boolean isStatic = true;
	
	public void setKey(final byte[] key)
	{
		crypt = new NewCrypt(key);
	}
	
	public boolean decrypt(final byte[] raw, final int offset, final int size) throws IOException
	{
		crypt.decrypt(raw, offset, size);
		return NewCrypt.verifyChecksum(raw, offset, size);
	}
	
	public int encrypt(final byte[] raw, final int offset, int size) throws IOException
	{
		// reserve checksum
		size += 4;
		
		if (isStatic)
		{
			// reserve for XOR "key"
			size += 4;
			
			// padding
			size += 8 - size % 8;
			NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
			staticCrypt.crypt(raw, offset, size);
			
			isStatic = false;
		}
		else
		{
			// padding
			size += 8 - size % 8;
			NewCrypt.appendChecksum(raw, offset, size);
			crypt.crypt(raw, offset, size);
		}
		return size;
	}
}
