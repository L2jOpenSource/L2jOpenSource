package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2BoatInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Maktakien
 */
public class GetOffVehicle extends L2GameServerPacket
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
	public GetOffVehicle(final L2PcInstance activeChar, final L2BoatInstance boat, final int x, final int y, final int z)
	{
		this.activeChar = activeChar;
		this.boat = boat;
		this.x = x;
		this.y = y;
		this.z = z;
		
		if (this.activeChar != null)
		{
			this.activeChar.setInBoat(false);
			this.activeChar.setBoat(null);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		if (boat == null || activeChar == null)
		{
			return;
		}
		
		writeC(0x5d);
		writeD(activeChar.getObjectId());
		writeD(boat.getObjectId());
		writeD(x);
		writeD(y);
		writeD(z);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] 5d GetOffVehicle";
	}
	
}
