package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

public class BeginRotation extends L2GameServerPacket
{
	private final int charObjId;
	private final int degree;
	private final int side;
	private final int speed;
	
	public BeginRotation(final L2Character player, final int degree, final int side, final int speed)
	{
		charObjId = player.getObjectId();
		this.degree = degree;
		this.side = side;
		this.speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x62);
		writeD(charObjId);
		writeD(degree);
		writeD(side);
		if (speed != 0)
		{
			writeD(speed);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 62 BeginRotation";
	}
}
