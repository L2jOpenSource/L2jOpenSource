package com.l2jfrozen.gameserver.model.base;

/**
 * This class defines all classes (ex : human fighter, darkFighter...) that a player can chose.<BR>
 * <BR>
 * Data :<BR>
 * <BR>
 * <li>id : The Identifier of the class</li>
 * <li>isMage : True if the class is a mage class</li>
 * <li>race : The race of this class</li>
 * <li>parent : The parent ClassId or null if this class is the root</li><BR>
 * <BR>
 * @version $Revision: 1.4.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public enum ClassId
{
	Human_Fighter(0x00, false, Race.human, null),
	Warrior(0x01, false, Race.human, Human_Fighter),
	Gladiator(0x02, false, Race.human, Warrior),
	Warlord(0x03, false, Race.human, Warrior),
	Knight(0x04, false, Race.human, Human_Fighter),
	Paladin(0x05, false, Race.human, Knight),
	Dark_Avenger(0x06, false, Race.human, Knight),
	Rogue(0x07, false, Race.human, Human_Fighter),
	Treasure_Hunter(0x08, false, Race.human, Rogue),
	Hawkeye(0x09, false, Race.human, Rogue),
	
	Human_Mystic(0x0a, true, Race.human, null),
	Human_Wizard(0x0b, true, Race.human, Human_Mystic),
	Sorcerer(0x0c, true, Race.human, Human_Wizard),
	Necromancer(0x0d, true, Race.human, Human_Wizard),
	Warlock(0x0e, true, Race.human, Human_Wizard),
	Cleric(0x0f, true, Race.human, Human_Mystic),
	Bishop(0x10, true, Race.human, Cleric),
	Prophet(0x11, true, Race.human, Cleric),
	
	Elven_Fighter(0x12, false, Race.elf, null),
	Elven_Knight(0x13, false, Race.elf, Elven_Fighter),
	Temple_Knight(0x14, false, Race.elf, Elven_Knight),
	Sword_Singer(0x15, false, Race.elf, Elven_Knight),
	Elven_Scout(0x16, false, Race.elf, Elven_Fighter),
	Plains_Walker(0x17, false, Race.elf, Elven_Scout),
	Silver_Ranger(0x18, false, Race.elf, Elven_Scout),
	
	Elven_Mystic(0x19, true, Race.elf, null),
	Elven_Wizard(0x1a, true, Race.elf, Elven_Mystic),
	Spellsinger(0x1b, true, Race.elf, Elven_Wizard),
	Elemental_Summoner(0x1c, true, Race.elf, Elven_Wizard),
	Oracle(0x1d, true, Race.elf, Elven_Mystic),
	Elder(0x1e, true, Race.elf, Oracle),
	
	Dark_Fighter(0x1f, false, Race.darkelf, null),
	Palus_Knight(0x20, false, Race.darkelf, Dark_Fighter),
	Shillien_Knight(0x21, false, Race.darkelf, Palus_Knight),
	Bladedancer(0x22, false, Race.darkelf, Palus_Knight),
	Assassin(0x23, false, Race.darkelf, Dark_Fighter),
	Abyss_Walker(0x24, false, Race.darkelf, Assassin),
	Phantom_Ranger(0x25, false, Race.darkelf, Assassin),
	
	Dark_Mystic(0x26, true, Race.darkelf, null),
	Dark_Wizard(0x27, true, Race.darkelf, Dark_Mystic),
	Spellhowler(0x28, true, Race.darkelf, Dark_Wizard),
	Phantom_Summoner(0x29, true, Race.darkelf, Dark_Wizard),
	ShillienOracle(0x2a, true, Race.darkelf, Dark_Mystic),
	ShillenElder(0x2b, true, Race.darkelf, ShillienOracle),
	
	Orc_Fighter(0x2c, false, Race.orc, null),
	Orc_Raider(0x2d, false, Race.orc, Orc_Fighter),
	Destroyer(0x2e, false, Race.orc, Orc_Raider),
	Monk(0x2f, false, Race.orc, Orc_Fighter),
	Tyrant(0x30, false, Race.orc, Monk),
	
	Orc_Mystic(0x31, false, Race.orc, null),
	Orc_Shaman(0x32, true, Race.orc, Orc_Mystic),
	Overlord(0x33, true, Race.orc, Orc_Shaman),
	Warcryer(0x34, true, Race.orc, Orc_Shaman),
	
	Dwarven_Fighter(0x35, false, Race.dwarf, null),
	Scavenger(0x36, false, Race.dwarf, Dwarven_Fighter),
	Bounty_Hunter(0x37, false, Race.dwarf, Scavenger),
	Artisan(0x38, false, Race.dwarf, Dwarven_Fighter),
	Warsmith(0x39, false, Race.dwarf, Artisan),
	
	// From 58 to 87 (Decimal number) --> Empty classes id.
	dummyEntry1(58, false, null, null),
	dummyEntry2(59, false, null, null),
	dummyEntry3(60, false, null, null),
	dummyEntry4(61, false, null, null),
	dummyEntry5(62, false, null, null),
	dummyEntry6(63, false, null, null),
	dummyEntry7(64, false, null, null),
	dummyEntry8(65, false, null, null),
	dummyEntry9(66, false, null, null),
	dummyEntry10(67, false, null, null),
	dummyEntry11(68, false, null, null),
	dummyEntry12(69, false, null, null),
	dummyEntry13(70, false, null, null),
	dummyEntry14(71, false, null, null),
	dummyEntry15(72, false, null, null),
	dummyEntry16(73, false, null, null),
	dummyEntry17(74, false, null, null),
	dummyEntry18(75, false, null, null),
	dummyEntry19(76, false, null, null),
	dummyEntry20(77, false, null, null),
	dummyEntry21(78, false, null, null),
	dummyEntry22(79, false, null, null),
	dummyEntry23(80, false, null, null),
	dummyEntry24(81, false, null, null),
	dummyEntry25(82, false, null, null),
	dummyEntry26(83, false, null, null),
	dummyEntry27(84, false, null, null),
	dummyEntry28(85, false, null, null),
	dummyEntry29(86, false, null, null),
	dummyEntry30(87, false, null, null),
	
	Duelist(0x58, false, Race.human, Gladiator),
	Dreadnought(0x59, false, Race.human, Warlord),
	Phoenix_Knight(0x5a, false, Race.human, Paladin),
	Hell_Knight(0x5b, false, Race.human, Dark_Avenger),
	Sagittarius(0x5c, false, Race.human, Hawkeye),
	Adventurer(0x5d, false, Race.human, Treasure_Hunter),
	
	Archmage(0x5e, true, Race.human, Sorcerer),
	Soultaker(0x5f, true, Race.human, Necromancer),
	Arcana_Lord(0x60, true, Race.human, Warlock),
	Cardinal(0x61, true, Race.human, Bishop),
	Hierophant(0x62, true, Race.human, Prophet),
	
	Evas_Templar(0x63, false, Race.elf, Temple_Knight),
	Sword_Muse(0x64, false, Race.elf, Sword_Singer),
	Wind_Rider(0x65, false, Race.elf, Plains_Walker),
	Moonlight_Sentinel(0x66, false, Race.elf, Silver_Ranger),
	
	Mystic_Muse(0x67, true, Race.elf, Spellsinger),
	Elemental_Master(0x68, true, Race.elf, Elemental_Summoner),
	Evas_Saint(0x69, true, Race.elf, Elder),
	
	Shillien_Templar(0x6a, false, Race.darkelf, Shillien_Knight),
	Spectral_Dancer(0x6b, false, Race.darkelf, Bladedancer),
	Ghost_Hunter(0x6c, false, Race.darkelf, Abyss_Walker),
	Ghost_Sentinel(0x6d, false, Race.darkelf, Phantom_Ranger),
	
	Storm_Screamer(0x6e, true, Race.darkelf, Spellhowler),
	Spectral_Master(0x6f, true, Race.darkelf, Phantom_Summoner),
	Shillien_Saint(0x70, true, Race.darkelf, ShillenElder),
	
	Titan(0x71, false, Race.orc, Destroyer),
	GrandKhauatari(0x72, false, Race.orc, Tyrant),
	
	Dominator(0x73, true, Race.orc, Overlord),
	Doomcryer(0x74, true, Race.orc, Warcryer),
	Fortune_Seeker(0x75, false, Race.dwarf, Bounty_Hunter),
	Maestro(0x76, false, Race.dwarf, Warsmith);
	
	private final int id;
	private final boolean isMage;
	private final Race race;
	private final ClassId parent;
	
	private ClassId(int pId, boolean pIsMage, Race pRace, ClassId pParent)
	{
		id = pId;
		isMage = pIsMage;
		race = pRace;
		parent = pParent;
	}
	
	public int getId()
	{
		return id;
	}
	
	public boolean isMage()
	{
		return isMage;
	}
	
	public Race getRace()
	{
		return race;
	}
	
	/**
	 * Return True if this Class is a child of the selected ClassId.<BR>
	 * <BR>
	 * @param  cid The parent ClassId to check
	 * @return     true, if successful
	 */
	public boolean childOf(ClassId cid)
	{
		if (parent == null)
		{
			return false;
		}
		
		if (parent == cid)
		{
			return true;
		}
		
		return parent.childOf(cid);
	}
	
	/**
	 * Return True if this Class is equal to the selected ClassId or a child of the selected ClassId.<BR>
	 * <BR>
	 * @param  cid The parent ClassId to check
	 * @return     true, if successful
	 */
	public boolean equalsOrChildOf(ClassId cid)
	{
		return this == cid || childOf(cid);
	}
	
	/**
	 * @return The child level of this Class (0=root, 1=child leve 1...).
	 */
	public int level()
	{
		if (parent == null)
		{
			return 0;
		}
		
		return 1 + parent.level();
	}
	
	/**
	 * @return parent ClassId<BR>
	 *         <B>For example:</B> Parent of Duelist is Gladiator. <BR>
	 */
	public ClassId getParent()
	{
		return parent;
	}
	
	public static ClassId getClassIdById(int id)
	{
		for (ClassId current : values())
		{
			if (current.id == id)
			{
				return current;
			}
		}
		return null;
	}
	
	/**
	 * @return The name of the enum for example mysticMuse should be Mystic Muse
	 */
	public String getName()
	{
		return toString().replace("_", " ");
	}
}
