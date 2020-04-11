package com.l2jfrozen.gameserver.network;

/**
 * @author L2JFrozen
 */
public class GameCrypt
{
	private final byte[] inKey = new byte[16];
	private final byte[] outKey = new byte[16];
	private boolean isEnabled;
	
	public static void decrypt(final byte[] raw, final int offset, final int size, final GameCrypt gcrypt)
	{
		if (!gcrypt.isEnabled)
		{
			return;
		}
		
		int temp = 0;
		
		for (int i = 0; i < size; i++)
		{
			final int temp2 = raw[offset + i] & 0xFF;
			
			raw[offset + i] = (byte) (temp2 ^ gcrypt.inKey[i & 15] ^ temp);
			temp = temp2;
		}
		
		int old = gcrypt.inKey[8] & 0xff;
		old |= gcrypt.inKey[9] << 8 & 0xff00;
		old |= gcrypt.inKey[10] << 0x10 & 0xff0000;
		old |= gcrypt.inKey[11] << 0x18 & 0xff000000;
		
		old += size;
		
		gcrypt.inKey[8] = (byte) (old & 0xff);
		gcrypt.inKey[9] = (byte) (old >> 0x08 & 0xff);
		gcrypt.inKey[10] = (byte) (old >> 0x10 & 0xff);
		gcrypt.inKey[11] = (byte) (old >> 0x18 & 0xff);
	}
	
	public static void encrypt(final byte[] raw, final int offset, final int size, final GameCrypt gcrypt)
	{
		if (!gcrypt.isEnabled)
		{
			gcrypt.isEnabled = true;
			return;
		}
		
		int temp = 0;
		
		for (int i = 0; i < size; i++)
		{
			final int temp2 = raw[offset + i] & 0xFF;
			
			temp = temp2 ^ gcrypt.outKey[i & 15] ^ temp;
			raw[offset + i] = (byte) temp;
		}
		
		int old = gcrypt.outKey[8] & 0xff;
		
		old |= gcrypt.outKey[9] << 8 & 0xff00;
		old |= gcrypt.outKey[10] << 0x10 & 0xff0000;
		old |= gcrypt.outKey[11] << 0x18 & 0xff000000;
		
		old += size;
		
		gcrypt.outKey[8] = (byte) (old & 0xff);
		gcrypt.outKey[9] = (byte) (old >> 0x08 & 0xff);
		gcrypt.outKey[10] = (byte) (old >> 0x10 & 0xff);
		gcrypt.outKey[11] = (byte) (old >> 0x18 & 0xff);
	}
	
	public static void setKey(final byte[] key, final GameCrypt gcrypt)
	{
		System.arraycopy(key, 0, gcrypt.inKey, 0, 16);
		System.arraycopy(key, 0, gcrypt.outKey, 0, 16);
	}
}
