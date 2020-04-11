package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample
 * <p>
 * 7d c1 b2 e0 4a 00 00 00 00
 * <p>
 * format cdd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AskJoinAlly extends L2GameServerPacket
{
	private final String requestorName;
	private final int requestorObjId;
	
	/**
	 * @param requestorObjId
	 * @param requestorName
	 */
	public AskJoinAlly(final int requestorObjId, final String requestorName)
	{
		this.requestorName = requestorName;
		this.requestorObjId = requestorObjId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xa8);
		writeD(requestorObjId);
		writeS(requestorName);
	}
	
	@Override
	public String getType()
	{
		return "[S] a8 AskJoinAlly 0xa8";
	}
	
}
