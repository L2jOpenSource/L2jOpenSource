package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreMsgSell extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private String storeMsg;
	
	public PrivateStoreMsgSell(final L2PcInstance player)
	{
		activeChar = player;
		if (activeChar.getSellList() != null)
		{
			storeMsg = activeChar.getSellList().getTitle();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9c);
		writeD(activeChar.getObjectId());
		writeS(storeMsg);
	}
	
	@Override
	public String getType()
	{
		return "[S] 9c PrivateStoreMsgSell";
	}
}