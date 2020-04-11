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

import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.ClanHallManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.entity.ClanHall;
import l2r.gameserver.model.entity.clanhall.AuctionableHall;
import l2r.gameserver.network.serverpackets.AgitDecoInfo;

/**
 * A clan hall zone
 * @author durgus
 */
public class L2ClanHallZone extends L2ResidenceZone
{
	public L2ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			setResidenceId(Integer.parseInt(value));
			// Register self to the correct clan hall
			ClanHall hall = ClanHallManager.getInstance().getClanHallById(getResidenceId());
			if (hall == null)
			{
				_log.warn("L2ClanHallZone: Clan hall with id " + getResidenceId() + " does not exist!");
			}
			else
			{
				hall.setZone(this);
			}
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			// Set as in clan hall
			character.setInsideZone(ZoneIdType.CLAN_HALL, true);
			
			AuctionableHall clanHall = ClanHallManager.getInstance().getAuctionableHallById(getResidenceId());
			if (clanHall == null)
			{
				return;
			}
			
			// Send decoration packet
			AgitDecoInfo deco = new AgitDecoInfo(clanHall);
			character.sendPacket(deco);
			
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			character.setInsideZone(ZoneIdType.CLAN_HALL, false);
		}
	}
	
	public void banishForeigners(int owningClanId)
	{
		super.banishForeigners(owningClanId, TeleportWhereType.ClanHall_banish);
	}
}
