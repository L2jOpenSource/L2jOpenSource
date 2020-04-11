package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * @author programmos
 */
@SuppressWarnings("unused")
public class MoveWithDelta extends L2GameClientPacket
{
	private int dx, dy, dz;
	
	@Override
	protected void readImpl()
	{
		dx = readD();
		dy = readD();
		dz = readD();
	}
	
	@Override
	protected void runImpl()
	{
	}
	
	@Override
	public String getType()
	{
		return "[C] 0x41 MoveWithDelta";
	}
}