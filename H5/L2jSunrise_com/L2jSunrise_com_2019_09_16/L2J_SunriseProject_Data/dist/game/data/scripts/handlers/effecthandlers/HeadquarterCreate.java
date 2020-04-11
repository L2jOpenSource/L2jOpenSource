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

import l2r.gameserver.data.sql.NpcTable;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeFlagInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.model.stats.Env;

/**
 * Headquarter Create effect implementation.
 * @author Adry_85
 */
public final class HeadquarterCreate extends L2Effect
{
	private static final int HQ_NPC_ID = 35062;
	private boolean _isAdvanced = false;
	
	public HeadquarterCreate(Env env, EffectTemplate template)
	{
		super(env, template);
		
		if (template.getParameters() != null)
		{
			_isAdvanced = template.getParameters().getBoolean("isAdvanced", false);
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
		final L2PcInstance player = getEffector().getActingPlayer();
		if (!player.isClanLeader())
		{
			return false;
		}
		
		final L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, NpcTable.getInstance().getTemplate(HQ_NPC_ID), _isAdvanced, false);
		flag.setTitle(player.getClan().getName());
		flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
		flag.setHeading(player.getHeading());
		flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
		final Castle castle = CastleManager.getInstance().getCastle(player);
		final Fort fort = FortManager.getInstance().getFort(player);
		final SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(player);
		
		// vGodFather territory flag fix
		if (TerritoryWarManager.getInstance().isTWInProgress())
		{
			TerritoryWarManager.getInstance().addClanFlag(player.getClan(), flag);
		}
		else if ((castle != null) && !castle.getSiege().getFlag(player.getClan()).contains(flag))
		{
			castle.getSiege().getFlag(player.getClan()).add(flag);
		}
		else if ((fort != null) && !fort.getSiege().getFlag(player.getClan()).contains(flag))
		{
			fort.getSiege().getFlag(player.getClan()).add(flag);
		}
		else if ((hall != null) && !hall.getSiege().getFlag(player.getClan()).contains(flag))
		{
			hall.getSiege().getFlag(player.getClan()).add(flag);
		}
		return true;
	}
}
