package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class SendTradeDone extends L2GameServerPacket
{
	private final int num;
	
	public SendTradeDone(final int num)
	{
		this.num = num;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x22);
		writeD(num);
	}
	
	@Override
	public String getType()
	{
		return "[S] 22 SendTradeDone";
	}
	
}
