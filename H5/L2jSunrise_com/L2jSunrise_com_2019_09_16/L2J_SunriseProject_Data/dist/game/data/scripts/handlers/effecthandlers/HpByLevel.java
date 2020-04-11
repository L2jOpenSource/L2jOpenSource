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

import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Hp By Level effect.
 * @author Zoey76
 */
public class HpByLevel extends L2Effect
{
	private final double _power;
	
	public HpByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_power = template.getParameters().getDouble("power", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null))
		{
			return false;
		}
		// Calculation
		final double abs = _power;
		final double absorb = ((getEffector().getCurrentHp() + abs) > getEffector().getMaxHp() ? getEffector().getMaxHp() : (getEffector().getCurrentHp() + abs));
		final int restored = (int) (absorb - getEffector().getCurrentHp());
		getEffector().setCurrentHp(absorb);
		// System message
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
		sm.addInt(restored);
		getEffector().sendPacket(sm);
		return true;
	}
}
