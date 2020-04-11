package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch) ddd b d: ? d: crest ID d: crest size b: raw data
 * @author -Wooden-
 */
public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private final int crestId;
	private final byte[] data;
	
	public ExPledgeCrestLarge(final int crestId, final byte[] data)
	{
		this.crestId = crestId;
		this.data = data;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x28);
		
		writeD(0x00); // ???
		writeD(crestId);
		writeD(data.length);
		
		writeB(data);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:28 ExPledgeCrestLarge";
	}
	
}
