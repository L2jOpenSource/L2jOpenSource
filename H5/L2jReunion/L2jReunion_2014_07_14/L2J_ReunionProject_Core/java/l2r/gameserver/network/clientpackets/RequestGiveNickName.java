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

import l2r.gameserver.model.ClanPrivilege;
import l2r.gameserver.model.L2ClanMember;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;

public class RequestGiveNickName extends L2GameClientPacket
{
	private static final String _C__0B_REQUESTGIVENICKNAME = "[C] 0B RequestGiveNickName";
	
	private String _target;
	private String _title;
	
	@Override
	protected void readImpl()
	{
		_target = readS();
		_title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Noblesse can bestow a title to themselves
		if (activeChar.isNoble() && _target.equalsIgnoreCase(activeChar.getName()))
		{
			activeChar.setTitle(_title);
			activeChar.sendPacket(SystemMessageId.TITLE_CHANGED);
			activeChar.broadcastTitleInfo();
		}
		else
		{
			// Can the player change/give a title?
			if (!activeChar.hasClanPrivilege(ClanPrivilege.CL_GIVE_TITLE))
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}
			
			if (activeChar.getClan().getLevel() < 3)
			{
				activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
				return;
			}
			
			L2ClanMember member1 = activeChar.getClan().getClanMember(_target);
			if (member1 != null)
			{
				L2PcInstance member = member1.getPlayerInstance();
				if (member != null)
				{
					// is target from the same clan?
					member.setTitle(_title);
					member.sendPacket(SystemMessageId.TITLE_CHANGED);
					member.broadcastTitleInfo();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__0B_REQUESTGIVENICKNAME;
	}
}
