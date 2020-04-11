package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private String name;
	private int lvl;
	private int classId;
	private int isOnline;
	private int pledgeType;
	
	public PledgeShowMemberListAdd(final L2PcInstance player)
	{
		name = player.getName();
		lvl = player.getLevel();
		classId = player.getClassId().getId();
		isOnline = player.isOnline() ? player.getObjectId() : 0;
		pledgeType = player.getPledgeType();
	}
	
	public PledgeShowMemberListAdd(final L2ClanMember cm)
	{
		try
		{
			name = cm.getName();
			lvl = cm.getLevel();
			classId = cm.getClassId();
			isOnline = cm.isOnline() ? cm.getObjectId() : 0;
			pledgeType = cm.getPledgeType();
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x55);
		writeS(name);
		writeD(lvl);
		writeD(classId);
		writeD(0);
		writeD(1);
		writeD(isOnline);
		writeD(pledgeType);
	}
	
	@Override
	public String getType()
	{
		return "[S] 55 PledgeShowMemberListAdd";
	}
	
}
