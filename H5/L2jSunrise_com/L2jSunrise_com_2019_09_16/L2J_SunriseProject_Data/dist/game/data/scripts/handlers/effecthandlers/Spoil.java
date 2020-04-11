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

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.network.SystemMessageId;

/**
 * Spoil effect.
 * @author _drunk_, Ahmed, Zoey76
 */
public class Spoil extends L2Effect
{
	public Spoil(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SPOIL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isMonster() || getEffected().isDead())
		{
			getEffector().sendPacket(SystemMessageId.INCORRECT_TARGET);
			return false;
		}
		
		final L2MonsterInstance target = (L2MonsterInstance) getEffected();
		if (target.isSpoiled())
		{
			getEffector().sendPacket(SystemMessageId.ALREADY_SPOILED);
			return false;
		}
		
		if (Formulas.calcMagicSuccess(getEffector(), target, getSkill()))
		{
			target.setSpoilerObjectId(getEffector().getObjectId());
			getEffector().sendPacket(SystemMessageId.SPOIL_SUCCESS);
		}
		
		target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
		return true;
	}
}
