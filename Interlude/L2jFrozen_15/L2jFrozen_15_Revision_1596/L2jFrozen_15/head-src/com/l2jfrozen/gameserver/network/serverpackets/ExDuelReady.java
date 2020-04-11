package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch d.
 * @author KenM
 */
public class ExDuelReady extends L2GameServerPacket
{
	private final int unk1;
	
	public ExDuelReady(final int unk1)
	{
		this.unk1 = unk1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4c);
		writeD(unk1);
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:4C ExDuelReady";
	}
	
}
