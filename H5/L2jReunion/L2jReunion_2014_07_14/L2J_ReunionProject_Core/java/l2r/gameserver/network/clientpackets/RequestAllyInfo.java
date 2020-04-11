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

import l2r.gameserver.model.ClanInfo;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.AllianceInfo;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1479 $ $Date: 2005-11-09 00:47:42 +0100 (mer., 09 nov. 2005) $
 */
public final class RequestAllyInfo extends L2GameClientPacket
{
	private static final String _C__2E_REQUESTALLYINFO = "[C] 2E RequestAllyInfo";
	
	@Override
	public void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		SystemMessage sm;
		final int allianceId = activeChar.getAllyId();
		if (allianceId > 0)
		{
			final AllianceInfo ai = new AllianceInfo(allianceId);
			activeChar.sendPacket(ai);
			
			// send for player
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
			sm.addString(ai.getName());
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
			sm.addString(ai.getLeaderC());
			sm.addString(ai.getLeaderP());
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
			sm.addInt(ai.getOnline());
			sm.addInt(ai.getTotal());
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
			sm.addInt(ai.getAllies().length);
			activeChar.sendPacket(sm);
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_HEAD);
			for (final ClanInfo aci : ai.getAllies())
			{
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_NAME_S1);
				sm.addString(aci.getClan().getName());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEADER_S1);
				sm.addString(aci.getClan().getLeaderName());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_LEVEL_S1);
				sm.addInt(aci.getClan().getLevel());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
				sm.addInt(aci.getOnline());
				sm.addInt(aci.getTotal());
				activeChar.sendPacket(sm);
				
				sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
			}
			
			sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_INFO_FOOT);
			activeChar.sendPacket(sm);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__2E_REQUESTALLYINFO;
	}
}
