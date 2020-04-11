package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PartySmallWindowDelete extends L2GameServerPacket
{
	private final L2PcInstance member;
	
	public PartySmallWindowDelete(final L2PcInstance member)
	{
		this.member = member;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x51);
		writeD(member.getObjectId());
		writeS(member.getName());
	}
	
	@Override
	public String getType()
	{
		return "[S] 51 PartySmallWindowDelete";
	}
}
