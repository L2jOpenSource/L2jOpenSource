package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.TradeItem;

public final class TradeOtherAdd extends AbstractItemPacket
{
	private final TradeItem _item;
	
	public TradeOtherAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1b);
		
		writeH(1); // item count
		writeH(0);
		writeItem(_item, true);
	}
}
