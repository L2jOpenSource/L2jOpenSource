package com.l2jfrozen.gameserver.network.serverpackets;

public class EnchantResult extends L2GameServerPacket
{
	private final int unknown;
	
	public EnchantResult(final int unknown)
	{
		this.unknown = unknown;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x81);
		writeD(unknown);
	}
	
	@Override
	public String getType()
	{
		return "[S] 81 EnchantResult";
	}
}
