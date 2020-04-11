package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * sample 0000: 3f 2a 89 00 4c 01 00 00 00 0a 15 00 00 66 fe 00 ?*..L........f.. 0010: 00 7c f1 ff ff .|... format dd ddd
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class ChangeWaitType extends L2GameServerPacket
{
	private final int charObjId;
	private final int moveType;
	private final int x, y, z;
	
	public static final int WT_SITTING = 0;
	public static final int WT_STANDING = 1;
	public static final int WT_START_FAKEDEATH = 2;
	public static final int WT_STOP_FAKEDEATH = 3;
	
	public ChangeWaitType(final L2Character character, final int newMoveType)
	{
		charObjId = character.getObjectId();
		moveType = newMoveType;
		
		x = character.getX();
		y = character.getY();
		z = character.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2f);
		writeD(charObjId);
		writeD(moveType);
		writeD(x);
		writeD(y);
		writeD(z);
	}
	
	@Override
	public String getType()
	{
		return "[S] 2F ChangeWaitType";
	}
}
