package com.l2jfrozen.gameserver.network.serverpackets;

public class StopPledgeWar extends L2GameServerPacket
{
	private final String pledgeName;
	private final String playerName;
	
	public StopPledgeWar(final String pledge, final String charName)
	{
		pledgeName = pledge;
		playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x67);
		writeS(pledgeName);
		writeS(playerName);
	}
	
	@Override
	public String getType()
	{
		return "[S] 67 StopPledgeWar";
	}
}
