package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType}, used for jail behavior. It is impossible to summon friends and use shops inside it.
 */
public class JailZone extends ZoneType
{
	public JailZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.JAIL, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			character.setInsideZone(ZoneId.NO_STORE, true);
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.JAIL, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
			character.setInsideZone(ZoneId.NO_STORE, false);
		}
	}
}