
package com.l2jfrozen.gameserver.model.zone.type;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.util.random.Rnd;

public class L2CastleTeleportZone extends L2ZoneType
{
	
	private final int spawnLoc[];
	private int castleId;
	private Castle castle;
	
	public L2CastleTeleportZone(final int id)
	{
		super(id);
		spawnLoc = new int[5];
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		switch (name)
		{
			case "castleId":
				castleId = Integer.parseInt(value);
				castle = CastleManager.getInstance().getCastleById(castleId);
				castle.setTeleZone(this);
				break;
			case "spawnMinX":
				spawnLoc[0] = Integer.parseInt(value);
				break;
			case "spawnMaxX":
				spawnLoc[1] = Integer.parseInt(value);
				break;
			case "spawnMinY":
				spawnLoc[2] = Integer.parseInt(value);
				break;
			case "spawnMaxY":
				spawnLoc[3] = Integer.parseInt(value);
				break;
			case "spawnZ":
				spawnLoc[4] = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		character.setInsideZone(4096, true);
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		character.setInsideZone(4096, false);
	}
	
	@Override
	public void onDieInside(final L2Character l2character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character l2character)
	{
	}
	
	public List<L2Character> getAllPlayers()
	{
		return characterList.values().stream().filter(character -> character.isPlayer()).collect(Collectors.toList());
	}
	
	public void oustAllPlayers()
	{
		if (characterList == null)
		{
			return;
		}
		
		if (characterList.isEmpty())
		{
			return;
		}
		
		Iterator<L2Character> i$ = characterList.values().iterator();
		while (i$.hasNext())
		{
			L2Character character = i$.next();
			
			if (character != null && character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				
				if (player.isOnline())
				{
					player.teleToLocation(Rnd.get(spawnLoc[0], spawnLoc[1]), Rnd.get(spawnLoc[2], spawnLoc[3]), spawnLoc[4]);
				}
				
				player = null;
			}
			
			character = null;
		}
		
		i$ = null;
	}
	
	public int[] getSpawn()
	{
		return spawnLoc;
	}
}
