package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public class RequestGiveNickName extends L2GameClientPacket
{
	static Logger LOGGER = Logger.getLogger(RequestGiveNickName.class);
	
	private String target;
	private String title;
	
	@Override
	protected void readImpl()
	{
		target = readS();
		title = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Noblesse can bestow a title to themselves
		if (activeChar.isNoble() && target.matches(activeChar.getName()))
		{
			activeChar.setTitle(title);
			final SystemMessage sm = new SystemMessage(SystemMessageId.TITLE_CHANGED);
			activeChar.sendPacket(sm);
			activeChar.broadcastTitleInfo();
		}
		// Can the player change/give a title?
		else if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_GIVE_TITLE) == L2Clan.CP_CL_GIVE_TITLE)
		{
			if (activeChar.getClan().getLevel() < 3)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_LVL_3_NEEDED_TO_ENDOWE_TITLE);
				activeChar.sendPacket(sm);
				sm = null;
				return;
			}
			
			final L2ClanMember member1 = activeChar.getClan().getClanMember(target);
			if (member1 != null)
			{
				final L2PcInstance member = member1.getPlayerInstance();
				if (member != null)
				{
					// is target from the same clan?
					member.setTitle(title);
					SystemMessage sm = new SystemMessage(SystemMessageId.TITLE_CHANGED);
					member.sendPacket(sm);
					member.broadcastTitleInfo();
					sm = null;
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
					sm.addString("Target needs to be online to get a title");
					activeChar.sendPacket(sm);
					sm = null;
				}
			}
			else
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Target does not belong to your clan");
				activeChar.sendPacket(sm);
				sm = null;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 55 RequestGiveNickName";
	}
}
