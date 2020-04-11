package l2r.gameserver.network.serverpackets;

import java.util.Collection;

import l2r.gameserver.datatables.xml.ProductItemData;
import l2r.gameserver.model.primeshop.L2ProductItem;

/**
 * Created by GodFather
 */
public class ExBrProductList extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD6);
		Collection<L2ProductItem> items = ProductItemData.getInstance().getAllItems();
		writeD(items.size());
		
		for (L2ProductItem template : items)
		{
			if (System.currentTimeMillis() < template.getStartTimeSale())
			{
				continue;
			}
			
			if (System.currentTimeMillis() > template.getEndTimeSale())
			{
				continue;
			}
			
			writeD(template.getProductId());
			writeH(template.getCategory());
			writeD(template.getPoints());
			writeD(template.getTabId());
			writeD((int) (template.getStartTimeSale() / 1000));
			writeD((int) (template.getEndTimeSale() / 1000));
			writeC(127);
			writeC(template.getStartHour());
			writeC(template.getStartMin());
			writeC(template.getEndHour());
			writeC(template.getEndMin());
			writeD(0);
			writeD(-1);
		}
	}
}