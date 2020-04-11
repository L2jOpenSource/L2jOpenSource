package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RecipeShopMsg extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	
	public RecipeShopMsg(final L2PcInstance player)
	{
		activeChar = player;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xdb);
		writeD(activeChar.getObjectId());
		writeS(activeChar.getCreateList().getStoreName()); // activeChar.getTradeList().getSellStoreName());
	}
	
	@Override
	public String getType()
	{
		return "[S] db RecipeShopMsg";
	}
}
