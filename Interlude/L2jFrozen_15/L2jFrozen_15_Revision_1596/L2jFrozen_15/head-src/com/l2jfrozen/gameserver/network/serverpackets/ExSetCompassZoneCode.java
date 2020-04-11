package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch d.
 * @author KenM
 */
public class ExSetCompassZoneCode extends L2GameServerPacket
{
	public static final int SIEGEWARZONE1 = 0x0A;
	public static final int SIEGEWARZONE2 = 0x0B;
	public static final int PEACEZONE = 0x0C;
	public static final int SEVENSIGNSZONE = 0x0D;
	public static final int PVPZONE = 0x0E;
	public static final int GENERALZONE = 0x0F;
	private final int zoneType;
	
	public ExSetCompassZoneCode(final int val)
	{
		zoneType = val;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x32);
		writeD(zoneType);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:32 ExSetCompassZoneCode";
	}
}
