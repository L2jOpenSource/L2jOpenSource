package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

public class ValidateLocation extends L2GameServerPacket
{
	private final int charObjId;
	private final int x, y, z, heading;
	
	public ValidateLocation(final L2Character cha)
	{
		charObjId = cha.getObjectId();
		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();
		heading = cha.getHeading();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x61);
		
		writeD(charObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(heading);
	}
	
	@Override
	public String getType()
	{
		return "[S] 61 ValidateLocation";
	}
}