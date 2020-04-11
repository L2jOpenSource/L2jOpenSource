package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan.SubPledge;

/**
 * @author -Wooden-
 */
public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private final SubPledge subPledge;
	
	/**
	 * @param subPledge
	 */
	public PledgeReceiveSubPledgeCreated(final SubPledge subPledge)
	{
		this.subPledge = subPledge;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3f);
		
		writeD(0x01);
		writeD(subPledge.getId());
		writeS(subPledge.getName());
		writeS(subPledge.getLeaderName());
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:3F PledgeReceiveSubPledgeCreated";
	}
}
