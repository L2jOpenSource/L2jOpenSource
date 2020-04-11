package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public class L2DecoInstance extends L2NpcInstance
{
	/**
	 * @param objectId
	 * @param template
	 */
	public L2DecoInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void reduceCurrentHp(double damage, final L2Character attacker, final boolean awake)
	{
		damage = 0;
		super.reduceCurrentHp(damage, attacker, awake);
	}
}
