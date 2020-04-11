package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * format (c) dd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:40 $
 */
public class SetSummonRemainTime extends L2GameServerPacket
{
	private final int maxTime;
	private final int remainingTime;
	
	public SetSummonRemainTime(final int maxTime, final int remainingTime)
	{
		this.remainingTime = remainingTime;
		this.maxTime = maxTime;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd1);
		writeD(maxTime);
		writeD(remainingTime);
	}
	
	@Override
	public String getType()
	{
		return "[S] d1 SetSummonRemainTime";
	}
	
}
