package com.l2jfrozen.gameserver.network.serverpackets;

public class StartPledgeWar extends L2GameServerPacket
{
	private final String pledgeName;
	private final String playerName;
	
	public StartPledgeWar(final String pledge, final String charName)
	{
		pledgeName = pledge;
		playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x65);
		writeS(playerName);
		writeS(pledgeName);
	}
	
	@Override
	public String getType()
	{
		return "[S] 65 StartPledgeWar";
	}
}
