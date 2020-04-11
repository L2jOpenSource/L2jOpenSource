package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:40 $
 */
public class Dice extends L2GameServerPacket
{
	private final int charObjId;
	private final int itemId;
	private final int number;
	private final int x;
	private final int y;
	private final int z;
	
	/**
	 * 0xd4 Dice dddddd
	 * @param charObjId
	 * @param itemId
	 * @param number
	 * @param x
	 * @param y
	 * @param z
	 */
	public Dice(final int charObjId, final int itemId, final int number, final int x, final int y, final int z)
	{
		this.charObjId = charObjId;
		this.itemId = itemId;
		this.number = number;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xD4);
		writeD(charObjId); // object id of player
		writeD(itemId); // item id of dice (spade) 4625,4626,4627,4628
		writeD(number); // number rolled
		writeD(x); // x
		writeD(y); // y
		writeD(z); // z
	}
	
	@Override
	public String getType()
	{
		return "[S] D4 Dice";
	}
}
