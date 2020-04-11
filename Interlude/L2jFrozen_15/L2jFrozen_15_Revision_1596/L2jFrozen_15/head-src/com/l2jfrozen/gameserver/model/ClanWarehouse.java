package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance.ItemLocation;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class ClanWarehouse extends Warehouse
{
	// private static final Logger LOGGER = Logger.getLogger(PcWarehouse.class);
	
	private final L2Clan clan;
	
	public ClanWarehouse(final L2Clan clan)
	{
		this.clan = clan;
	}
	
	@Override
	public int getOwnerId()
	{
		return clan.getClanId();
	}
	
	@Override
	public L2PcInstance getOwner()
	{
		return clan.getLeader().getPlayerInstance();
	}
	
	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}
	
	public String getLocationId()
	{
		return "0";
	}
	
	public int getLocationId(final boolean dummy)
	{
		return 0;
	}
	
	public void setLocationId(final L2PcInstance dummy)
	{
	}
	
	@Override
	public boolean validateCapacity(final int slots)
	{
		return itemsList.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
	}
}
