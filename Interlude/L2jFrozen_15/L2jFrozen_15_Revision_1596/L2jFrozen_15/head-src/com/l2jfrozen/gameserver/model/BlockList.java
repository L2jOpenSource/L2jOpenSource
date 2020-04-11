package com.l2jfrozen.gameserver.model;

import java.util.Set;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastSet;

/**
 * @author luisantonioa
 */
public class BlockList
{
	private final Set<String> blockSet;
	private boolean blockAll;
	private final L2PcInstance owner;
	
	public BlockList(final L2PcInstance owner)
	{
		this.owner = owner;
		blockSet = new FastSet<>();
		blockAll = false;
	}
	
	public void addToBlockList(final String character)
	{
		if (character != null)
		{
			blockSet.add(character);
			
			SystemMessage sm = null;
			
			final L2PcInstance target = L2World.getInstance().getPlayer(character);
			if (target != null)
			{
				sm = new SystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST);
				sm.addString(owner.getName());
				target.sendPacket(sm);
			}
			
			sm = new SystemMessage(SystemMessageId.S1_WAS_ADDED_TO_YOUR_IGNORE_LIST);
			sm.addString(character);
			owner.sendPacket(sm);
		}
	}
	
	public void removeFromBlockList(final String character)
	{
		if (character != null)
		{
			blockSet.remove(character);
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_REMOVED_FROM_YOUR_IGNORE_LIST);
			sm.addString(character);
			owner.sendPacket(sm);
		}
	}
	
	public boolean isInBlockList(final String character)
	{
		return blockSet.contains(character);
	}
	
	public boolean isBlockAll()
	{
		return blockAll;
	}
	
	public void setBlockAll(final boolean state)
	{
		blockAll = state;
	}
	
	public Set<String> getBlockList()
	{
		return blockSet;
	}
	
}
