package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0000: 76 7a 07 80 49 ea 01 00 00 c1 37 fe uz..Ic'.J.....7.
 * <p>
 * 0010: ff 9e c3 03 00 8f f3 ff ff .........
 * <p>
 * <p>
 * format dddddd (player id, target id, distance, startx, starty, startz)
 * <p>
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MoveOnVehicle extends L2GameServerPacket
{
	private final int id;
	private final int x, y, z;
	private final L2PcInstance activeChar;
	
	public MoveOnVehicle(final int vehicleID, final L2PcInstance player, final int x, final int y, final int z)
	{
		id = vehicleID;
		activeChar = player;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x71);
		
		writeD(activeChar.getObjectId());
		writeD(id);
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(activeChar.getX());
		writeD(activeChar.getY());
		writeD(activeChar.getZ());
	}
	
	@Override
	public String getType()
	{
		return "[S] 71 MoveOnVehicle";
	}
}
