package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Maktakien
 */
public class GetOnVehicle extends L2GameServerPacket
{
	private final int x;
	private final int y;
	private final int z;
	private final L2PcInstance activeChar;
	private final L2BoatInstance boat;
	
	/**
	 * @param activeChar
	 * @param boat
	 * @param x
	 * @param y
	 * @param z
	 */
	public GetOnVehicle(final L2PcInstance activeChar, final L2BoatInstance boat, final int x, final int y, final int z)
	{
		this.activeChar = activeChar;
		this.boat = boat;
		this.x = x;
		this.y = y;
		this.z = z;
		this.activeChar.setBoat(this.boat);
		this.activeChar.setInBoat(true);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x5c);
		writeD(activeChar.getObjectId());
		writeD(boat.getObjectId());
		writeD(x);
		writeD(y);
		writeD(z);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] 5C GetOnVehicle";
	}
	
}
