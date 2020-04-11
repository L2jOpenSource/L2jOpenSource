package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Wooden-
 */
public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int pledgeType;
	private int hasSponsor;
	private final String name;
	private final int level;
	private final int classId;
	private final int objectId;
	private boolean isOnline;
	
	public PledgeShowMemberListUpdate(final L2PcInstance player)
	{
		activeChar = player;
		pledgeType = player.getPledgeType();
		if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			hasSponsor = activeChar.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			if (activeChar.isOnline())
			{
				hasSponsor = activeChar.isClanLeader() ? 1 : 0;
			}
			else
			{
				hasSponsor = 0;
			}
		}
		name = activeChar.getName();
		level = activeChar.getLevel();
		classId = activeChar.getClassId().getId();
		objectId = activeChar.getObjectId();
		isOnline = activeChar.isOnline();
	}
	
	public PledgeShowMemberListUpdate(final L2ClanMember player)
	{
		activeChar = player.getPlayerInstance();
		name = player.getName();
		level = player.getLevel();
		classId = player.getClassId();
		objectId = player.getObjectId();
		isOnline = player.isOnline();
		pledgeType = player.getPledgeType();
		if (pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			hasSponsor = activeChar.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			if (player.isOnline())
			{
				hasSponsor = activeChar.isClanLeader() ? 1 : 0;
			}
			else
			{
				hasSponsor = 0;
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x54);
		writeS(name);
		writeD(level);
		writeD(classId);
		writeD(0);
		writeD(objectId);
		writeD(isOnline ? 1 : 0); // 1=online 0=offline
		writeD(pledgeType);
		writeD(hasSponsor);
	}
	
	@Override
	public String getType()
	{
		return "[S] 54 PledgeShowMemberListUpdate";
	}
	
}
