package l2r.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExQuestItemList extends AbstractItemPacket
{
	private final L2PcInstance _activeChar;
	private final List<L2ItemInstance> _items = new ArrayList<>();
	
	public ExQuestItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		
		final L2ItemInstance[] items = activeChar.getInventory().getItems();
		for (int i = 0; i < items.length; i++)
		{
			if ((items[i] != null) && items[i].isQuestItem())
			{
				_items.add(items[i]); // add to questinv
				items[i] = null; // remove from list
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC6);
		
		writeH(_items.size());
		for (L2ItemInstance item : _items)
		{
			writeItem(item);
		}
		writeInventoryBlock(_activeChar.getInventory());
	}
}
