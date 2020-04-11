package com.l2jfrozen.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * A castle zone
 * @author durgus
 */
public class L2CastleZone extends L2ZoneType
{
	private int castleId;
	private Castle castle;
	private final int[] spawnLoc;
	
	public L2CastleZone(final int id)
	{
		super(id);
		
		spawnLoc = new int[3];
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		switch (name)
		{
			case "castleId":
				castleId = Integer.parseInt(value);
				
				// Register self to the correct castle
				castle = CastleManager.getInstance().getCastleById(castleId);
				castle.setZone(this);
				break;
			case "spawnX":
				spawnLoc[0] = Integer.parseInt(value);
				break;
			case "spawnY":
				spawnLoc[1] = Integer.parseInt(value);
				break;
			case "spawnZ":
				spawnLoc[2] = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (castle.getSiege().getIsInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			character.setInsideZone(L2Character.ZONE_SIEGE, true);
			
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (castle.getSiege().getIsInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
			character.setInsideZone(L2Character.ZONE_SIEGE, false);
			
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
				
				// Set pvp flag
				if (((L2PcInstance) character).getPvpFlag() == 0)
				{
					((L2PcInstance) character).startPvPFlag();
				}
			}
		}
		if (character instanceof L2SiegeSummonInstance)
		{
			((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
		}
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
	}
	
	public void updateZoneStatusForCharactersInside()
	{
		if (castle.getSiege().getIsInProgress())
		{
			for (final L2Character character : characterList.values())
			{
				try
				{
					onEnter(character);
				}
				catch (final NullPointerException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
				}
			}
		}
		else
		{
			for (final L2Character character : characterList.values())
			{
				try
				{
					character.setInsideZone(L2Character.ZONE_PVP, false);
					character.setInsideZone(L2Character.ZONE_SIEGE, false);
					
					if (character instanceof L2PcInstance)
					{
						((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
					}
					
					if (character instanceof L2SiegeSummonInstance)
					{
						((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
					}
				}
				catch (final NullPointerException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Removes all foreigners from the castle
	 * @param owningClanId
	 */
	public void banishForeigners(final int owningClanId)
	{
		for (final L2Character temp : characterList.values())
		{
			if (!(temp instanceof L2PcInstance))
			{
				continue;
			}
			
			if (((L2PcInstance) temp).getClanId() == owningClanId)
			{
				continue;
			}
			
			((L2PcInstance) temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}
	
	/**
	 * Sends a message to all players in this zone
	 * @param message
	 */
	public void announceToPlayers(final String message)
	{
		for (final L2Character temp : characterList.values())
		{
			if (temp instanceof L2PcInstance)
			{
				((L2PcInstance) temp).sendMessage(message);
			}
		}
	}
	
	/**
	 * Returns all players within this zone
	 * @return
	 */
	public List<L2PcInstance> getAllPlayers()
	{
		List<L2PcInstance> players = new ArrayList<>();
		
		for (L2Character temp : characterList.values())
		{
			if (temp.isPlayer())
			{
				players.add((L2PcInstance) temp);
			}
		}
		
		return players;
	}
	
	/**
	 * Get the castles defender spawn
	 * @return
	 */
	public int[] getSpawn()
	{
		return spawnLoc;
	}
	
	/**
	 * @return
	 */
	
	public boolean isSiegeActive()
	{
		if (castle != null)
		{
			return castle.isSiegeInProgress();
		}
		
		return false;
	}
}
