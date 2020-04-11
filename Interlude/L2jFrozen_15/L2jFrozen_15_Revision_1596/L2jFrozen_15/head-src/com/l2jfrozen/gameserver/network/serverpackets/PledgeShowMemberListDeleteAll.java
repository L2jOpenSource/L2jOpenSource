package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeShowMemberListDeleteAll extends L2GameServerPacket
{
	
	public PledgeShowMemberListDeleteAll()
	{
		
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x82);
	}
	
	@Override
	public String getType()
	{
		return "[S] 82 PledgeShowMemberListDeleteAll";
	}
}
