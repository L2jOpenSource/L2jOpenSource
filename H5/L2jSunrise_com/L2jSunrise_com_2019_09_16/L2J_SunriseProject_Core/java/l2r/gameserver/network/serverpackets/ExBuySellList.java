package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;

public class ExBuySellList extends AbstractItemPacket
{
	private L2ItemInstance[] _sellList = null;
	private L2ItemInstance[] _refundList = null;
	private final boolean _done;
	
	public ExBuySellList(L2PcInstance player, double taxRate, boolean done)
	{
		_sellList = player.getInventory().getAvailableItems(false, false, false);
		if (player.hasRefund())
		{
			_refundList = player.getRefund().getItems();
		}
		_done = done;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB7);
		
		writeD(0x01);
		
		if ((_sellList != null) && (_sellList.length > 0))
		{
			writeH(_sellList.length);
			for (L2ItemInstance item : _sellList)
			{
				writeItem(item);
				writeQ(item.getItem().getReferencePrice() / 2);
			}
		}
		else
		{
			writeH(0x00);
		}
		
		if ((_refundList != null) && (_refundList.length > 0))
		{
			writeH(_refundList.length);
			int i = 0;
			for (L2ItemInstance item : _refundList)
			{
				writeItem(item);
				writeD(i++);
				writeQ((item.getItem().getReferencePrice() / 2) * item.getCount());
			}
		}
		else
		{
			writeH(0x00);
		}
		
		writeC(_done ? 0x01 : 0x00);
	}
}