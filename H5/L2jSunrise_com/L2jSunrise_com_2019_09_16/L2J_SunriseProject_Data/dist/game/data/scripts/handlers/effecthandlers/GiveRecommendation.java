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
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Give Recommendation effect implementation.
 * @author NosBit
 */
public final class GiveRecommendation extends L2Effect
{
	private final int _amount;
	
	public GiveRecommendation(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_amount = template.getParameters().getInt("amount", 0);
		if (_amount == 0)
		{
			_log.warn(getClass().getSimpleName() + ": amount parameter is missing or set to 0. id:" + template.getParameters().getInt("id", -1));
		}
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
		
		int recommendationsGiven = _amount;
		
		if ((target.getRecomHave() + _amount) >= 255)
		{
			recommendationsGiven = 255 - target.getRecomHave();
		}
		
		if (recommendationsGiven > 0)
		{
			target.setRecomHave(target.getRecomHave() + recommendationsGiven);
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
			sm.addInt(recommendationsGiven);
			target.sendPacket(sm);
			target.sendUserInfo(true);
			target.sendPacket(new ExVoteSystemInfo(target));
		}
		else
		{
			L2PcInstance player = getEffector() instanceof L2PcInstance ? (L2PcInstance) getEffector() : null;
			if (player != null)
			{
				player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			}
		}
		return true;
	}
}
