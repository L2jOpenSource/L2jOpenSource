package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample
 * <p>
 * 4b c1 b2 e0 4a 00 00 00 00
 * <p>
 * format cdd
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AskJoinParty extends L2GameServerPacket
{
	private final String requestorName;
	private final int itemDistribution;
	
	/**
	 * @param requestorName
	 * @param itemDistribution
	 */
	public AskJoinParty(final String requestorName, final int itemDistribution)
	{
		this.requestorName = requestorName;
		this.itemDistribution = itemDistribution;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x39);
		writeS(requestorName);
		writeD(itemDistribution);
	}
	
	@Override
	public String getType()
	{
		return "[S] 39 AskJoinParty 0x4b";
	}
	
}
