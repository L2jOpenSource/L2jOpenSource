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

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import l2r.gameserver.util.Util;

/**
 * @author KenM
 */
public class SetPrivateStoreWholeMsg extends L2GameClientPacket
{
	private static final String _C_D0_4A_SETPRIVATESTOREWHOLEMSG = "[C] D0:4A SetPrivateStoreWholeMsg";
	private static final int MAX_MSG_LENGTH = 29;
	
	private String _msg;
	
	@Override
	protected void readImpl()
	{
		_msg = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if ((player == null) || (player.getSellList() == null))
		{
			return;
		}
		
		if ((_msg != null) && (_msg.length() > MAX_MSG_LENGTH))
		{
			Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to overflow private store whole message", Config.DEFAULT_PUNISH);
			return;
		}
		
		player.getSellList().setTitle(_msg);
		sendPacket(new ExPrivateStoreSetWholeMsg(player));
	}
	
	@Override
	public String getType()
	{
		return _C_D0_4A_SETPRIVATESTOREWHOLEMSG;
	}
}
