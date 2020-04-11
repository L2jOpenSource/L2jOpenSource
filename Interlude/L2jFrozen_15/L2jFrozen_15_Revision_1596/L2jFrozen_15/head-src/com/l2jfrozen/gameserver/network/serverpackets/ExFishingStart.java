package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * Format (ch)ddddd
 * @author -Wooden-
 */
public class ExFishingStart extends L2GameServerPacket
{
	private final L2Character activeChar;
	private final int x, y, z, fishType;
	@SuppressWarnings("unused")
	private final boolean isNightLure;
	
	public ExFishingStart(final L2Character character, final int fishType, final int x, final int y, final int z, final boolean isNightLure)
	{
		activeChar = character;
		this.fishType = fishType;
		this.x = x;
		this.y = y;
		this.z = z;
		this.isNightLure = isNightLure;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x13);
		writeD(activeChar.getObjectId());
		writeD(fishType); // fish type
		writeD(x); // x poisson
		writeD(y); // y poisson
		writeD(z); // z poisson
		writeC(0x00); // night lure
		writeC(0x00); // ??
		writeC(fishType >= 7 && fishType <= 9 ? 0x01 : 0x00); // 0 = day lure 1 = night lure
		writeC(0x00);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:13 ExFishingStart";
	}
	
}
