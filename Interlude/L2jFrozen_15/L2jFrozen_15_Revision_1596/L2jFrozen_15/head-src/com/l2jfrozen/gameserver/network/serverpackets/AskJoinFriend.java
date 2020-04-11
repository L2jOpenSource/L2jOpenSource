package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample
 * <p>
 * 7d c1 b2 e0 4a 00 00 00 00
 * <p>
 * format cdd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AskJoinFriend extends L2GameServerPacket
{
	private final String requestorName;
	
	public AskJoinFriend(final String requestorName)
	{
		this.requestorName = requestorName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7d);
		writeS(requestorName);
		writeD(0);
	}
	
	@Override
	public String getType()
	{
		return "[S] 7d AskJoinFriend 0x7d";
	}
}
