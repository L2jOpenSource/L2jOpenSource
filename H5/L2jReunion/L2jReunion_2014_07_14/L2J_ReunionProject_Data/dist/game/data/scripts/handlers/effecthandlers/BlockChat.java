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

import l2r.gameserver.instancemanager.PunishmentManager;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.punishment.PunishmentAffect;
import l2r.gameserver.model.punishment.PunishmentTask;
import l2r.gameserver.model.punishment.PunishmentType;
import l2r.gameserver.model.stats.Env;

/**
 * @author BiggBoss
 */
public final class BlockChat extends L2Effect
{
	public BlockChat(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CHAT_BLOCK;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer())
		{
			return false;
		}
		
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN, 0, "Chat banned bot report", "system"));
		return true;
	}
	
	@Override
	public void onExit()
	{
		PunishmentManager.getInstance().stopPunishment(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
}
