package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;

public class PrivateStoreManageListSell extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _itemList;
	private final TradeItem[] _sellList;
	
	public PrivateStoreManageListSell(L2PcInstance player, boolean isPackageSale)
	{
		_objId = player.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		player.getSellList().updateItems();
		_packageSale = isPackageSale;
		_itemList = player.getInventory().getAvailableItems(player.getSellList());
		_sellList = player.getSellList().getItems();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xA0);
		
		writeD(_objId);
		writeD(_packageSale ? 1 : 0); // Package sell
		writeQ(_playerAdena);
		
		writeD(_itemList.length); // for potential sells
		for (TradeItem item : _itemList)
		{
			writeItem(item);
			writeQ(item.getItem().getReferencePrice() * 2);
		}
		
		writeD(_sellList.length); // count for any items already added for sell
		for (TradeItem item : _sellList)
		{
			writeItem(item);
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
		}
	}
}
