package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;

public class AllyInfo extends L2GameServerPacket
{
	private final L2PcInstance character;
	
	public AllyInfo(final L2PcInstance cha)
	{
		character = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getAllyId() == 0)
		{
			character.sendPacket(new SystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
			return;
		}
		
		// ======<AllyInfo>======
		SystemMessage sm = new SystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
		character.sendPacket(sm);
		// ======<Ally Name>======
		sm = new SystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
		sm.addString(character.getClan().getAllyName());
		character.sendPacket(sm);
		int online = 0;
		int count = 0;
		int clancount = 0;
		for (final L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == character.getAllyId())
			{
				clancount++;
				online += clan.getOnlineMembers("").length;
				count += clan.getMembers().length;
			}
		}
		// Connection
		sm = new SystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
		sm.addString("" + online);
		sm.addString("" + count);
		character.sendPacket(sm);
		final L2Clan leaderclan = ClanTable.getInstance().getClan(character.getAllyId());
		sm = new SystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
		sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		character.sendPacket(sm);
		// clan count
		sm = new SystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
		sm.addString("" + clancount);
		character.sendPacket(sm);
		// clan information
		sm = new SystemMessage(SystemMessageId.CLAN_INFO_HEAD);
		character.sendPacket(sm);
		for (final L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == character.getAllyId())
			{
				// clan name
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_NAME);
				sm.addString(clan.getName());
				character.sendPacket(sm);
				// clan leader name
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_LEADER);
				sm.addString(clan.getLeaderName());
				character.sendPacket(sm);
				// clan level
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_LEVEL);
				sm.addNumber(clan.getLevel());
				character.sendPacket(sm);
				// ---------
				sm = new SystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
				character.sendPacket(sm);
			}
		}
		// =========================
		sm = new SystemMessage(SystemMessageId.CLAN_INFO_FOOT);
		character.sendPacket(sm);
	}
	
	@Override
	public String getType()
	{
		return "[S] 7a AllyInfo";
	}
}
