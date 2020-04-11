package l2r.gameserver.network.clientpackets;

import l2r.gameserver.datatables.xml.ProductItemData;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by GodFather
 */
public class RequestBrRecentProductList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		ProductItemData.getInstance().recentProductList(player);
	}
	
	@Override
	public String getType()
	{
		return null;
	}
}