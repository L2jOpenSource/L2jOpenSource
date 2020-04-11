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

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.instance.L2ChestInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;

/**
 * Open Chest effect implementation.
 * @author Adry_85
 */
public final class OpenChest extends L2Effect
{
	public OpenChest(Env env, EffectTemplate template)
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
		if (!(getEffected() instanceof L2ChestInstance))
		{
			return false;
		}
		
		final L2PcInstance player = getEffector().getActingPlayer();
		final L2ChestInstance chest = (L2ChestInstance) getEffected();
		if (chest.isDead() || (player.getInstanceId() != chest.getInstanceId()))
		{
			return false;
		}
		
		if (((player.getLevel() <= 77) && (Math.abs(chest.getLevel() - player.getLevel()) <= 6)) || ((player.getLevel() >= 78) && (Math.abs(chest.getLevel() - player.getLevel()) <= 5)))
		{
			player.broadcastSocialAction(3);
			// chest.setSpecialDrop();
			chest.setMustRewardExpSp(false);
			chest.reduceCurrentHp(chest.getMaxHp(), player, getSkill());
		}
		else
		{
			player.broadcastSocialAction(13);
			chest.addDamageHate(player, 0, 1);
			chest.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return true;
	}
}
