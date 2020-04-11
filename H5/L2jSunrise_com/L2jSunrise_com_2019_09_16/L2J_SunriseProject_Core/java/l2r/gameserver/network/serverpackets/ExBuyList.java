package l2r.gameserver.network.serverpackets;

import java.util.Collection;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.buylist.L2BuyList;
import l2r.gameserver.model.buylist.Product;

/**
 * @author vGodFather
 */
public final class ExBuyList extends AbstractItemPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private double _taxRate = 0;
	private boolean _loadAll = true;
	@SuppressWarnings("unused")
	private final int _inventorySlots;
	
	public ExBuyList(L2PcInstance player)
	{
		_listId = -1;
		_list = null;
		_money = player.getAdena();
		_taxRate = 0;
		_loadAll = false;
		_inventorySlots = player.getInventory().getItemsWithoutQuest().size();
	}
	
	public ExBuyList(L2BuyList list, L2PcInstance player, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = player.getAdena();
		_taxRate = taxRate;
		_loadAll = true;
		_inventorySlots = player.getInventory().getItemsWithoutQuest().size();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB7);
		
		writeD(0x00);
		writeQ(_money); // current money
		
		if (_loadAll)
		{
			writeD(_listId);
			writeH(_list.size());
			
			for (Product product : _list)
			{
				if ((product.getCount() > 0) || !product.hasLimitedStock())
				{
					writeItem(product);
					
					if ((product.getId() >= 3960) && (product.getId() <= 4026))
					{
						writeQ((long) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
					}
					else
					{
						writeQ((long) (product.getPrice() * (1 + _taxRate)));
					}
				}
			}
		}
		else
		{
			writeD(-1);
			writeH(0);
		}
	}
}
