package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Wooden-
 */
public final class SnoopQuit extends L2GameClientPacket
{
	private int snoopID;
	
	@Override
	protected void readImpl()
	{
		snoopID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = (L2PcInstance) L2World.getInstance().findObject(snoopID);
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		player.removeSnooper(activeChar);
		activeChar.removeSnooped(player);
	}
	
	@Override
	public String getType()
	{
		return "[C] AB SnoopQuit";
	}
}