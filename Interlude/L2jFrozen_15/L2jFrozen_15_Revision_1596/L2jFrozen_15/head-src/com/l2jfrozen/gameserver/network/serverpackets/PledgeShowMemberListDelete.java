package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowMemberListDelete extends L2GameServerPacket
{
	private final String player;
	
	public PledgeShowMemberListDelete(final String playerName)
	{
		player = playerName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x56);
		writeS(player);
	}
	
	@Override
	public String getType()
	{
		return "[S] 56 PledgeShowMemberListDelete";
	}
}
