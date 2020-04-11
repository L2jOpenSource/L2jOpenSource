package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch Sd.
 * @author KenM
 */
public class ExDuelAskStart extends L2GameServerPacket
{
	private final String requestorName;
	private final int partyDuel;
	
	/**
	 * Instantiates a new ex duel ask start.
	 * @param requestor the requestor
	 * @param partyDuel the party duel
	 */
	public ExDuelAskStart(final String requestor, final int partyDuel)
	{
		requestorName = requestor;
		this.partyDuel = partyDuel;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4b);
		
		writeS(requestorName);
		writeD(partyDuel);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:4B ExDuelAskStart";
	}
}
