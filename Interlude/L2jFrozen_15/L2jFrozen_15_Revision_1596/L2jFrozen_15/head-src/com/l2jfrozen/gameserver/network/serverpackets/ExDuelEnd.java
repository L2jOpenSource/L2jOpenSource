package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch d.
 * @author KenM
 */
public class ExDuelEnd extends L2GameServerPacket
{
	private final int unk1;
	
	public ExDuelEnd(final int unk1)
	{
		this.unk1 = unk1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4e);
		
		writeD(unk1);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:4E ExDuelEnd";
	}
}
