package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2TeleportLocation
{
	private int teleId;
	private int locX;
	private int locY;
	private int locZ;
	private int price;
	private boolean forNoble;
	
	/**
	 * @param id
	 */
	public void setTeleId(final int id)
	{
		teleId = id;
	}
	
	/**
	 * @param locX
	 */
	public void setLocX(final int locX)
	{
		this.locX = locX;
	}
	
	/**
	 * @param locY
	 */
	public void setLocY(final int locY)
	{
		this.locY = locY;
	}
	
	/**
	 * @param locZ
	 */
	public void setLocZ(final int locZ)
	{
		this.locZ = locZ;
	}
	
	/**
	 * @param price
	 */
	public void setPrice(final int price)
	{
		this.price = price;
	}
	
	/**
	 * @param val
	 */
	public void setIsForNoble(final boolean val)
	{
		forNoble = val;
	}
	
	/**
	 * @return
	 */
	public int getTeleId()
	{
		return teleId;
	}
	
	/**
	 * @return
	 */
	public int getLocX()
	{
		return locX;
	}
	
	/**
	 * @return
	 */
	public int getLocY()
	{
		return locY;
	}
	
	/**
	 * @return
	 */
	public int getLocZ()
	{
		return locZ;
	}
	
	/**
	 * @return
	 */
	public int getPrice()
	{
		return price;
	}
	
	/**
	 * @return
	 */
	public boolean getIsForNoble()
	{
		return forNoble;
	}
}
