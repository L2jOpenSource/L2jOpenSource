package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * sample format d
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ChairSit extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int staticObjectId;
	
	/**
	 * @param player
	 * @param staticObjectId
	 */
	public ChairSit(final L2PcInstance player, final int staticObjectId)
	{
		activeChar = player;
		this.staticObjectId = staticObjectId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe1);
		writeD(activeChar.getObjectId());
		writeD(staticObjectId);
	}
	
	@Override
	public String getType()
	{
		return "[S] e1 ChairSit";
	}
}
