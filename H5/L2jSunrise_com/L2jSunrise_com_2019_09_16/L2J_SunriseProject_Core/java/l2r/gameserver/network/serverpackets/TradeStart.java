package l2r.gameserver.network.serverpackets;

import l2r.Config;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public final class TradeStart extends AbstractItemPacket
{
	private final L2PcInstance _activeChar;
	private final L2ItemInstance[] _itemList;
	
	public TradeStart(L2PcInstance player)
	{
		_activeChar = player;
		_itemList = _activeChar.getInventory().getAvailableItems(true, (_activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && Config.GM_TRADE_RESTRICTED_ITEMS), false);
	}
	
	@Override
	protected final void writeImpl()
	{
		if ((_activeChar.getActiveTradeList() == null) || (_activeChar.getActiveTradeList().getPartner() == null))
		{
			return;
		}
		
		writeC(0x14);
		
		writeD(_activeChar.getActiveTradeList().getPartner().getObjectId());
		
		writeH(_itemList.length);
		
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
		}
	}
}
