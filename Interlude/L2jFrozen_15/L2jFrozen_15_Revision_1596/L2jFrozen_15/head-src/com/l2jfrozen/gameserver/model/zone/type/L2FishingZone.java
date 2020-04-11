package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;

/**
 * A fishing zone
 * @author durgus
 */
public class L2FishingZone extends L2ZoneType
{
	public L2FishingZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
	}
	
	/*
	 * getWaterZ() this added function returns the Z value for the water surface. In effect this simply returns the upper Z value of the zone. This required some modification of L2ZoneShape, and zone form extentions.
	 */
	public int getWaterZ()
	{
		return getZoneShape().getHighZ();
	}
}
