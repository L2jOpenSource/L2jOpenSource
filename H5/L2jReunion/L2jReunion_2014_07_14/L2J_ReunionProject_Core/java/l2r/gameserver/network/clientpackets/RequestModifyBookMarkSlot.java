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

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author ShanSoft
 * @structure chddSdS
 */
public final class RequestModifyBookMarkSlot extends L2GameClientPacket
{
	private static final String _C__D0_51_02_REQUESTMODIFYBOOKMARKSLOT = "[C] D0:51:02 RequestModifyBookMarkSlot";
	
	private int id, icon;
	private String name, tag;
	
	@Override
	protected void readImpl()
	{
		id = readD();
		name = readS();
		icon = readD();
		tag = readS();
		
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		activeChar.teleportBookmarkModify(id, icon, tag, name);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_51_02_REQUESTMODIFYBOOKMARKSLOT;
	}
}
