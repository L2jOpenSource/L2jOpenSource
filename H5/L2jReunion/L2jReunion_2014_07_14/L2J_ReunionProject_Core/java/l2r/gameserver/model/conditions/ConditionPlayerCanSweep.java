/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.conditions;

import l2r.Config;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;

/**
 * Checks Sweeper conditions:
 * <ul>
 * <li>Minimum checks, player not null, skill not null.</li>
 * <li>Checks if the target isn't null, is dead and spoiled.</li>
 * <li>Checks if the sweeper player is the target spoiler, or is in the spoiler party.</li>
 * <li>Checks if the corpse is too old.</li>
 * <li>Checks inventory limit and weight max load won't be exceed after sweep.</li>
 * </ul>
 * If two or more conditions aren't meet at the same time, one message per condition will be shown.
 * @author Zoey76
 */
public class ConditionPlayerCanSweep extends Condition
{
	private final boolean _val;
	
	public ConditionPlayerCanSweep(boolean val)
	{
		_val = val;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		boolean canSweep = false;
		if (env.getPlayer() != null)
		{
			final L2PcInstance sweeper = env.getPlayer();
			final L2Skill sweep = env.getSkill();
			if (sweep != null)
			{
				final L2Object[] targets = sweep.getTargetList(sweeper);
				if (targets != null)
				{
					L2Attackable target;
					for (L2Object objTarget : targets)
					{
						if (objTarget instanceof L2Attackable)
						{
							target = (L2Attackable) objTarget;
							if (target.isDead())
							{
								if (target.isSpoil())
								{
									canSweep = target.checkSpoilOwner(sweeper, true);
									canSweep &= !target.isOldCorpse(sweeper, Config.MAX_SWEEPER_TIME, true);
									canSweep &= sweeper.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, true);
								}
								else
								{
									sweeper.sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED);
								}
							}
						}
					}
				}
			}
		}
		return (_val == canSweep);
	}
}
