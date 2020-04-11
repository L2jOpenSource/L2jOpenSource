package com.l2jfrozen.gameserver.model.base;

import static com.l2jfrozen.gameserver.model.base.ClassLevel.First;
import static com.l2jfrozen.gameserver.model.base.ClassLevel.Fourth;
import static com.l2jfrozen.gameserver.model.base.ClassLevel.Second;
import static com.l2jfrozen.gameserver.model.base.ClassLevel.Third;
import static com.l2jfrozen.gameserver.model.base.ClassType.Fighter;
import static com.l2jfrozen.gameserver.model.base.ClassType.Mystic;
import static com.l2jfrozen.gameserver.model.base.ClassType.Priest;
import static com.l2jfrozen.gameserver.model.base.PlayerRace.DarkElf;
import static com.l2jfrozen.gameserver.model.base.PlayerRace.Dwarf;
import static com.l2jfrozen.gameserver.model.base.PlayerRace.Human;
import static com.l2jfrozen.gameserver.model.base.PlayerRace.LightElf;
import static com.l2jfrozen.gameserver.model.base.PlayerRace.Orc;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author luisantonioa, programmos, l2jfrozen dev
 */
public enum PlayerClass
{
	HumanFighter(Human, Fighter, First),
	Warrior(Human, Fighter, Second),
	Gladiator(Human, Fighter, Third),
	Warlord(Human, Fighter, Third),
	HumanKnight(Human, Fighter, Second),
	Paladin(Human, Fighter, Third),
	DarkAvenger(Human, Fighter, Third),
	Rogue(Human, Fighter, Second),
	TreasureHunter(Human, Fighter, Third),
	Hawkeye(Human, Fighter, Third),
	HumanMystic(Human, Mystic, First),
	HumanWizard(Human, Mystic, Second),
	Sorceror(Human, Mystic, Third),
	Necromancer(Human, Mystic, Third),
	Warlock(Human, Mystic, Third),
	Cleric(Human, Priest, Second),
	Bishop(Human, Priest, Third),
	Prophet(Human, Priest, Third),
	
	ElvenFighter(LightElf, Fighter, First),
	ElvenKnight(LightElf, Fighter, Second),
	TempleKnight(LightElf, Fighter, Third),
	Swordsinger(LightElf, Fighter, Third),
	ElvenScout(LightElf, Fighter, Second),
	Plainswalker(LightElf, Fighter, Third),
	SilverRanger(LightElf, Fighter, Third),
	ElvenMystic(LightElf, Mystic, First),
	ElvenWizard(LightElf, Mystic, Second),
	Spellsinger(LightElf, Mystic, Third),
	ElementalSummoner(LightElf, Mystic, Third),
	ElvenOracle(LightElf, Priest, Second),
	ElvenElder(LightElf, Priest, Third),
	
	DarkElvenFighter(DarkElf, Fighter, First),
	PalusKnight(DarkElf, Fighter, Second),
	ShillienKnight(DarkElf, Fighter, Third),
	Bladedancer(DarkElf, Fighter, Third),
	Assassin(DarkElf, Fighter, Second),
	AbyssWalker(DarkElf, Fighter, Third),
	PhantomRanger(DarkElf, Fighter, Third),
	DarkElvenMystic(DarkElf, Mystic, First),
	DarkElvenWizard(DarkElf, Mystic, Second),
	Spellhowler(DarkElf, Mystic, Third),
	PhantomSummoner(DarkElf, Mystic, Third),
	ShillienOracle(DarkElf, Priest, Second),
	ShillienElder(DarkElf, Priest, Third),
	
	OrcFighter(Orc, Fighter, First),
	OrcRaider(Orc, Fighter, Second),
	Destroyer(Orc, Fighter, Third),
	OrcMonk(Orc, Fighter, Second),
	Tyrant(Orc, Fighter, Third),
	OrcMystic(Orc, Mystic, First),
	OrcShaman(Orc, Mystic, Second),
	Overlord(Orc, Mystic, Third),
	Warcryer(Orc, Mystic, Third),
	
	DwarvenFighter(Dwarf, Fighter, First),
	DwarvenScavenger(Dwarf, Fighter, Second),
	BountyHunter(Dwarf, Fighter, Third),
	DwarvenArtisan(Dwarf, Fighter, Second),
	Warsmith(Dwarf, Fighter, Third),
	
	dummyEntry1(null, null, null),
	dummyEntry2(null, null, null),
	dummyEntry3(null, null, null),
	dummyEntry4(null, null, null),
	dummyEntry5(null, null, null),
	dummyEntry6(null, null, null),
	dummyEntry7(null, null, null),
	dummyEntry8(null, null, null),
	dummyEntry9(null, null, null),
	dummyEntry10(null, null, null),
	dummyEntry11(null, null, null),
	dummyEntry12(null, null, null),
	dummyEntry13(null, null, null),
	dummyEntry14(null, null, null),
	dummyEntry15(null, null, null),
	dummyEntry16(null, null, null),
	dummyEntry17(null, null, null),
	dummyEntry18(null, null, null),
	dummyEntry19(null, null, null),
	dummyEntry20(null, null, null),
	dummyEntry21(null, null, null),
	dummyEntry22(null, null, null),
	dummyEntry23(null, null, null),
	dummyEntry24(null, null, null),
	dummyEntry25(null, null, null),
	dummyEntry26(null, null, null),
	dummyEntry27(null, null, null),
	dummyEntry28(null, null, null),
	dummyEntry29(null, null, null),
	dummyEntry30(null, null, null),
	
	// (3rd classes)
	duelist(Human, Fighter, Fourth),
	dreadnought(Human, Fighter, Fourth),
	phoenixKnight(Human, Fighter, Fourth),
	hellKnight(Human, Fighter, Fourth),
	sagittarius(Human, Fighter, Fourth),
	adventurer(Human, Fighter, Fourth),
	archmage(Human, Mystic, Fourth),
	soultaker(Human, Mystic, Fourth),
	arcanaLord(Human, Mystic, Fourth),
	cardinal(Human, Mystic, Fourth),
	hierophant(Human, Mystic, Fourth),
	
	evaTemplar(LightElf, Fighter, Fourth),
	swordMuse(LightElf, Fighter, Fourth),
	windRider(LightElf, Fighter, Fourth),
	moonlightSentinel(LightElf, Fighter, Fourth),
	mysticMuse(LightElf, Mystic, Fourth),
	elementalMaster(LightElf, Mystic, Fourth),
	evaSaint(LightElf, Mystic, Fourth),
	
	shillienTemplar(DarkElf, Fighter, Fourth),
	spectralDancer(DarkElf, Fighter, Fourth),
	ghostHunter(DarkElf, Fighter, Fourth),
	ghostSentinel(DarkElf, Fighter, Fourth),
	stormScreamer(DarkElf, Mystic, Fourth),
	spectralMaster(DarkElf, Mystic, Fourth),
	shillienSaint(DarkElf, Mystic, Fourth),
	
	titan(Orc, Fighter, Fourth),
	grandKhauatari(Orc, Fighter, Fourth),
	dominator(Orc, Mystic, Fourth),
	doomcryer(Orc, Mystic, Fourth),
	
	fortuneSeeker(Dwarf, Fighter, Fourth),
	maestro(Dwarf, Fighter, Fourth);
	
	private PlayerRace race;
	private ClassLevel level;
	private ClassType type;
	
	private static final Set<PlayerClass> mainSubclassSet;
	private static final Set<PlayerClass> neverSubclassed = EnumSet.of(Overlord, Warsmith);
	
	private static final Set<PlayerClass> subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);
	private static final Set<PlayerClass> subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);
	private static final Set<PlayerClass> subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);
	private static final Set<PlayerClass> subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);
	private static final Set<PlayerClass> subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);
	
	private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap = new EnumMap<>(PlayerClass.class);
	
	static
	{
		Set<PlayerClass> subclasses = getSet(null, Third);
		subclasses.removeAll(neverSubclassed);
		
		mainSubclassSet = subclasses;
		
		subclassSetMap.put(DarkAvenger, subclasseSet1);
		subclassSetMap.put(Paladin, subclasseSet1);
		subclassSetMap.put(TempleKnight, subclasseSet1);
		subclassSetMap.put(ShillienKnight, subclasseSet1);
		
		subclassSetMap.put(TreasureHunter, subclasseSet2);
		subclassSetMap.put(AbyssWalker, subclasseSet2);
		subclassSetMap.put(Plainswalker, subclasseSet2);
		
		subclassSetMap.put(Hawkeye, subclasseSet3);
		subclassSetMap.put(SilverRanger, subclasseSet3);
		subclassSetMap.put(PhantomRanger, subclasseSet3);
		
		subclassSetMap.put(Warlock, subclasseSet4);
		subclassSetMap.put(ElementalSummoner, subclasseSet4);
		subclassSetMap.put(PhantomSummoner, subclasseSet4);
		
		subclassSetMap.put(Sorceror, subclasseSet5);
		subclassSetMap.put(Spellsinger, subclasseSet5);
		subclassSetMap.put(Spellhowler, subclasseSet5);
		
		subclasses = null;
	}
	
	PlayerClass(final PlayerRace pRace, final ClassType pType, final ClassLevel pLevel)
	{
		race = pRace;
		level = pLevel;
		type = pType;
	}
	
	public final Set<PlayerClass> getAvailableSubclasses(final L2PcInstance player)
	{
		Set<PlayerClass> subclasses = null;
		
		if (level == Third)
		{
			subclasses = EnumSet.copyOf(mainSubclassSet);
			
			subclasses.removeAll(neverSubclassed);
			subclasses.remove(this);
			
			switch (player.getRace())
			{
				case elf:
					subclasses.removeAll(getSet(DarkElf, Third));
					break;
				case darkelf:
					subclasses.removeAll(getSet(LightElf, Third));
					break;
			}
			
			Set<PlayerClass> unavailableClasses = subclassSetMap.get(this);
			
			if (unavailableClasses != null)
			{
				subclasses.removeAll(unavailableClasses);
			}
			
			unavailableClasses = null;
		}
		
		return subclasses;
	}
	
	public static final EnumSet<PlayerClass> getSet(final PlayerRace race, final ClassLevel level)
	{
		final EnumSet<PlayerClass> allOf = EnumSet.noneOf(PlayerClass.class);
		
		for (final PlayerClass playerClass : EnumSet.allOf(PlayerClass.class))
		{
			if (race == null || playerClass.isOfRace(race))
			{
				if (level == null || playerClass.isOfLevel(level))
				{
					allOf.add(playerClass);
				}
			}
		}
		
		return allOf;
	}
	
	public final boolean isOfRace(final PlayerRace pRace)
	{
		return race == pRace;
	}
	
	public final boolean isOfType(final ClassType pType)
	{
		return type == pType;
	}
	
	public final boolean isOfLevel(final ClassLevel pLevel)
	{
		return level == pLevel;
	}
	
	public final ClassLevel getLevel()
	{
		return level;
	}
}
