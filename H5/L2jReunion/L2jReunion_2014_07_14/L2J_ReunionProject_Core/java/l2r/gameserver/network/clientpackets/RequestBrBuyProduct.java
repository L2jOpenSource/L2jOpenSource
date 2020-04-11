package l2r.gameserver.network.clientpackets;

import l2r.gameserver.datatables.xml.ProductItemData;
import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * Created by GodFather
 */
public class RequestBrBuyProduct extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0 68 RequestBrBuyProduct";
	
	private int _productId;
	private int _count;
	
	@Override
	protected void readImpl()
	{
		_productId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		ProductItemData.getInstance().requestBuyItem(player, _productId, _count);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}