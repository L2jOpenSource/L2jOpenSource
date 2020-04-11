package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: ch dddd.
 * @author KenM
 */
public class ExUseSharedGroupItem extends L2GameServerPacket
{
	private final int unk1, unk2, unk3, unk4;
	
	public ExUseSharedGroupItem(final int unk1, final int unk2, final int unk3, final int unk4)
	{
		this.unk1 = unk1;
		this.unk2 = unk2;
		this.unk3 = unk3;
		this.unk4 = unk4;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x49);
		
		writeD(unk1);
		writeD(unk2);
		writeD(unk3);
		writeD(unk4);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:49 ExUseSharedGroupItem";
	}
}
