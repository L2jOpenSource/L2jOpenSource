package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author Maktakien
 */
public class OnVehicleCheckLocation extends L2GameServerPacket
{
	private final L2BoatInstance boat;
	private final int x;
	private final int y;
	private final int z;
	
	public OnVehicleCheckLocation(final L2BoatInstance instance, final int x, final int y, final int z)
	{
		boat = instance;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected void writeImpl()
	{
		
		writeC(0x5b);
		writeD(boat.getObjectId());
		writeD(x);
		writeD(y);
		writeD(z);
		writeD(boat.getPosition().getHeading());
	}
	
	@Override
	public String getType()
	{
		return null;
	}
	
}
