package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author chris_00 Asks the player to join a CC
 */
public class ExAskJoinMPCC extends L2GameServerPacket
{
	private final String requestorName;
	
	public ExAskJoinMPCC(final String requestorName)
	{
		this.requestorName = requestorName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x27);
		writeS(requestorName);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:27 ExAskJoinMPCC";
	}
}
