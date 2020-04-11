package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.TradeItem;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;

public class PrivateStoreListSell extends AbstractItemPacket
{
	private final int _objId;
	private final long _playerAdena;
	private final boolean _packageSale;
	private final TradeItem[] _items;
	
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_objId = storePlayer.getObjectId();
		_playerAdena = CustomServerConfigs.ALTERNATE_PAYMODE_SHOPS ? player.getFAdena() : player.getAdena();
		_items = storePlayer.getSellList().getItems();
		_packageSale = storePlayer.getSellList().isPackaged();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xA1);
		
		writeD(_objId);
		writeD(_packageSale ? 1 : 0);
		writeQ(_playerAdena);
		
		writeD(_items.length);
		for (TradeItem item : _items)
		{
			writeItem(item);
			
			writeQ(item.getPrice());
			writeQ(item.getItem().getReferencePrice() * 2);
		}
	}
}
