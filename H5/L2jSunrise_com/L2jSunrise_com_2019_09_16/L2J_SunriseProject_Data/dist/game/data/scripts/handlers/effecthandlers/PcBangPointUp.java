/*
 * Copyright (C) 2004-2015 L2J DataPack
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
import l2r.gameserver.network.serverpackets.ExPCCafePointInfo;

/**
 * Give Recommendation effect implementation.
 * @author vGodFather
 */
public final class PcBangPointUp extends L2Effect
{
	private final int _amount;
	
	public PcBangPointUp(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_amount = template.getParameters().getInt("amount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		L2PcInstance target = getEffected() instanceof L2PcInstance ? (L2PcInstance) getEffected() : null;
		if (target == null)
		{
			return false;
		}
		
		int pointsToGive = _amount;
		
		target.setPcBangPoints(pointsToGive);
		target.sendPacket(new ExPCCafePointInfo(target.getPcBangPoints(), pointsToGive, true, false, 1));
		return true;
	}
}
