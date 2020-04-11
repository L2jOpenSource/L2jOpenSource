package com.l2jfrozen.netcore;

import java.nio.ByteBuffer;

/**
 * @param  <T>
 * @author KenM
 */
public abstract class AbstractPacket<T extends MMOClient<?>>
{
	protected ByteBuffer buf;
	
	T client;
	
	public final T getClient()
	{
		return client;
	}
}