package l2r.gameserver.network.serverpackets;

import java.util.Collection;

import l2r.gameserver.data.xml.impl.ProductItemData;
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
			
			writeD(template.getProductId()); // product id
			writeH(template.getCategory()); // category 1 - enchant 2 - supplies 3 - decoration 4 - package 5 - other
			writeD(template.getPoints()); // points
			writeD(template.getTabId()); // show tab 2-th group - 1 shows a window about an item
			writeD((int) (template.getStartTimeSale() / 1000)); // start sale unix date in seconds
			writeD((int) (template.getEndTimeSale() / 1000)); // end sale unix date in seconds
			writeC(127); // day week (127 = not daily goods)
			writeC(template.getStartHour()); // start hour
			writeC(template.getStartMin()); // start min
			writeC(template.getEndHour()); // end hour
			writeC(template.getEndMin()); // end min
			writeD(0); // stock
			writeD(-1); // max stock
		}
	}
}