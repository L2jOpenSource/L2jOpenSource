package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class GMViewItemList extends AbstractItemPacket
{
	private final L2ItemInstance[] _items;
	private final int _limit;
	private final String _playerName;
	
	public GMViewItemList(L2PcInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	public GMViewItemList(L2PetInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_limit = cha.getInventoryLimit();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9A);
		
		writeS(_playerName);
		writeD(_limit); // inventory limit
		writeH(0x01); // show window ??
		writeH(_items.length);
		
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
	}
}
