package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public final class ItemList extends AbstractItemPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2ItemInstance> _items = new ArrayList<>();
	private final boolean _showWindow;
	
	public ItemList(L2PcInstance cha, boolean showWindow)
	{
		_activeChar = cha.getActingPlayer();
		final L2ItemInstance[] items = cha.getInventory().getItems();
		_showWindow = showWindow;
		
		for (int i = 0; i < items.length; i++)
		{
			if ((items[i] != null) && !items[i].isQuestItem())
			{
				_items.add(items[i]); // add to questinv
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x11);
		
		writeH(_showWindow ? 0x01 : 0x00);
		writeH(_items.size());
		
		for (L2ItemInstance temp : _items)
		{
			writeItem(temp);
		}
		writeInventoryBlock(_activeChar.getInventory());
	}
}
