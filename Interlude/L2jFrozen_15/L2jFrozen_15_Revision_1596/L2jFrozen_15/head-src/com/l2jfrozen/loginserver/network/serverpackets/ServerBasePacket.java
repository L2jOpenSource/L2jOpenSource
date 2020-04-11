package com.l2jfrozen.loginserver.network.serverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:11 $
 */
public abstract class ServerBasePacket
{
	ByteArrayOutputStream bao;
	
	protected ServerBasePacket()
	{
		bao = new ByteArrayOutputStream();
	}
	
	protected void writeD(final int value)
	{
		bao.write(value & 0xff);
		bao.write(value >> 8 & 0xff);
		bao.write(value >> 16 & 0xff);
		bao.write(value >> 24 & 0xff);
	}
	
	protected void writeH(final int value)
	{
		bao.write(value & 0xff);
		bao.write(value >> 8 & 0xff);
	}
	
	protected void writeC(final int value)
	{
		bao.write(value & 0xff);
	}
	
	protected void writeF(final double org)
	{
		final long value = Double.doubleToRawLongBits(org);
		bao.write((int) (value & 0xff));
		bao.write((int) (value >> 8 & 0xff));
		bao.write((int) (value >> 16 & 0xff));
		bao.write((int) (value >> 24 & 0xff));
		bao.write((int) (value >> 32 & 0xff));
		bao.write((int) (value >> 40 & 0xff));
		bao.write((int) (value >> 48 & 0xff));
		bao.write((int) (value >> 56 & 0xff));
	}
	
	protected void writeS(final String text)
	{
		try
		{
			if (text != null)
			{
				bao.write(text.getBytes("UTF-16LE"));
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		bao.write(0);
		bao.write(0);
	}
	
	protected void writeB(final byte[] array)
	{
		try
		{
			bao.write(array);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public int getLength()
	{
		return bao.size() + 2;
	}
	
	public byte[] getBytes()
	{
		// if (this instanceof Init)
		// writeD(0x00); //reserve for XOR initial key
		
		writeD(0x00); // reserve for checksum
		
		final int padding = bao.size() % 8;
		if (padding != 0)
		{
			for (int i = padding; i < 8; i++)
			{
				writeC(0x00);
			}
		}
		
		return bao.toByteArray();
	}
	
	public abstract byte[] getContent() throws IOException;
}
