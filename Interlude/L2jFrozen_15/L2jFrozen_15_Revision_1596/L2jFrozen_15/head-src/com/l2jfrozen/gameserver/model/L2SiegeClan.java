package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;

public class L2SiegeClan
{
	private int clanId = 0;
	private List<L2NpcInstance> flag = new ArrayList<>();
	private int numFlagsAdded = 0;
	private SiegeClanType type;
	
	public enum SiegeClanType
	{
		OWNER,
		DEFENDER,
		ATTACKER,
		DEFENDER_PENDING
	}
	
	public L2SiegeClan(final int clanId, final SiegeClanType type)
	{
		this.clanId = clanId;
		this.type = type;
	}
	
	public int getNumFlags()
	{
		return numFlagsAdded;
	}
	
	public void addFlag(final L2NpcInstance flag)
	{
		numFlagsAdded++;
		getFlag().add(flag);
	}
	
	public boolean removeFlag(final L2NpcInstance flag)
	{
		if (flag == null)
		{
			return false;
		}
		
		final boolean ret = getFlag().remove(flag);
		
		// flag.deleteMe();
		// check if null objects or dups remain in the list.
		// for some reason, this might be happening sometimes...
		// delete false duplicates: if this flag got deleted, delete its copies too.
		if (ret)
		{
			while (getFlag().remove(flag))
			{
				//
			}
		}
		
		// now delete nulls
		int n;
		boolean more = true;
		
		while (more)
		{
			more = false;
			n = getFlag().size();
			
			if (n > 0)
			{
				for (int i = 0; i < n; i++)
				{
					if (getFlag().get(i) == null)
					{
						getFlag().remove(i);
						more = true;
						break;
					}
				}
			}
		}
		
		numFlagsAdded--; // remove flag count
		flag.deleteMe();
		return ret;
	}
	
	public void removeFlags()
	{
		for (final L2NpcInstance flag : getFlag())
		{
			removeFlag(flag);
		}
	}
	
	public final int getClanId()
	{
		return clanId;
	}
	
	public final List<L2NpcInstance> getFlag()
	{
		if (flag == null)
		{
			flag = new ArrayList<>();
		}
		
		return flag;
	}
	
	public SiegeClanType getType()
	{
		return type;
	}
	
	public void setType(final SiegeClanType setType)
	{
		type = setType;
	}
}