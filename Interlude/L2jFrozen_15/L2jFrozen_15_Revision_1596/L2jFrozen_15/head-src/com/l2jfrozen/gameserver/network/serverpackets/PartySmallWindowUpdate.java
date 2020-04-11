package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySmallWindowUpdate extends L2GameServerPacket
{
	private final L2PcInstance member;
	
	public PartySmallWindowUpdate(final L2PcInstance member)
	{
		this.member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x52);
		writeD(member.getObjectId());
		writeS(member.getName());
		
		writeD((int) member.getCurrentCp()); // c4
		writeD(member.getMaxCp()); // c4
		
		writeD((int) member.getCurrentHp());
		writeD(member.getMaxHp());
		writeD((int) member.getCurrentMp());
		writeD(member.getMaxMp());
		writeD(member.getLevel());
		writeD(member.getClassId().getId());
		
	}
	
	@Override
	public String getType()
	{
		return "[S] 52 PartySmallWindowUpdate";
	}
}
