package com.l2jfrozen.gameserver.network.serverpackets;

public class Ride extends L2GameServerPacket
{
	public static final int ACTION_MOUNT = 1;
	public static final int ACTION_DISMOUNT = 0;
	private final int id;
	private final int bRide;
	private int rideType;
	private final int rideClassID;
	
	public Ride(final int id, final int action, final int rideClassId)
	{
		this.id = id; // charobjectID
		bRide = action; // 1 for mount ; 2 for dismount
		rideClassID = rideClassId + 1000000; // npcID
		
		if (rideClassId == 12526 || // wind strider
			rideClassId == 12527 || // star strider
			rideClassId == 12528) // twilight strider
		{
			rideType = 1; // 1 for Strider ; 2 for wyvern
		}
		else if (rideClassId == 12621) // wyvern
		{
			rideType = 2; // 1 for Strider ; 2 for wyvern
		}
	}
	
	@Override
	public void runImpl()
	{
		
	}
	
	public int getMountType()
	{
		return rideType;
	}
	
	@Override
	protected final void writeImpl()
	{
		
		writeC(0x86);
		writeD(id);
		writeD(bRide);
		writeD(rideType);
		writeD(rideClassID);
	}
	
	@Override
	public String getType()
	{
		return "[S] 86 Ride";
	}
}
