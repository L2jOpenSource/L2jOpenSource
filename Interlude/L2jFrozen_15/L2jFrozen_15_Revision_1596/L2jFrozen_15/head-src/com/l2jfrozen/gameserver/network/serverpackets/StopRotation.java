package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class StopRotation extends L2GameServerPacket
{
	private final int charObjId;
	private final int degree;
	private final int speed;
	
	public StopRotation(final L2Character player, final int degree, final int speed)
	{
		charObjId = player.getObjectId();
		this.degree = degree;
		this.speed = speed;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x63);
		writeD(charObjId);
		writeD(degree);
		writeD(speed);
		writeC(0); // ?
	}
	
	@Override
	public String getType()
	{
		return "[S] 63 StopRotation";
	}
}
