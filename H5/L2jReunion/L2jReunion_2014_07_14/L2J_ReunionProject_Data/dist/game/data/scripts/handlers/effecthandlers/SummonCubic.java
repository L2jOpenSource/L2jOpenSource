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

import l2r.gameserver.model.actor.instance.L2CubicInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

public class SummonCubic extends L2Effect
{
	private final int _npcId;
	private final int _cubicPower;
	private final int _cubicDuration;
	private final int _cubicDelay;
	private final int _cubicMaxCount;
	private final int _cubicSkillChance;
	
	public SummonCubic(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_npcId = template.getParameters().getInteger("npcId", 0);
		_cubicPower = template.getParameters().getInteger("cubicPower", 0);
		_cubicDuration = template.getParameters().getInteger("cubicDuration", 0);
		_cubicDelay = template.getParameters().getInteger("cubicDelay", 0);
		_cubicMaxCount = template.getParameters().getInteger("cubicMaxCount", -1);
		_cubicSkillChance = template.getParameters().getInteger("cubicSkillChance", 0);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer() || getEffected().isAlikeDead() || getEffected().getActingPlayer().inObserverMode())
		{
			return false;
		}
		
		if (_npcId <= 0)
		{
			_log.warn(SummonCubic.class.getSimpleName() + ": Invalid NPC Id:" + _npcId + " in skill Id: " + getSkill().getId());
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
		
		final L2CubicInstance cubic = player.getCubicById(_npcId);
		if (cubic != null)
		{
			cubic.stopAction();
			cubic.cancelDisappear();
			player.getCubics().remove(cubic);
		}
		else
		{
			final L2Effect cubicMastery = player.getFirstPassiveEffect(L2EffectType.CUBIC_MASTERY);
			final int cubicCount = (int) (cubicMastery != null ? cubicMastery.calc() : 1);
			final int currectCubicCount = player.getCubics().size();
			
			if (currectCubicCount >= cubicCount)
			{
				final int removedCubicId = (int) player.getCubics().keySet().toArray()[Rnd.get(currectCubicCount)];
				final L2CubicInstance removedCubic = player.getCubicById(removedCubicId);
				removedCubic.stopAction();
				removedCubic.cancelDisappear();
				player.getCubics().remove(removedCubic.getId());
			}
		}
		player.addCubic(_npcId, _cubicSkillLevel, _cubicPower, _cubicDelay, _cubicSkillChance, _cubicMaxCount, _cubicDuration, getEffected() != getEffector());
		player.broadcastUserInfo();
		return true;
	}
}