package com.l2jfrozen.gameserver.model.actor.knownlist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.util.Util;

public class CharKnownList extends ObjectKnownList
{
	private Map<Integer, L2PcInstance> knownPlayers;
	private Map<Integer, Integer> knownRelations;
	
	public CharKnownList(final L2Character activeChar)
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
		
		if (object instanceof L2PcInstance)
		{
			getKnownPlayers().put(object.getObjectId(), (L2PcInstance) object);
			getKnownRelations().put(object.getObjectId(), -1);
		}
		return true;
	}
	
	/**
	 * @param  player The L2PcInstance to search in knownPlayer
	 * @return        True if the L2PcInstance is in knownPlayer of the L2Character.
	 */
	public final boolean knowsThePlayer(final L2PcInstance player)
	{
		return getActiveChar() == player || getKnownPlayers().containsKey(player.getObjectId());
	}
	
	/**
	 * Remove all L2Object from knownObjects and knownPlayer of the L2Character then cancel Attak or Cast and notify AI.
	 */
	@Override
	public final void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		getKnownPlayers().clear();
		getKnownRelations().clear();
		
		// Set target of the L2Character to null
		// Cancel Attack or Cast
		getActiveChar().setTarget(null);
		
		// Cancel AI Task
		if (getActiveChar().hasAI())
		{
			getActiveChar().setAI(null);
		}
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
			getKnownPlayers().remove(object.getObjectId());
			getKnownRelations().remove(object.getObjectId());
		}
		// If object is targeted by the L2Character, cancel Attack or Cast
		if (object == getActiveChar().getTarget())
		{
			getActiveChar().setTarget(null);
		}
		
		return true;
	}
	
	public L2Character getActiveChar()
	{
		return (L2Character) super.getActiveObject();
	}
	
	@Override
	public int getDistanceToForgetObject(final L2Object object)
	{
		return 0;
	}
	
	@Override
	public int getDistanceToWatchObject(final L2Object object)
	{
		return 0;
	}
	
	public Collection<L2Character> getKnownCharacters()
	{
		List<L2Character> result = new ArrayList<>();
		
		for (L2Object obj : getKnownObjects().values())
		{
			if (obj != null && obj instanceof L2Character)
			{
				result.add((L2Character) obj);
			}
		}
		
		return result;
	}
	
	public Collection<L2Character> getKnownCharactersInRadius(final long radius)
	{
		List<L2Character> result = new ArrayList<>();
		
		for (L2Object obj : getKnownObjects().values())
		{
			if (obj instanceof L2PcInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2PcInstance) obj);
				}
			}
			else if (obj instanceof L2MonsterInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2MonsterInstance) obj);
				}
			}
			else if (obj instanceof L2NpcInstance)
			{
				if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
				{
					result.add((L2NpcInstance) obj);
				}
			}
		}
		
		return result;
	}
	
	public final Map<Integer, L2PcInstance> getKnownPlayers()
	{
		if (knownPlayers == null)
		{
			knownPlayers = new ConcurrentHashMap<>();
		}
		
		return knownPlayers;
	}
	
	public final Map<Integer, Integer> getKnownRelations()
	{
		if (knownRelations == null)
		{
			knownRelations = new ConcurrentHashMap<>();
		}
		
		return knownRelations;
	}
	
	public final Collection<L2PcInstance> getKnownPlayersInRadius(final long radius)
	{
		List<L2PcInstance> result = new ArrayList<>();
		
		for (L2PcInstance player : getKnownPlayers().values())
		{
			if (Util.checkIfInRange((int) radius, getActiveChar(), player, true))
			{
				result.add(player);
			}
		}
		
		return result;
	}
}
