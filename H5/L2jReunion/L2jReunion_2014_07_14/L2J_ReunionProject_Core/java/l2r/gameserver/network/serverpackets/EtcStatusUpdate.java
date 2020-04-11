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
package l2r.gameserver.network.serverpackets;

import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectFlag;

/**
 * @author Luca Baldi
 */
public class EtcStatusUpdate extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	
	public EtcStatusUpdate(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xf9); // several icons to a separate line (0 = disabled)
		writeD(_activeChar.getCharges()); // 1-7 increase force, lvl
		writeD(_activeChar.getWeightPenalty()); // 1-4 weight penalty, lvl (1=50%, 2=66.6%, 3=80%, 4=100%)
		writeD((_activeChar.getMessageRefusal() || _activeChar.isChatBanned() || _activeChar.isSilenceMode()) ? 1 : 0); // 1 = block all chat
		writeD(_activeChar.isInsideZone(ZoneIdType.DANGER_AREA) ? 1 : 0); // 1 = danger area
		writeD(_activeChar.getExpertiseWeaponPenalty()); // Weapon Grade Penalty [1-4]
		writeD(_activeChar.getExpertiseArmorPenalty()); // Armor Grade Penalty [1-4]
		writeD(_activeChar.isAffected(EffectFlag.CHARM_OF_COURAGE) ? 1 : 0); // 1 = charm of courage (allows resurrection on the same spot upon death on the siege battlefield)
		writeD(_activeChar.getDeathPenaltyBuffLevel()); // 1-15 death penalty, lvl (combat ability decreased due to death)
		writeD(_activeChar.getChargedSouls());
	}
}
