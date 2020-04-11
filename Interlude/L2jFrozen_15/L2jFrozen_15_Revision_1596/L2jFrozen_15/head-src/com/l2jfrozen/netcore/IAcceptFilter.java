package com.l2jfrozen.netcore;

import java.nio.channels.SocketChannel;

/**
 * @author KenM
 */
public interface IAcceptFilter
{
	public boolean accept(SocketChannel sc);
}