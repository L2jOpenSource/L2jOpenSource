package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * sample 0000: 3e 2a 89 00 4c 01 00 00 00 .|... format dd
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class ChangeMoveType extends L2GameServerPacket
{
	public static final int WALK = 0;
	public static final int RUN = 1;
	
	private final int charObjId;
	private final boolean running;
	
	public ChangeMoveType(final L2Character character)
	{
		charObjId = character.getObjectId();
		running = character.isRunning();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x2e);
		writeD(charObjId);
		writeD(running ? RUN : WALK);
		writeD(0); // c2
	}
	
	@Override
	public String getType()
	{
		return "[S] 3E ChangeMoveType";
	}
}
