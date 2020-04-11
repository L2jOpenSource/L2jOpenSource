package l2r.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import l2r.gameserver.model.ItemInfo;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public abstract class AbstractInventoryUpdate extends AbstractItemPacket
{
	private final Map<Integer, ItemInfo> _items = new ConcurrentSkipListMap<>();
	
	public AbstractInventoryUpdate()
	{
	}
	
	public AbstractInventoryUpdate(L2ItemInstance item)
	{
		addItem(item);
	}
	
	public AbstractInventoryUpdate(List<ItemInfo> items)
	{
		for (ItemInfo item : items)
		{
			_items.put(item.getObjectId(), item);
		}
	}
	
	public final void addItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item));
	}
	
	public final void addNewItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 1));
	}
	
	public final void addModifiedItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 2));
	}
	
	public final void addRemovedItem(L2ItemInstance item)
	{
		_items.put(item.getObjectId(), new ItemInfo(item, 3));
	}
	
	public final void addItems(List<L2ItemInstance> items)
	{
		for (L2ItemInstance item : items)
		{
			_items.put(item.getObjectId(), new ItemInfo(item));
		}
	}
	
	public final Collection<ItemInfo> getItems()
	{
		return _items.values();
	}
	
	protected final void writeItems()
	{
		writeH(_items.size());
		
		for (ItemInfo item : _items.values())
		{
			writeH(item.getChange()); // Update type : 01-add, 02-modify, 03-remove
			writeItem(item);
		}
	}
}
