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
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Hp By Level effect.
 * @author Zoey76
 */
public class HpByLevel extends L2Effect
{
	public HpByLevel(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffector() == null) || (getEffected() == null))
		{
			return false;
		}
		// Calculation
		final int abs = (int) calc();
		final double absorb = ((getEffected().getCurrentHp() + abs) > getEffected().getMaxHp() ? getEffected().getMaxHp() : (getEffected().getCurrentHp() + abs));
		final int restored = (int) (absorb - getEffected().getCurrentHp());
		getEffected().setCurrentHp(absorb);
		// Status update
		final StatusUpdate su = new StatusUpdate(getEffected());
		su.addAttribute(StatusUpdate.CUR_HP, (int) absorb);
		getEffected().sendPacket(su);
		// System message
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
		sm.addInt(restored);
		getEffected().sendPacket(sm);
		return true;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
}
