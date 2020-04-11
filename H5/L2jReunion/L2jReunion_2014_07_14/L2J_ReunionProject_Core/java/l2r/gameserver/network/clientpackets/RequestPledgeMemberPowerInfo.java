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

import l2r.gameserver.model.L2Clan;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.PledgeReceivePowerInfo;

/**
 * Format: (ch) dS
 * @author -Wooden-
 */
public final class RequestPledgeMemberPowerInfo extends L2GameClientPacket
{
	private static final String _C__D0_14_REQUESTPLEDGEMEMBERPOWERINFO = "[C] D0:14 RequestPledgeMemberPowerInfo";
	
	@SuppressWarnings("unused")
	private int _unk1;
	private String _player;
	
	@Override
	protected void readImpl()
	{
		_unk1 = readD();
		_player = readS();
	}
	
	@Override
	protected void runImpl()
	{
		// _log.info("C5: RequestPledgeMemberPowerInfo d:"+_unk1);
		// _log.info("C5: RequestPledgeMemberPowerInfo S:"+_player);
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		// do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if (clan == null)
		{
			return;
		}
		L2ClanMember member = clan.getClanMember(_player);
		if (member == null)
		{
			return;
		}
		activeChar.sendPacket(new PledgeReceivePowerInfo(member));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_14_REQUESTPLEDGEMEMBERPOWERINFO;
	}
}