package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType}, used to restrict {@link Player}s to enter mounted on wyverns.<br>
 * <br>
 * A task and a message is called if event is triggered. If the player didn't leave after 5 seconds, he will be dismounted.
 */
public class NoLandingZone extends ZoneType
{
	public NoLandingZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.NO_LANDING, true);
			
			((Player) character).enterOnNoLandingZone();
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.NO_LANDING, false);
			
			((Player) character).exitOnNoLandingZone();
		}
	}
}