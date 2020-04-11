package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.StopMoveInVehicle;

public final class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private int _boatId;
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (player.isInBoat() && player.getBoat().getObjectId() == _boatId)
		{
			player.getBoatPosition().set(_x, _y, _z, _heading);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.broadcastPacket(new StopMoveInVehicle(player, _boatId));
		}
	}
}