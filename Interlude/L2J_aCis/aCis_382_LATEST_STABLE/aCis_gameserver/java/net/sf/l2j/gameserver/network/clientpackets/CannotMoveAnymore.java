package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

public final class CannotMoveAnymore extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	
	@Override
	protected void readImpl()
	{
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Creature player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (player.hasAI())
			player.getAI().notifyEvent(AiEventType.ARRIVED_BLOCKED, new SpawnLocation(_x, _y, _z, _heading));
	}
}