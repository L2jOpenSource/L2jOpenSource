package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.StopMoveInVehicle;
import com.l2jfrozen.util.Point3D;

/**
 * @author Maktakien
 */
public final class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private int x;
	private int y;
	private int z;
	private int heading;
	private int boatId;
	
	@Override
	protected void readImpl()
	{
		boatId = readD();
		x = readD();
		y = readD();
		z = readD();
		heading = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (player.isInBoat())
		{
			if (player.getBoat().getObjectId() == boatId)
			{
				player.setInBoatPosition(new Point3D(x, y, z));
				player.getPosition().setHeading(heading);
				player.broadcastPacket(new StopMoveInVehicle(player, boatId));
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 5D CannotMoveAnymoreInVehicle";
	}
}