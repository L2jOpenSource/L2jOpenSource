package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:40 $
 */
public class SendTradeRequest extends L2GameServerPacket
{
	private final int senderID;
	
	public SendTradeRequest(final int senderID)
	{
		this.senderID = senderID;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5e);
		writeD(senderID);
	}
	
	@Override
	public String getType()
	{
		return "[S] 5e SendTradeRequest";
	}
}
