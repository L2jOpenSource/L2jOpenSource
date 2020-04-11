package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;

public class PrivateStoreListBuy extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final List<TradeItem> _items;
	
	public PrivateStoreListBuy(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		storePlayer.getSellList().updateItems(); // Update SellList for case inventory content has changed
		_items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xBE);
		
		writeD(_objId);
		writeQ(_playerAdena);
		
		writeD(_items.size());
		
		int slotNumber = 0;
		for (TradeItem item : _items)
		{
			slotNumber++;
			writeItem(item);
			writeD(slotNumber); // Slot in shop
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
			writeQ(item.getStoreCount());
		}
	}
}
