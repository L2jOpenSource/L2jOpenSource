package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;

public class PrivateStoreManageListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final L2ItemInstance[] _itemList;
	private final TradeItem[] _buyList;
	
	public PrivateStoreManageListBuy(L2PcInstance player)
	{
		_objId = player.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		_itemList = player.getInventory().getUniqueItems(false, true);
		_buyList = player.getBuyList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xBD);
		
		writeD(_objId);
		writeQ(_playerAdena);
		
		writeD(_itemList.length); // for potential sells
		for (L2ItemInstance item : _itemList)
		{
			writeItem(item);
			writeQ(item.getItem().getReferencePrice() * 2);
		}
		
		writeD(_buyList.length); // count for any items already added for sell
		for (TradeItem item : _buyList)
		{
			writeItem(item);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
			writeQ(item.getCount());
		}
	}
}
