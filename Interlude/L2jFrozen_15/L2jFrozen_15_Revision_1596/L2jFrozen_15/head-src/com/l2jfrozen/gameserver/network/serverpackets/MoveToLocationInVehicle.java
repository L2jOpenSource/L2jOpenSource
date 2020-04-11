package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.position.L2CharPosition;

/**
 * @author Maktakien
 */
public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int charObjId;
	private int boatId;
	private L2CharPosition destination;
	private L2CharPosition origin;
	
	/**
	 * @param actor
	 * @param destination
	 * @param origin
	 */
	public MoveToLocationInVehicle(final L2Character actor, final L2CharPosition destination, final L2CharPosition origin)
	{
		if (!(actor instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) actor;
		
		if (player.getBoat() == null)
		{
			return;
		}
		
		charObjId = player.getObjectId();
		boatId = player.getBoat().getObjectId();
		this.destination = destination;
		this.origin = origin;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x71);
		writeD(charObjId);
		writeD(boatId);
		writeD(destination.x);
		writeD(destination.y);
		writeD(destination.z);
		writeD(origin.x);
		writeD(origin.y);
		writeD(origin.z);
	}
	
	@Override
	public String getType()
	{
		return "[S] 71 MoveToLocationInVehicle";
	}
	
}
