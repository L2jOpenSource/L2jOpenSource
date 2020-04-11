package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author devScarlet & mrTJO
 */
public class ShowXMasSeal extends L2GameServerPacket
{
	private final int item;
	
	public ShowXMasSeal(final int item)
	{
		this.item = item;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xF2);
		
		writeD(item);
	}
	
	/**
	 * @see com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] F2 ShowXMasSeal";
	}
	
}
