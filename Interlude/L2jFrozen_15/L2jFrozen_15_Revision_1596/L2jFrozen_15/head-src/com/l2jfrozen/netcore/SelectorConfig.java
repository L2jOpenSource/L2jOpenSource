package com.l2jfrozen.netcore;

/**
 * @author KenM
 */

public final class SelectorConfig
{
	private int readBufferSize = 64 * 1024;
	
	private int writeBufferSize = 64 * 1024;
	
	private int helperBufferCount = 20;
	
	private int helperBufferSize = 64 * 1024;
	
	/**
	 * Server will try to send maxSendPerPass packets per socket write call<br>
	 * however it may send less if the write buffer was filled before achieving this value.
	 */
	private int maxSendPerPass = 10;
	
	/**
	 * Server will try to read maxReadPerPass packets per socket read call<br>
	 * however it may read less if the read buffer was empty before achieving this value.
	 */
	private int maxReadPerPass = 10;
	
	/**
	 * Defines how much time (in milis) should the selector sleep, an higher value increases throughput but also increases latency(to a max of the sleep value itself).<BR>
	 * Also an extremely high value(usually > 100) will decrease throughput due to the server not doing enough sends per second (depends on max sends per pass).<BR>
	 * <BR>
	 * Recommended values:<BR>
	 * 1 for minimal latency.<BR>
	 * 10-30 for an latency/troughput trade-off based on your needs.<BR>
	 */
	private int sleepTime = 10;
	
	public int getReadBufferSize()
	{
		return readBufferSize;
	}
	
	public void setReadBufferSize(final int readBufferSize)
	{
		this.readBufferSize = readBufferSize;
	}
	
	public int getWriteBufferSize()
	{
		return writeBufferSize;
	}
	
	public void setWriteBufferSize(final int writeBufferSize)
	{
		this.writeBufferSize = writeBufferSize;
	}
	
	public int getHelperBufferCount()
	{
		return helperBufferCount;
	}
	
	public void setHelperBufferCount(final int helperBufferCount)
	{
		this.helperBufferCount = helperBufferCount;
	}
	
	public int getHelperBufferSize()
	{
		return helperBufferSize;
	}
	
	public void setHelperBufferSize(final int helperBufferSize)
	{
		this.helperBufferSize = helperBufferSize;
	}
	
	public int getMaxSendPerPass()
	{
		return maxSendPerPass;
	}
	
	public void setMaxSendPerPass(final int maxSendPerPass)
	{
		this.maxSendPerPass = maxSendPerPass;
	}
	
	public int getMaxReadPerPass()
	{
		return maxReadPerPass;
	}
	
	public void setMaxReadPerPass(final int maxReadPerPass)
	{
		this.maxReadPerPass = maxReadPerPass;
	}
	
	public int getSleepTime()
	{
		return sleepTime;
	}
	
	public void setSleepTime(final int sleepTime)
	{
		this.sleepTime = sleepTime;
	}
}
