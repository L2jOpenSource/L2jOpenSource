package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author Maktakien
 */
public class VehicleDeparture extends L2GameServerPacket
{
	private final L2BoatInstance boat;
	private final int speed;
	private final int speedRotation;// rotation
	private final int x;
	private final int y;
	private final int z;
	
	public VehicleDeparture(final L2BoatInstance boat, final int speed1, final int speed2, final int x, final int y, final int z)
	{
		this.boat = boat;
		speed = speed1;
		speedRotation = speed2;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5a);
		writeD(boat.getObjectId());
		writeD(speed);
		writeD(speedRotation);
		writeD(x);
		writeD(y);
		writeD(z);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] 5A VehicleDeparture";
	}
}
