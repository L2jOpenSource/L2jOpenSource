package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ExAutoSoulShot extends L2GameServerPacket
{
	private final int itemId;
	private final int type;
	
	/**
	 * 0xfe:0x12 ExAutoSoulShot (ch)dd
	 * @param itemId
	 * @param type
	 */
	public ExAutoSoulShot(final int itemId, final int type)
	{
		this.itemId = itemId;
		this.type = type;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x12); // sub id
		writeD(itemId);
		writeD(type);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:12 ExAutoSoulShot";
	}
}
