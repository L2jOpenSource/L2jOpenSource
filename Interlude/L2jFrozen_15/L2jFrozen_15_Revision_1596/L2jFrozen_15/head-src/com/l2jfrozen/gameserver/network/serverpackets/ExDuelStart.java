package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch d.
 * @author KenM
 */
public class ExDuelStart extends L2GameServerPacket
{
	private final int unk1;
	
	/**
	 * Instantiates a new ex duel start.
	 * @param unk1 the unk1
	 */
	public ExDuelStart(final int unk1)
	{
		this.unk1 = unk1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4d);
		
		writeD(unk1);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:4D ExDuelStart";
	}
}
