package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExReplyPostItemList extends AbstractItemPacket
{
	L2PcInstance _activeChar;
	private final L2ItemInstance[] _itemList;
	
	public ExReplyPostItemList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_itemList = _activeChar.getInventory().getAvailableItems(true, false, false);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB2);
		
		writeD(_itemList.length);
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
		}
	}
}
