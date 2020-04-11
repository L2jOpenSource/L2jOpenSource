package l2s.gameserver.model.items;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;

public final class ClanWarehouse extends Warehouse
{
	public ClanWarehouse(Clan clan)
	{
		super(clan.getClanId());
	}

	@Override
	public ItemLocation getItemLocation()
	{
		return ItemLocation.CLANWH;
	}
}