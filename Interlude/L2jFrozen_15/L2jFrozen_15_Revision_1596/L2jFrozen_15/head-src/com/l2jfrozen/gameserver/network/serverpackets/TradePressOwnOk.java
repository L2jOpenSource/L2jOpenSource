package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author Beetle
 */
public final class TradePressOwnOk extends L2GameServerPacket
{
	public static final TradePressOwnOk STATIC_PACKET = new TradePressOwnOk();
	
	private TradePressOwnOk()
	{
	}
	
	@Override
	public String getType()
	{
		return "[S] 75 TradePressOwnOk";
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x75);
	}
}