package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ShowMiniMap;

/**
 * sample format d
 * @version $Revision: 1 $ $Date: 2005/04/10 00:17:44 $
 */
public final class RequestShowMiniMap extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected final void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.sendPacket(new ShowMiniMap(1665));
	}
	
	@Override
	public String getType()
	{
		return "[C] cd RequestShowMiniMap";
	}
}
