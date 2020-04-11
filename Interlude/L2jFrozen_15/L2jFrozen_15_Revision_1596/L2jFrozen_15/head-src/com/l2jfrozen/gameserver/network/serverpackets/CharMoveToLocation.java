package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * 0000: 01 7a 73 10 4c b2 0b 00 00 a3 fc 00 00 e8 f1 ff .zs.L........... 0010: ff bd 0b 00 00 b3 fc 00 00 e8 f1 ff ff ............. ddddddd
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class CharMoveToLocation extends L2GameServerPacket
{
	private final int charbjId, x, y, z, xDst, yDst, zDst;
	
	public CharMoveToLocation(final L2Character cha)
	{
		charbjId = cha.getObjectId();
		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();
		xDst = cha.getXdestination();
		yDst = cha.getYdestination();
		zDst = cha.getZdestination();
	}
	
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		// reset old Moving task
		if (activeChar != null && activeChar.isMovingTaskDefined())
		{
			activeChar.setMovingTaskDefined(false);
		}
		
		writeC(0x01);
		
		writeD(charbjId);
		
		writeD(xDst);
		writeD(yDst);
		writeD(zDst);
		
		writeD(x);
		writeD(y);
		writeD(z);
	}
	
	@Override
	public String getType()
	{
		return "[S] 01 CharMoveToLocation";
	}
	
}
