package com.l2jfrozen.loginserver;

/**
 * This class ...
 * @version $Revision: 1.2.4.2 $ $Date: 2005/03/27 15:30:09 $
 */

public class HackingException extends Exception
{
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 4050762693478463029L;
	String ip;
	private final int connects;
	
	public HackingException(final String ip, final int connects)
	{
		this.ip = ip;
		this.connects = connects;
	}
	
	/**
	 * @return
	 */
	public String getIP()
	{
		return ip;
	}
	
	public int getConnects()
	{
		return connects;
	}
	
}
