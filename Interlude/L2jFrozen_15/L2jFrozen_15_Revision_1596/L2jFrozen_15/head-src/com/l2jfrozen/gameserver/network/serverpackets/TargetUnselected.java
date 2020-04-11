package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * format dddd sample 0000: 3a 69 08 10 48 02 c1 00 00 f7 56 00 00 89 ea ff :i..H.....V..... 0010: ff 0c b2 d8 61 ....a
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class TargetUnselected extends L2GameServerPacket
{
	private final int targetObjId;
	private final int x;
	private final int y;
	private final int z;
	
	/**
	 * @param character
	 */
	public TargetUnselected(final L2Character character)
	{
		targetObjId = character.getObjectId();
		x = character.getX();
		y = character.getY();
		z = character.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2a);
		writeD(targetObjId);
		writeD(x);
		writeD(y);
		writeD(z);
		// writeD(_target.getTargetId()); //?? probably not used in client
	}
	
	@Override
	public String getType()
	{
		return "[S] 2A TargetUnselected";
	}
}
