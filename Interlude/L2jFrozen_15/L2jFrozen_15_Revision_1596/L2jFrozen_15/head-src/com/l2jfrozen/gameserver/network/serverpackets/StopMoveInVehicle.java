package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Maktakien
 */
public class StopMoveInVehicle extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int boatId;
	
	public StopMoveInVehicle(final L2PcInstance player, final int boatid)
	{
		activeChar = player;
		boatId = boatid;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x72);
		writeD(activeChar.getObjectId());
		writeD(boatId);
		writeD(activeChar.getInBoatPosition().getX());
		writeD(activeChar.getInBoatPosition().getY());
		writeD(activeChar.getInBoatPosition().getZ());
		writeD(activeChar.getPosition().getHeading());
	}
	
	@Override
	public String getType()
	{
		return "[S] 72 StopMoveInVehicle";
	}
}
