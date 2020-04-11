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

import java.util.HashSet;
import java.util.Set;

import l2r.gameserver.data.sql.BotReportTable;
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
public final class BlockAction extends L2Effect
{
	private final Set<Integer> _blockedActions = new HashSet<>();
	
	/**
	 * @param env
	 * @param template
	 */
	public BlockAction(Env env, EffectTemplate template)
	{
		super(env, template);
		
		final String[] actions = template.getParameters().getString("blockedActions").split(",");
		for (String action : actions)
		{
			_blockedActions.add(Integer.parseInt(action));
		}
	}
	
	@Override
	public boolean onStart()
	{
		if ((getEffected() == null) || !getEffected().isPlayer())
		{
			return false;
		}
		
		if (_blockedActions.contains(BotReportTable.PARTY_ACTION_BLOCK_ID))
		{
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN, 0, "block action debuff", "system"));
		}
		
		if (_blockedActions.contains(BotReportTable.CHAT_BLOCK_ID))
		{
			PunishmentManager.getInstance().startPunishment(new PunishmentTask(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN, 0, "block action debuff", "system"));
		}
		
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_blockedActions.contains(BotReportTable.PARTY_ACTION_BLOCK_ID))
		{
			PunishmentManager.getInstance().stopPunishment(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
		}
		if (_blockedActions.contains(BotReportTable.CHAT_BLOCK_ID))
		{
			PunishmentManager.getInstance().stopPunishment(getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN);
		}
	}
	
	@Override
	public boolean checkCondition(Object id)
	{
		return !_blockedActions.contains(id);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ACTION_BLOCK;
	}
}
