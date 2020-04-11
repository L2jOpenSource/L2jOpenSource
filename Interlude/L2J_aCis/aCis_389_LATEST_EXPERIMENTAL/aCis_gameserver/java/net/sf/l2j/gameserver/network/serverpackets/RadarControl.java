package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.location.Location;

public class RadarControl extends L2GameServerPacket
{
	private final int _showRadar;
	private final int _type;
	private final int _x, _y, _z;
	
	public RadarControl(int showRadar, int type, int x, int y, int z)
	{
		_showRadar = showRadar; // 0 = showradar; 1 = delete radar;
		_type = type;
		_x = x;
		_y = y;
		_z = z;
	}
	
	public RadarControl(int showRadar, int type, Location loc)
	{
		_showRadar = showRadar; // 0 = showradar; 1 = delete radar;
		_type = type;
		_x = loc.getX();
		_y = loc.getY();
		_z = loc.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xEB);
		writeD(_showRadar);
		writeD(_type);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}