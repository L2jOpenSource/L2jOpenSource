package net.sf.l2j.gameserver.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private final int _itemId;
	
	public ChooseInventoryItem(int itemId)
	{
		_itemId = itemId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x6f);
		writeD(_itemId);
	}
}