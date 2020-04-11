package com.l2jfrozen.netcore;

import java.nio.ByteBuffer;

/**
 * @param  <T>
 * @author KenM
 */
public interface IPacketHandler<T extends MMOClient<?>>
{
	public ReceivablePacket<T> handlePacket(ByteBuffer buf, T client);
}