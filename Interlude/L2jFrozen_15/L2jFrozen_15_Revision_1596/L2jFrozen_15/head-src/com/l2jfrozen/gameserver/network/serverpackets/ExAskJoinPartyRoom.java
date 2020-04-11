package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch S
 * @author KenM
 */
public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private final String charName;
	
	public ExAskJoinPartyRoom(final String charName)
	{
		this.charName = charName;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x34);
		writeS(charName);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:34 ExAskJoinPartyRoom";
	}
}