package com.l2jfrozen.gameserver.model.entity.event.manager;

/**
 * @author Shyla
 */
public interface EventTask extends Runnable
{
	
	public String getEventIdentifier();
	
	public String getEventStartTime(); // hh:mm (es. 01:05)
	
	// public void notifyEventStart();
	
}
