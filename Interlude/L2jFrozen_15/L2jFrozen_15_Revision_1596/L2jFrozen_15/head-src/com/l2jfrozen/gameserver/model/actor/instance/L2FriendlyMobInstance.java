package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.knownlist.FriendlyMobKnownList;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * This class represents Friendly Mobs lying over the world. These friendly mobs should only attack players with karma > 0 and it is always aggro, since it just attacks players with karma
 * @version $Revision: 1.20.4.6 $ $Date: 2005/07/23 16:13:39 $
 */
public class L2FriendlyMobInstance extends L2Attackable
{
	public L2FriendlyMobInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
	}
	
	@Override
	public final FriendlyMobKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof FriendlyMobKnownList))
		{
			setKnownList(new FriendlyMobKnownList(this));
		}
		return (FriendlyMobKnownList) super.getKnownList();
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		if (attacker instanceof L2PcInstance)
		{
			return ((L2PcInstance) attacker).getKarma() > 0;
		}
		return false;
	}
	
	@Override
	public boolean isAggressive()
	{
		return true;
	}
}
