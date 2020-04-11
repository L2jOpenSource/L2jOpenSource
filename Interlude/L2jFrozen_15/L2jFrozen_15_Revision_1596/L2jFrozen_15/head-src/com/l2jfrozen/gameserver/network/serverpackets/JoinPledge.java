package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class JoinPledge extends L2GameServerPacket
{
	private final int pledgeId;
	
	public JoinPledge(final int pledgeId)
	{
		this.pledgeId = pledgeId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x33);
		
		writeD(pledgeId);
	}
	
	@Override
	public String getType()
	{
		return "[S] 33 JoinPledge";
	}
	
}
