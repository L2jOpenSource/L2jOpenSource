package com.l2jfrozen.netcore;

import java.nio.ByteBuffer;

/**
 * @param  <T>
 * @author KenM
 */
public abstract class MMOClient<T extends MMOConnection<?>>
{
	private final T con;
	
	public MMOClient(final T con)
	{
		this.con = con;
	}
	
	public T getConnection()
	{
		return con;
	}
	
	public abstract boolean decrypt(final ByteBuffer buf, final int size);
	
	public abstract boolean encrypt(final ByteBuffer buf, final int size);
	
	protected abstract void onDisconnection();
	
	protected abstract void onForcedDisconnection(boolean critical);
}