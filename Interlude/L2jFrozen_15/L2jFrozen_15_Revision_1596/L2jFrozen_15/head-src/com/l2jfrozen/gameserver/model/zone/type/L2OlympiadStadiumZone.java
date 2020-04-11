package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable.TeleportWhereType;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author durgus
 */
public class L2OlympiadStadiumZone extends L2ZoneType
{
	private int stadiumId;
	
	public L2OlympiadStadiumZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("stadiumId"))
		{
			stadiumId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	public void onEnter(L2Character character)
	{
		if (character == null)
		{
			return;
		}
		
		character.setInsideZone(L2Character.ZONE_PVP, true);
		character.setInsideZone(L2Character.ZONE_OLY, true);
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, true);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			
			if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode())
			{
				player.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadium");
				player.teleToLocation(TeleportWhereType.Town);
			}
		}
	}
	
	@Override
	public void onExit(L2Character character)
	{
		if (character == null)
		{
			return;
		}
		
		character.setInsideZone(L2Character.ZONE_PVP, false);
		character.setInsideZone(L2Character.ZONE_OLY, false);
		character.setInsideZone(L2Character.ZONE_NOSUMMONFRIEND, false);
		
		if (character instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance) character;
			player.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	/**
	 * @return this zones stadium id (if any)
	 */
	public int getStadiumId()
	{
		return stadiumId;
	}
}