/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.data.xml.impl.AdminData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.CastleManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.TerritoryWarManager;
import l2r.gameserver.model.L2AccessLevel;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2SiegeClan;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;

import gr.sr.configsEngine.configs.impl.ChaoticZoneConfigs;
import gr.sr.configsEngine.configs.impl.FlagZoneConfigs;
import gr.sr.interf.SunriseEvents;

public class Die extends L2GameServerPacket
{
	private final int _charObjId;
	private boolean _canTeleport;
	private final boolean _sweepable;
	private L2AccessLevel _access = AdminData.getInstance().getAccessLevel(0);
	private L2Clan _clan;
	private final L2Character _activeChar;
	private boolean _isJailed;
	private boolean _staticRes = false;
	
	/**
	 * @param cha
	 */
	public Die(L2Character cha)
	{
		_charObjId = cha.getObjectId();
		_activeChar = cha;
		if (cha.isPlayer())
		{
			L2PcInstance player = (L2PcInstance) cha;
			_access = player.getAccessLevel();
			_clan = player.getClan();
			_isJailed = player.isJailed();
		}
		_canTeleport = !cha.isPendingRevive();
		_sweepable = cha.isSweepActive();
		
		if (cha.isPlayer())
		{
			L2PcInstance activeChar = cha.getActingPlayer();
			if (activeChar.isInsideZone(ZoneIdType.ZONE_CHAOTIC) && ChaoticZoneConfigs.ENABLE_CHAOTIC_ZONE_AUTO_REVIVE)
			{
				_canTeleport = false;
			}
			
			if (activeChar.isInsideZone(ZoneIdType.FLAG) && FlagZoneConfigs.ENABLE_FLAG_ZONE_AUTO_REVIVE)
			{
				_canTeleport = false;
			}
			
			if (SunriseEvents.isInEvent(activeChar))
			{
				if (!SunriseEvents.canShowToVillageWindow(activeChar))
				{
					_canTeleport = false;
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x00);
		writeD(_charObjId);
		writeD(_canTeleport ? 0x01 : 0x00);
		
		if (_activeChar.isPlayer())
		{
			if (!OlympiadManager.getInstance().isRegistered(_activeChar.getActingPlayer()) && !_activeChar.isOnEvent())
			{
				_staticRes = _activeChar.getInventory().haveItemForSelfResurrection();
			}
			
			// Verify if player can use fixed resurrection without Feather
			if (_access.allowFixedRes())
			{
				_staticRes = true;
			}
		}
		
		if (_canTeleport && (_clan != null) && !_isJailed)
		{
			boolean isInCastleDefense = false;
			boolean isInFortDefense = false;
			
			L2SiegeClan siegeClan = null;
			Castle castle = CastleManager.getInstance().getCastle(_activeChar);
			Fort fort = FortManager.getInstance().getFort(_activeChar);
			SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(_activeChar);
			if ((castle != null) && castle.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = castle.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && castle.getSiege().checkIsDefender(_clan))
				{
					isInCastleDefense = true;
				}
			}
			else if ((fort != null) && fort.getSiege().isInProgress())
			{
				// siege in progress
				siegeClan = fort.getSiege().getAttackerClan(_clan);
				if ((siegeClan == null) && fort.getSiege().checkIsDefender(_clan))
				{
					isInFortDefense = true;
				}
			}
			
			writeD(_clan.getHideoutId() > 0 ? 0x01 : 0x00); // 6d 01 00 00 00 - to hide away
			writeD((_clan.getCastleId() > 0) || isInCastleDefense ? 0x01 : 0x00); // 6d 02 00 00 00 - to castle
			// vGodFather territory flag fix
			//@formatter:off
			writeD((TerritoryWarManager.getInstance().getHQForClan(_clan) != null)
				|| (TerritoryWarManager.getInstance().getFlagForClan(_clan) != null)
				|| ((siegeClan != null) && !isInCastleDefense && !isInFortDefense && !siegeClan.getFlag().isEmpty())
				|| ((hall != null) && hall.getSiege().checkIsAttacker(_clan)) ? 0x01 : 0x00); // 6d 03 00 00 00 - to siege HQ
			//@formatter:on
			writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
			writeD(_staticRes ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			writeD((_clan.getFortId() > 0) || isInFortDefense ? 0x01 : 0x00); // 6d 05 00 00 00 - to fortress
		}
		else
		{
			writeD(0x00); // 6d 01 00 00 00 - to hide away
			writeD(0x00); // 6d 02 00 00 00 - to castle
			writeD(0x00); // 6d 03 00 00 00 - to siege HQ
			writeD(_sweepable ? 0x01 : 0x00); // sweepable (blue glow)
			writeD(_staticRes ? 0x01 : 0x00); // 6d 04 00 00 00 - to FIXED
			writeD(0x00); // 6d 05 00 00 00 - to fortress
		}
		// TODO: protocol 152
		// writeC(0); // show die animation
		// writeD(0); // agathion ress button
		// writeD(0); // additional free space
	}
}
