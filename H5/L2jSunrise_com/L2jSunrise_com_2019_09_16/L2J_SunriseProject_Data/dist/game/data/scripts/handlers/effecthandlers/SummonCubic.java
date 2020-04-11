/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

public class SummonCubic extends L2Effect
{
	/** Cubic ID. */
	private final int _cubicId;
	/** Cubic power. */
	private final int _cubicPower;
	/** Cubic duration. */
	private final int _cubicDuration;
	/** Cubic activation delay. */
	private final int _cubicDelay;
	/** Cubic maximum casts before going idle. */
	private final int _cubicMaxCount;
	/** Cubic activation chance. */
	private final int _cubicSkillChance;
	
	public SummonCubic(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_cubicId = template.getParameters().getInt("cubicId", -1);
		// Custom AI data.
		_cubicPower = template.getParameters().getInt("cubicPower", 0);
		_cubicDuration = template.getParameters().getInt("cubicDuration", 0);
		_cubicDelay = template.getParameters().getInt("cubicDelay", 0);
		_cubicMaxCount = template.getParameters().getInt("cubicMaxCount", -1);
		_cubicSkillChance = template.getParameters().getInt("cubicSkillChance", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer() || getEffected().isAlikeDead() || getEffected().getActingPlayer().inObserverMode())
		{
			return false;
		}
		
		if (_cubicId < 0)
		{
			_log.warn(SummonCubic.class.getSimpleName() + ": Invalid NPC Id:" + _cubicId + " in skill Id: " + getSkill().getId());
			return false;
		}
		
		final L2PcInstance player = getEffected().getActingPlayer();
		if (player.inObserverMode() || player.isMounted())
		{
			return false;
		}
		
		int _cubicSkillLevel = getSkill().getLevel();
		if (_cubicSkillLevel > 100)
		{
			_cubicSkillLevel = ((getSkill().getLevel() - 100) / 7) + 8;
		}
		
		if (!player.stopCubicById(_cubicId))
		{
			// If maximum amount is reached, random cubic is removed.
			// Players with no mastery can have only one cubic.
			final int allowedCubicCount = getEffected().getActingPlayer().getStat().getMaxCubicCount();
			final int currentCubicCount = player.getCubics().size();
			// Extra cubics are removed, one by one, randomly.
			for (int i = 0; i <= (currentCubicCount - allowedCubicCount); i++)
			{
				final int removedCubicId = (int) player.getCubics().keySet().toArray()[Rnd.get(currentCubicCount)];
				player.stopCubicById(removedCubicId);
			}
		}
		// Adding a new cubic.
		player.addCubic(_cubicId, _cubicSkillLevel, _cubicPower, _cubicDelay, _cubicSkillChance, _cubicMaxCount, _cubicDuration, getEffected() != getEffector());
		player.broadcastUserInfo();
		return true;
	}
}