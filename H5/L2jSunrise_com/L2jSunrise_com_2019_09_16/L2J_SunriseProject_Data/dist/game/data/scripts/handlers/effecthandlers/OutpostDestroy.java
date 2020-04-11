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

import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Outpost Destroy effect implementation.
 * @author UnAfraid
 */
public class OutpostDestroy extends L2Effect
{
	public OutpostDestroy(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		final L2PcInstance player = getEffector().getActingPlayer();
		if (!player.isClanLeader() || (player.getClan().getCastleId() <= 0))
		{
			return false;
		}
		
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			final L2SiegeFlagInstance flag = TerritoryWarManager.getInstance().getHQForClan(player.getClan());
			if (flag != null)
			{
				flag.deleteMe();
			}
			TerritoryWarManager.getInstance().removeHQForClan(player.getClan());
		}
		return true;
	}
}
