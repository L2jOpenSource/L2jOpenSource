/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.zone.type;

import l2r.Config;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.zone.L2ZoneType;

/**
 * A Peace Zone
 * @author durgus
 */
public class L2PeaceZone extends L2ZoneType
{
	public L2PeaceZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			L2PcInstance player = character.getActingPlayer();
			if (player.isCombatFlagEquipped() && TerritoryWarManager.getInstance().isTWInProgress())
			{
				TerritoryWarManager.getInstance().dropCombatFlag(player, false, true);
			}
			
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if ((player.getSiegeState() != 0) && (Config.PEACE_ZONE_MODE == 1))
			{
				return;
			}
		}
		
		if (Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(ZoneIdType.PEACE, true);
			
			if (character.isPlayer())
			{
				L2PcInstance player = character.getActingPlayer();
				player.pauseAdventTask(true);
			}
		}
		
		if (!getAllowStore())
		{
			character.setInsideZone(ZoneIdType.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (Config.PEACE_ZONE_MODE != 2)
		{
			character.setInsideZone(ZoneIdType.PEACE, false);
		}
		
		if (!getAllowStore())
		{
			character.setInsideZone(ZoneIdType.NO_STORE, false);
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
}
