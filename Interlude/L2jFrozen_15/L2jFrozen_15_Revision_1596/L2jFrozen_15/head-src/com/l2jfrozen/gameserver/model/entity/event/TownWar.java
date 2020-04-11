package com.l2jfrozen.gameserver.model.entity.event;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.zone.type.L2TownZone;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class TownWar
{
	private boolean inProgress = false;
	private static TownWar instance;
	
	public static TownWar getInstance()
	{
		if (instance == null)
		{
			instance = new TownWar();
		}
		
		return instance;
	}
	
	public void start()
	{
		if (isInProgress())
		{
			return;
		}
		
		if (Config.TW_ALL_TOWNS) // All Towns will become War Zones
		{
			for (int townId = 1; townId <= 18; townId++)
			{
				setTownInWar(Config.TW_TOWN_ID, true);
			}
			
			Announcements.getInstance().gameAnnounceToAll("All towns have been set to war zone by .");
		}
		else // A Town will become War Zone
		{
			setTownInWar(Config.TW_TOWN_ID, true);
			Announcements.getInstance().gameAnnounceToAll(TownManager.getInstance().getTown(Config.TW_TOWN_ID).getZoneName() + " has been set to war zone.");
		}
		
		inProgress = true;
	}
	
	public void end()
	{
		if (!isInProgress())
		{
			return;
		}
		
		if (Config.TW_ALL_TOWNS) // All Towns will become Peace Zones
		{
			for (int townId = 1; townId <= 18; townId++)
			{
				setTownInWar(Config.TW_TOWN_ID, false);
			}
			
			Announcements.getInstance().gameAnnounceToAll("All towns have been set back to normal.");
		}
		else
		{
			setTownInWar(Config.TW_TOWN_ID, false); // A Town will become Peace Zone
			Announcements.getInstance().gameAnnounceToAll(TownManager.getInstance().getTown(Config.TW_TOWN_ID).getZoneName() + " has been set back to normal.");
		}
		
		inProgress = false;
	}
	
	public void setTownInWar(int townId, boolean isInWar)
	{
		boolean peace = !isInWar;
		
		L2TownZone town = TownManager.getInstance().getTown(townId);
		town.setParameter("noPeace", String.valueOf(isInWar));
		
		for (L2Character character : town.characterList.values())
		{
			if (character.isPlayer())
			{
				L2PcInstance player = (L2PcInstance) character;
				player.setInsideZone(L2Character.ZONE_PEACE, peace);
				player.setInsideZone(L2Character.ZONE_PVP, isInWar);
				player.setInTownWar(isInWar);
				
				if (isInWar)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
				}
				else
				{
					player.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				}
			}
		}
		
		// Maybe the player leave the zone TownWar
		if (!isInWar)
		{
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (player == null)
				{
					return;
				}
				
				if (player.isinTownWar())
				{
					player.setInTownWar(false);
				}
			}
		}
	}
	
	public boolean isInProgress()
	{
		return inProgress;
	}
}
