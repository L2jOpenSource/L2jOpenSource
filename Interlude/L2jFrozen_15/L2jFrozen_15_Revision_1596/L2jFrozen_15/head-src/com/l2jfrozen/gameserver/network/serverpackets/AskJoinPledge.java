package com.l2jfrozen.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket
{
	private final int requestorObjId;
	private final String pledgeName;
	
	public AskJoinPledge(final int requestorObjId, final String pledgeName)
	{
		this.requestorObjId = requestorObjId;
		this.pledgeName = pledgeName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x32);
		writeD(requestorObjId);
		writeS(pledgeName);
	}
	
	@Override
	public String getType()
	{
		return "[S] 32 AskJoinPledge";
	}
}