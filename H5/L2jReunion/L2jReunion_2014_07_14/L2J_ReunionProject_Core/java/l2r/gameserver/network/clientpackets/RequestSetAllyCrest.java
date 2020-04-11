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

import l2r.gameserver.datatables.sql.ClanTable;
import l2r.gameserver.datatables.sql.CrestTable;
import l2r.gameserver.enums.CrestType;
import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2Crest;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;

/**
 * Client packet for setting ally crest.
 */
public final class RequestSetAllyCrest extends L2GameClientPacket
{
	private static final String _C__91_REQUESTSETALLYCREST = "[C] 91 RequestSetAllyCrest";
	
	private int _length;
	private byte[] _data = null;
	
	@Override
	protected void readImpl()
	{
		_length = readD();
		if (_length > 192)
		{
			return;
		}
		
		_data = new byte[_length];
		readB(_data);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (_length < 0)
		{
			activeChar.sendMessage("File transfer error.");
			return;
		}
		
		if (_length > 192)
		{
			activeChar.sendPacket(SystemMessageId.ADJUST_IMAGE_8_12);
			return;
		}
		
		if (activeChar.getAllyId() == 0)
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		
		final L2Clan leaderClan = ClanTable.getInstance().getClan(activeChar.getAllyId());
		
		if ((activeChar.getClanId() != leaderClan.getId()) || !activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
			return;
		}
		
		if (_length == 0)
		{
			if (leaderClan.getAllyCrestId() != 0)
			{
				leaderClan.changeAllyCrest(0, false);
			}
		}
		else
		{
			final L2Crest crest = CrestTable.getInstance().createCrest(_data, CrestType.ALLY);
			if (crest != null)
			{
				leaderClan.changeAllyCrest(crest.getId(), false);
				activeChar.sendPacket(SystemMessageId.CLAN_CREST_WAS_SUCCESSFULLY_REGISTRED);
			}
		}
		
	}
	
	@Override
	public String getType()
	{
		return _C__91_REQUESTSETALLYCREST;
	}
}
