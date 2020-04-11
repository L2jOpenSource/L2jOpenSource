package com.l2jfrozen.netcore;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param  <T>
 * @author KenM
 */
public abstract class ReceivablePacket<T extends MMOClient<?>> extends AbstractPacket<T> implements Runnable
{
	NioNetStringBuffer sbuf;
	protected static final Logger LOGGER = LoggerFactory.getLogger(ReceivablePacket.class);
	
	protected ReceivablePacket()
	{
		
	}
	
	protected abstract boolean read();
	
	@Override
	public abstract void run();
	
	protected final void readB(final byte[] dst)
	{
		try
		{
			
			buf.get(dst);
			
		}
		catch (final BufferUnderflowException e)
		{
			
			LOGGER.warn("", e);
			
		}
		
	}
	
	protected final void readB(final byte[] dst, final int offset, final int len)
	{
		try
		{
			
			buf.get(dst, offset, len);
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
	}
	
	protected final int readC()
	{
		try
		{
			
			return buf.get() & 0xFF;
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
		
	}
	
	protected final int readH()
	{
		
		try
		{
			
			return buf.getShort() & 0xFFFF;
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final int readD()
	{
		
		try
		{
			
			return buf.getInt();
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final long readQ()
	{
		
		try
		{
			
			return buf.getLong();
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final double readF()
	{
		try
		{
			
			return buf.getDouble();
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return -1;
	}
	
	protected final String readS()
	{
		sbuf.clear();
		
		try
		{
			
			char ch;
			while ((ch = buf.getChar()) != 0)
			{
				sbuf.append(ch);
			}
			
		}
		catch (final BufferUnderflowException e)
		{
			LOGGER.warn("", e);
			
		}
		
		return sbuf.toString();
	}
	
	/**
	 * packet forge purpose
	 * @param data
	 * @param client
	 * @param sBuffer
	 */
	public void setBuffers(final ByteBuffer data, final T client, final NioNetStringBuffer sBuffer)
	{
		buf = data;
		this.client = client;
		sbuf = sBuffer;
	}
}
