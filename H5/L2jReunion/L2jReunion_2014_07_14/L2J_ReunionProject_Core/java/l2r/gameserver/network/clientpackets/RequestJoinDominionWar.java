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

package l2r.gameserver.network.clientpackets;

import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExShowDominionRegistry;

/**
 * @author Gigiikun
 */
public final class RequestJoinDominionWar extends L2GameClientPacket
{
	private static final String _C__D0_57_REQUESTJOINDOMINIONWAR = "[C] D0:57 RequestJoinDominionWar";
	
	private int _territoryId;
	private int _isClan;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_territoryId = readD();
		_isClan = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		L2Clan clan = activeChar.getClan();
		int castleId = _territoryId - 80;
		
		if (TerritoryWarManager.getInstance().getIsRegistrationOver())
		{
			activeChar.sendPacket(SystemMessageId.NOT_TERRITORY_REGISTRATION_PERIOD);
			return;
		}
		else if ((clan != null) && (TerritoryWarManager.getInstance().getTerritory(castleId).getOwnerClan() == clan))
		{
			activeChar.sendPacket(SystemMessageId.THE_TERRITORY_OWNER_CLAN_CANNOT_PARTICIPATE_AS_MERCENARIES);
			return;
		}
		
		if (_isClan == 0x01)
		{
			if (!activeChar.hasClanPrivilege(ClanPrivilege.CS_MANAGE_SIEGE))
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (clan == null)
			{
				return;
			}
			
			if (_isJoining == 1)
			{
				if (System.currentTimeMillis() < clan.getDissolvingExpiryTime())
				{
					activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
					return;
				}
				else if (TerritoryWarManager.getInstance().checkIsRegistered(-1, clan))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
					return;
				}
				TerritoryWarManager.getInstance().registerClan(castleId, clan);
			}
			else
			{
				TerritoryWarManager.getInstance().removeClan(castleId, clan);
			}
		}
		else
		{
			if ((activeChar.getLevel() < 40) || (activeChar.getClassId().level() < 2))
			{
				// TODO: punish player
				return;
			}
			if (_isJoining == 1)
			{
				if (TerritoryWarManager.getInstance().checkIsRegistered(-1, activeChar.getObjectId()))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
					return;
				}
				else if ((clan != null) && TerritoryWarManager.getInstance().checkIsRegistered(-1, clan))
				{
					activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
					return;
				}
				TerritoryWarManager.getInstance().registerMerc(castleId, activeChar);
			}
			else
			{
				TerritoryWarManager.getInstance().removeMerc(castleId, activeChar);
			}
		}
		activeChar.sendPacket(new ExShowDominionRegistry(castleId, activeChar));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_57_REQUESTJOINDOMINIONWAR;
	}
}
