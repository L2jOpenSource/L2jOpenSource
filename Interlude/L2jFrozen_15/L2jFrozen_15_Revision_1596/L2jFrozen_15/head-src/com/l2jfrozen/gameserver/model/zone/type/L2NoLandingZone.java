package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;

/**
 * A no landing zone
 * @author durgus
 */
public class L2NoLandingZone extends L2ZoneType
{
	public L2NoLandingZone(final int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_NOLANDING, true);
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_NOLANDING, false);
		}
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
	}
}
