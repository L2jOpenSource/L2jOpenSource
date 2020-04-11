package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;

public final class CannotMoveAnymore extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(CannotMoveAnymore.class);
	private int x, y, z, heading;
	
	@Override
	protected void readImpl()
	{
		x = readD();
		y = readD();
		z = readD();
		heading = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2Character player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("DEBUG " + getType() + ": client: x:" + x + " y:" + y + " z:" + z + " server x:" + player.getX() + " y:" + player.getY() + " z:" + player.getZ());
		}
		
		if (player.getAI() != null)
		{
			player.getAI().notifyEvent(CtrlEvent.EVT_ARRIVED_BLOCKED, new L2CharPosition(x, y, z, heading));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 36 CannotMoveAnymore";
	}
}
