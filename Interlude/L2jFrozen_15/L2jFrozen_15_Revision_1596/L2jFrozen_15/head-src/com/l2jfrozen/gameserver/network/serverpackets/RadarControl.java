package com.l2jfrozen.gameserver.network.serverpackets;

public class RadarControl extends L2GameServerPacket
{
	private final int showRadar;
	private final int type;
	private final int x;
	private final int y;
	private final int z;
	
	// 0xEB RadarControl ddddd
	public RadarControl(final int showRadar, final int type, final int x, final int y, final int z)
	{
		this.showRadar = showRadar; // showRader?? 0 = showradar; 1 = delete radar;
		this.type = type; // radar type??
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEB);
		writeD(showRadar);
		writeD(type); // maybe type
		writeD(x); // x
		writeD(y); // y
		writeD(z); // z
	}
	
	@Override
	public String getType()
	{
		return "[S] EB RadarControl";
	}
}
