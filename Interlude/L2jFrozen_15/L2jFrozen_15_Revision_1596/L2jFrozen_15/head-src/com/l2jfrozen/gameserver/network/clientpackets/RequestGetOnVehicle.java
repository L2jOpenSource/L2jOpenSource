package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.BoatManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jfrozen.util.Point3D;

public final class RequestGetOnVehicle extends L2GameClientPacket
{
	private int id, x, y, z;
	
	@Override
	protected void readImpl()
	{
		id = readD();
		x = readD();
		y = readD();
		z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2BoatInstance boat = BoatManager.getInstance().GetBoat(id);
		if (boat == null)
		{
			return;
		}
		
		final GetOnVehicle Gon = new GetOnVehicle(activeChar, boat, x, y, z);
		activeChar.setInBoatPosition(new Point3D(x, y, z));
		activeChar.getPosition().setXYZ(boat.getPosition().getX(), boat.getPosition().getY(), boat.getPosition().getZ());
		activeChar.broadcastPacket(Gon);
		activeChar.revalidateZone(true);
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 5C GetOnVehicle";
	}
}
