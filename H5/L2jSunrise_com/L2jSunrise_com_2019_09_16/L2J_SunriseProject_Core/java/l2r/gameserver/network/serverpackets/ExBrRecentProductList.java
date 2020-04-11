package l2r.gameserver.network.serverpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import l2r.L2DatabaseFactory;
import l2r.gameserver.data.xml.impl.ProductItemData;
import l2r.gameserver.model.primeshop.L2ProductItem;

/**
 * Created by GodFather
 */
public class ExBrRecentProductList extends L2GameServerPacket
{
	List<L2ProductItem> list = new ArrayList<>();
	
	public ExBrRecentProductList(int objId)
	{
		list = ProductItemData.getInstance().getRecentListByOID(objId);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT productId FROM character_item_mall_transactions WHERE charId=? ORDER BY transactionTime DESC"))
		{
			statement.setInt(1, objId);
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final L2ProductItem product = ProductItemData.getInstance().getProduct(rset.getInt("productId"));
					if ((product != null) && !list.contains(product))
					{
						list.add(product);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("Could not restore Item Mall transaction: " + e.getMessage(), e);
		}
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