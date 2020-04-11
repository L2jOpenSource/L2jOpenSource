package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType} where the use of "summoning friend" skill isn't allowed.
 */
public class NoSummonFriendZone extends ZoneType
{
	public NoSummonFriendZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
	}
}