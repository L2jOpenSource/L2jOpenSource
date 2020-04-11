package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.BoatManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.GetOffVehicle;

/**
 * @author Maktakien
 */
public final class RequestGetOffVehicle extends L2GameClientPacket
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
		final GetOffVehicle Gon = new GetOffVehicle(activeChar, boat, x, y, z);
		activeChar.broadcastPacket(Gon);
	}
	
	@Override
	public String getType()
	{
		return "[S] 5d GetOffVehicle";
	}
	
}
