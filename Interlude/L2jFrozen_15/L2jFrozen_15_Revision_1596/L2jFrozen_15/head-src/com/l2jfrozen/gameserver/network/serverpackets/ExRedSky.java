package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch d.
 * @author KenM
 */
public class ExRedSky extends L2GameServerPacket
{
	private final int duration;
	
	public ExRedSky(final int duration)
	{
		this.duration = duration;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x40);
		writeD(duration);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:40 ExRedSkyPacket";
	}
}
