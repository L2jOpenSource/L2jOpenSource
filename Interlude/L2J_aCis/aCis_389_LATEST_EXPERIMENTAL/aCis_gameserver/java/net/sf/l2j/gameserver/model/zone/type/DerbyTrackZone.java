package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.zone.type.subtype.ZoneType;

/**
 * A zone extending {@link ZoneType} used by Derby Track system.<br>
 * <br>
 * The zone shares peace, no summon and monster track behaviors.
 */
public class DerbyTrackZone extends ZoneType
{
	public DerbyTrackZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Playable)
		{
			character.setInsideZone(ZoneId.MONSTER_TRACK, true);
			character.setInsideZone(ZoneId.PEACE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Playable)
		{
			character.setInsideZone(ZoneId.MONSTER_TRACK, false);
			character.setInsideZone(ZoneId.PEACE, false);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		}
	}
}