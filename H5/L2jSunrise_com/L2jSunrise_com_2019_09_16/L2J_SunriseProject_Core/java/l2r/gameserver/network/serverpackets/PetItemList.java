package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.items.instance.L2ItemInstance;

public class PetItemList extends AbstractItemPacket
{
	private final L2ItemInstance[] _items;
	
	public PetItemList(L2ItemInstance[] items)
	{
		_items = items;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xB3);
		
		writeH(_items.length);
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
	}
}
