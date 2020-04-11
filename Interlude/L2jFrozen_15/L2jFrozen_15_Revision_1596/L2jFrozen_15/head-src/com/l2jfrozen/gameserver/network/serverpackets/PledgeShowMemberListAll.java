package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Clan.SubPledge;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

//import org.apache.log4j.Logger;
/**
 * sample 0000: 68 b1010000 48 00 61 00 6d 00 62 00 75 00 72 00 67 00 00 00 H.a.m.b.u.r.g... 43 00 61 00 6c 00 61 00 64 00 6f 00 6e 00 00 00 C.a.l.a.d.o.n... 00000000 crestid | not used (nuocnam) 00000000 00000000 00000000 00000000 22000000 00000000 00000000 00000000 ally id 00 00 ally name 00000000
 * ally crrest id 02000000 6c 00 69 00 74 00 68 00 69 00 75 00 6d 00 31 00 00 00 l.i.t.h.i.u.m... 0d000000 level 12000000 class id 00000000 01000000 offline 1=true 00000000 45 00 6c 00 61 00 6e 00 61 00 00 00 E.l.a.n.a... 08000000 19000000 01000000 01000000 00000000 format dSS dddddddddSdd d
 * (Sddddd) dddSS dddddddddSdd d (Sdddddd)
 * @version $Revision: 1.6.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final L2Clan clan;
	private final L2PcInstance activeChar;
	private final L2ClanMember[] members;
	private int pledgeType;
	
	// private static Logger LOGGER = Logger.getLogger(PledgeShowMemberListAll.class);
	
	public PledgeShowMemberListAll(final L2Clan clan, final L2PcInstance activeChar)
	{
		this.clan = clan;
		this.activeChar = activeChar;
		members = this.clan.getMembers();
	}
	
	@Override
	protected final void writeImpl()
	{
		
		pledgeType = 0;
		writePledge(0);
		
		final SubPledge[] subPledge = clan.getAllSubPledges();
		for (final SubPledge element : subPledge)
		{
			activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(element));
		}
		
		for (final L2ClanMember m : members)
		{
			if (m.getPledgeType() == 0)
			{
				continue;
			}
			activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		}
		
		// unless this is sent sometimes, the client doesn't recognise the player as the leader
		activeChar.sendPacket(new UserInfo(activeChar));
		
	}
	
	void writePledge(final int mainOrSubpledge)
	{
		final int TOP = ClanTable.getInstance().getTopRate(clan.getClanId());
		
		writeC(0x53);
		
		writeD(mainOrSubpledge); // c5 main clan 0 or any subpledge 1?
		writeD(clan.getClanId());
		writeD(pledgeType); // c5 - possibly pledge type?
		writeS(clan.getName());
		writeS(clan.getLeaderName());
		
		writeD(clan.getCrestId()); // crest id .. is used again
		writeD(clan.getLevel());
		writeD(clan.getCastleId());
		writeD(clan.getHasHideout());
		writeD(TOP);
		writeD(clan.getReputationScore()); // was activechar lvl
		writeD(0); // 0
		writeD(0); // 0
		
		writeD(clan.getAllyId());
		writeS(clan.getAllyName());
		writeD(clan.getAllyCrestId());
		writeD(clan.isAtWar());
		writeD(clan.getSubPledgeMembersCount(pledgeType));
		
		int yellow;
		for (final L2ClanMember m : members)
		{
			if (m.getPledgeType() != pledgeType)
			{
				continue;
			}
			if (m.getPledgeType() == -1)
			{
				yellow = m.getSponsor() != 0 ? 1 : 0;
			}
			else if (m.getPlayerInstance() != null)
			{
				yellow = m.getPlayerInstance().isClanLeader() ? 1 : 0;
			}
			else
			{
				yellow = 0;
			}
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			writeD(0);
			writeD(m.getObjectId());
			writeD(m.isOnline() ? 1 : 0);
			writeD(yellow);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 53 PledgeShowMemberListAll";
	}
	
}
