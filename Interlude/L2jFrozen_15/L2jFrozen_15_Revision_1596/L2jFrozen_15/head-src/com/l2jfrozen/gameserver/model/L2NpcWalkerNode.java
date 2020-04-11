package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * @author Rayan RPG
 * @since  927
 */
public class L2NpcWalkerNode
{
	private int routeId;
	private int npcId;
	private String movePoint;
	private String chatText;
	private int moveX;
	private int moveY;
	private int moveZ;
	private int delay;
	private boolean running;
	
	public void setRunning(final boolean val)
	{
		running = val;
	}
	
	public void setRouteId(final int id)
	{
		routeId = id;
	}
	
	public void setNpcId(final int id)
	{
		npcId = id;
	}
	
	public void setMovePoint(final String val)
	{
		movePoint = val;
	}
	
	public void setChatText(final String val)
	{
		chatText = val;
	}
	
	public void setMoveX(final int val)
	{
		moveX = val;
	}
	
	public void setMoveY(final int val)
	{
		moveY = val;
	}
	
	public void setMoveZ(final int val)
	{
		moveZ = val;
	}
	
	public void setDelay(final int val)
	{
		delay = val;
	}
	
	public int getRouteId()
	{
		return routeId;
	}
	
	public int getNpcId()
	{
		return npcId;
	}
	
	public String getMovePoint()
	{
		return movePoint;
	}
	
	public String getChatText()
	{
		return chatText;
	}
	
	public int getMoveX()
	{
		return moveX;
	}
	
	public int getMoveY()
	{
		return moveY;
	}
	
	public int getMoveZ()
	{
		return moveZ;
	}
	
	public int getDelay()
	{
		return delay;
	}
	
	public boolean getRunning()
	{
		return running;
	}
	
	/**
	 * Constructor of L2NpcWalker.
	 */
	public L2NpcWalkerNode()
	{
	}
	
	/**
	 * Constructor of L2NpcWalker.<BR>
	 * <BR>
	 * @param set The StatsSet object to transfert data to the method
	 */
	public L2NpcWalkerNode(final StatsSet set)
	{
		npcId = set.getInteger("npc_id");
		movePoint = set.getString("move_point");
		chatText = set.getString("chatText");
		moveX = set.getInteger("move_x");
		moveX = set.getInteger("move_y");
		moveX = set.getInteger("move_z");
		delay = set.getInteger("delay");
	}
}
