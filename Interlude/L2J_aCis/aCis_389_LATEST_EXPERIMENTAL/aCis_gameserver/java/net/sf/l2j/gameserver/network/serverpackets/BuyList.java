package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;

public final class BuyList extends L2GameServerPacket
{
	private final int _listId, _money;
	private final Collection<Product> _list;
	private double _taxRate = 0;
	
	public BuyList(NpcBuyList list, int currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x11);
		writeD(_money);
		writeD(_listId);
		writeH(_list.size());
		
		for (Product product : _list)
		{
			if (product.getCount() > 0 || !product.hasLimitedStock())
			{
				writeH(product.getItem().getType1());
				writeD(product.getItemId());
				writeD(product.getItemId());
				writeD((product.getCount() < 0) ? 0 : product.getCount());
				writeH(product.getItem().getType2());
				writeH(0x00); // TODO: ItemInstance getCustomType1()
				writeD(product.getItem().getBodyPart());
				writeH(0x00); // TODO: ItemInstance getEnchantLevel()
				writeH(0x00); // TODO: ItemInstance getCustomType2()
				writeH(0x00);
				
				if (product.getItemId() >= 3960 && product.getItemId() <= 4026)
					writeD((int) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				else
					writeD((int) (product.getPrice() * (1 + _taxRate)));
			}
		}
	}
}