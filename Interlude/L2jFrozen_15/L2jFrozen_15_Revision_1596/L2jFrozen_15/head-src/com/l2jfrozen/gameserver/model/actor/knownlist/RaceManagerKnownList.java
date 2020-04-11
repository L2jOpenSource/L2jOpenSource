package com.l2jfrozen.gameserver.model.actor.knownlist;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaceManagerInstance;
import com.l2jfrozen.gameserver.model.entity.MonsterRace;
import com.l2jfrozen.gameserver.network.serverpackets.DeleteObject;

public class RaceManagerKnownList extends NpcKnownList
{
	public RaceManagerKnownList(final L2RaceManagerInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public boolean addKnownObject(final L2Object object)
	{
		return addKnownObject(object, null);
	}
	
	@Override
	public boolean addKnownObject(final L2Object object, final L2Character dropper)
	{
		if (!super.addKnownObject(object, dropper))
		{
			return false;
		}
		
		/*
		 * DONT KNOW WHY WE NEED THIS WHEN RACE MANAGER HAS A METHOD THAT BROADCAST TO ITS KNOW PLAYERS if (object instanceof L2PcInstance) { if (packet != null) ((L2PcInstance) object).sendPacket(packet); }
		 */
		
		return true;
	}
	
	@Override
	public boolean removeKnownObject(final L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		if (object instanceof L2PcInstance)
		{
			// LOGGER.info("Sending delete monsrac info.");
			DeleteObject obj = null;
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				((L2PcInstance) object).sendPacket(obj);
			}
			
			obj = null;
		}
		
		return true;
	}
	
	@Override
	public L2RaceManagerInstance getActiveChar()
	{
		return (L2RaceManagerInstance) super.getActiveChar();
	}
}
