package com.l2jfrozen.gameserver.network.gameserverpackets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.l2jfrozen.gameserver.thread.TaskPriority;

/**
 * @author -Wooden-
 */
public abstract class GameServerBasePacket
{
	private final ByteArrayOutputStream baos;
	
	protected GameServerBasePacket()
	{
		baos = new ByteArrayOutputStream();
	}
	
	protected void writeD(final int value)
	{
		baos.write(value & 0xff);
		baos.write(value >> 8 & 0xff);
		baos.write(value >> 16 & 0xff);
		baos.write(value >> 24 & 0xff);
	}
	
	protected void writeH(final int value)
	{
		baos.write(value & 0xff);
		baos.write(value >> 8 & 0xff);
	}
	
	protected void writeC(final int value)
	{
		baos.write(value & 0xff);
	}
	
	protected void writeF(final double org)
	{
		final long value = Double.doubleToRawLongBits(org);
		baos.write((int) (value & 0xff));
		baos.write((int) (value >> 8 & 0xff));
		baos.write((int) (value >> 16 & 0xff));
		baos.write((int) (value >> 24 & 0xff));
		baos.write((int) (value >> 32 & 0xff));
		baos.write((int) (value >> 40 & 0xff));
		baos.write((int) (value >> 48 & 0xff));
		baos.write((int) (value >> 56 & 0xff));
	}
	
	protected void writeS(final String text)
	{
		try
		{
			if (text != null)
			{
				baos.write(text.getBytes("UTF-16LE"));
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		
		baos.write(0);
		baos.write(0);
	}
	
	protected void writeB(final byte[] array)
	{
		try
		{
			baos.write(array);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public int getLength()
	{
		return baos.size() + 2;
	}
	
	public byte[] getBytes()
	{
		writeD(0x00); // reserve for checksum
		
		final int padding = baos.size() % 8;
		if (padding != 0)
		{
			for (int i = padding; i < 8; i++)
			{
				writeC(0x00);
			}
		}
		
		return baos.toByteArray();
	}
	
	public TaskPriority getPriority()
	{
		return TaskPriority.PR_HIGH;
	}
	
	public abstract byte[] getContent() throws IOException;
}
