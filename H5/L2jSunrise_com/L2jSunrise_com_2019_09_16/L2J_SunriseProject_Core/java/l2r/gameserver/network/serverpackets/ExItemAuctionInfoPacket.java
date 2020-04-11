package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.itemauction.ItemAuction;
import l2r.gameserver.model.itemauction.ItemAuctionBid;
import l2r.gameserver.model.itemauction.ItemAuctionState;

/**
 * @author vGodFather
 */
public final class ExItemAuctionInfoPacket extends AbstractItemPacket
{
	private final boolean _refresh;
	private final int _timeRemaining;
	private final ItemAuction _currentAuction;
	private final ItemAuction _nextAuction;
	
	public ExItemAuctionInfoPacket(final boolean refresh, final ItemAuction currentAuction, final ItemAuction nextAuction)
	{
		if (currentAuction == null)
		{
			throw new NullPointerException();
		}
		
		_timeRemaining = currentAuction.getAuctionState() != ItemAuctionState.STARTED ? 0 : (int) (currentAuction.getFinishingTimeRemaining() / 1000); // in seconds;
		_refresh = refresh;
		_currentAuction = currentAuction;
		_nextAuction = nextAuction;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x68);
		writeC(_refresh ? 0x00 : 0x01);
		writeD(_currentAuction.getInstanceId());
		
		final ItemAuctionBid highestBid = _currentAuction.getHighestBid();
		writeQ(highestBid != null ? highestBid.getLastBid() : _currentAuction.getAuctionInitBid());
		
		writeD(_timeRemaining);
		writeItem(_currentAuction.getItemInfo());
		
		if (_nextAuction != null)
		{
			writeQ(_nextAuction.getAuctionInitBid());
			writeD((int) (_nextAuction.getStartingTime() / 1000)); // unix time in seconds
			writeItem(_nextAuction.getItemInfo());
		}
	}
}