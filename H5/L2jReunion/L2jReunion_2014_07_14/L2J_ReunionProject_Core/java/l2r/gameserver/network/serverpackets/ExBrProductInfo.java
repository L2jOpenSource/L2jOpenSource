package l2r.gameserver.network.serverpackets;

import l2r.gameserver.datatables.xml.ProductItemData;
import l2r.gameserver.model.primeshop.L2ProductItem;
import l2r.gameserver.model.primeshop.L2ProductItemComponent;

/**
 * Created by GodFather
 */
public class ExBrProductInfo extends L2GameServerPacket
{
	private final L2ProductItem _productId;
	
	public ExBrProductInfo(int id)
	{
		_productId = ProductItemData.getInstance().getProduct(id);
	}
	
	@Override
	protected void writeImpl()
	{
		if (_productId == null)
		{
			return;
		}
		
		writeC(0xFE);
		writeH(0xD7);
		
		writeD(_productId.getProductId());
		writeD(_productId.getPoints());
		writeD(_productId.getComponents().size());
		
		for (L2ProductItemComponent com : _productId.getComponents())
		{
			writeD(com.getId());
			writeD(com.getCount());
			writeD(com.getWeight());
			writeD(com.isDropable() ? 1 : 0);
		}
	}
}