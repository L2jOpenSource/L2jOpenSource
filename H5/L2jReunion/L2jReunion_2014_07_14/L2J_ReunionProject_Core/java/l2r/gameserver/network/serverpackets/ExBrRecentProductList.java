package l2r.gameserver.network.serverpackets;

import java.util.List;

import l2r.gameserver.datatables.xml.ProductItemData;
import l2r.gameserver.model.primeshop.L2ProductItem;

/**
 * Created by GodFather
 */
public class ExBrRecentProductList extends L2GameServerPacket
{
	List<L2ProductItem> list;
	
	public ExBrRecentProductList(int objId)
	{
		list = ProductItemData.getInstance().getRecentListByOID(objId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xDC);
		writeD(list.size());
		for (L2ProductItem template : list)
		{
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