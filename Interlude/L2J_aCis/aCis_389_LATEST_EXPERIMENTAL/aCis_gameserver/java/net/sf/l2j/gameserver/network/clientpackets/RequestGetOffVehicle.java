package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.Point2D;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.GetOffVehicle;
import net.sf.l2j.gameserver.network.serverpackets.MoveToLocation;
import net.sf.l2j.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestGetOffVehicle extends L2GameClientPacket
{
	private int _boatId;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		_boatId = readD();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (!player.isInBoat() || player.getBoat().getObjectId() != _boatId)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.broadcastPacket(new StopMoveInVehicle(player, _boatId));
		player.setBoat(null);
		player.broadcastPacket(new GetOffVehicle(player.getObjectId(), _boatId, _x, _y, _z));
		
		// Proper heading has been set when we clicked outside of the ship, just move player forward.
		final Point2D outsidePoint = MathUtil.getNewLocationByDistanceAndHeading(_x, _y, player.getPosition().getHeading(), 60);
		
		player.setXYZ(outsidePoint.getX(), outsidePoint.getY(), _z);
		player.revalidateZone(true);
		
		player.broadcastPacket(new MoveToLocation(player, new Location(outsidePoint.getX(), outsidePoint.getY(), _z)));
	}
}