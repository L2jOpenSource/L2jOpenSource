package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.TradeItem;

public final class TradeOwnAdd extends AbstractItemPacket
{
	private final TradeItem _item;
	
	public TradeOwnAdd(TradeItem item)
	{
		_item = item;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1A);
		
		writeH(1); // items added count
		writeH(0);
		writeItem(_item, true);
	}
}
