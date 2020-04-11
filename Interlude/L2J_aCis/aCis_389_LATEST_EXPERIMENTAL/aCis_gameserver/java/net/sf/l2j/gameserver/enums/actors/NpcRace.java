package net.sf.l2j.gameserver.enums.actors;

import net.sf.l2j.gameserver.enums.skills.Stats;

public enum NpcRace
{
	UNKNOWN(null, null),
	UNDEAD(null, null),
	MAGICCREATURE(Stats.PATK_MCREATURES, Stats.PDEF_MCREATURES),
	BEAST(Stats.PATK_MONSTERS, Stats.PDEF_MONSTERS),
	ANIMAL(Stats.PATK_ANIMALS, Stats.PDEF_ANIMALS),
	PLANT(Stats.PATK_PLANTS, Stats.PDEF_PLANTS),
	HUMANOID(null, null),
	SPIRIT(null, null),
	ANGEL(null, null),
	DEMON(null, null),
	DRAGON(Stats.PATK_DRAGONS, Stats.PDEF_DRAGONS),
	GIANT(Stats.PATK_GIANTS, Stats.PDEF_GIANTS),
	BUG(Stats.PATK_INSECTS, Stats.PDEF_INSECTS),
	FAIRIE(null, null),
	HUMAN(null, null),
	ELVE(null, null),
	DARKELVE(null, null),
	ORC(null, null),
	DWARVE(null, null),
	OTHER(null, null),
	NONLIVING(null, null),
	SIEGEWEAPON(null, null),
	DEFENDINGARMY(null, null),
	MERCENARIE(null, null);
	
	public static final NpcRace[] VALUES = values();
	
	private NpcRace(Stats atkStat, Stats resStat)
	{
		_atkStat = atkStat;
		_resStat = resStat;
	}
	
	private Stats _atkStat;
	private Stats _resStat;
	
	public Stats getAtkStat()
	{
		return _atkStat;
	}
	
	public Stats getResStat()
	{
		return _resStat;
	}
}