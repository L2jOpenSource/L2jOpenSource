package com.l2jfrozen.gameserver.network.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket
{
	private final String pledgeName;
	private final String playerName;
	
	public SurrenderPledgeWar(final String pledge, final String charName)
	{
		pledgeName = pledge;
		playerName = charName;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x69);
		writeS(pledgeName);
		writeS(playerName);
	}
	
	@Override
	public String getType()
	{
		return "[S] 69 SurrenderPledgeWar";
	}
}
