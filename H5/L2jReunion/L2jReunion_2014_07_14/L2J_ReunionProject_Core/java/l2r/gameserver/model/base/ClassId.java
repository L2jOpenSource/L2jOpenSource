/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.base;

import l2r.gameserver.enums.PcRace;
import l2r.gameserver.model.interfaces.IIdentifiable;

/**
 * This class defines all classes (ex : human fighter, darkFighter...) that a player can chose.<br>
 * Data:
 * <ul>
 * <li>id : The Identifier of the class</li>
 * <li>isMage : True if the class is a mage class</li>
 * <li>race : The race of this class</li>
 * <li>parent : The parent ClassId or null if this class is the root</li>
 * </ul>
 * @version $Revision: 1.4.4.4 $ $Date: 2005/03/27 15:29:33 $
 */
public enum ClassId implements IIdentifiable
{
	fighter(0x00, false, PcRace.Human, null),
	
	warrior(0x01, false, PcRace.Human, fighter),
	gladiator(0x02, false, PcRace.Human, warrior),
	warlord(0x03, false, PcRace.Human, warrior),
	knight(0x04, false, PcRace.Human, fighter),
	paladin(0x05, false, PcRace.Human, knight),
	darkAvenger(0x06, false, PcRace.Human, knight),
	rogue(0x07, false, PcRace.Human, fighter),
	treasureHunter(0x08, false, PcRace.Human, rogue),
	hawkeye(0x09, false, PcRace.Human, rogue),
	
	mage(0x0a, true, PcRace.Human, null),
	wizard(0x0b, true, PcRace.Human, mage),
	sorceror(0x0c, true, PcRace.Human, wizard),
	necromancer(0x0d, true, PcRace.Human, wizard),
	warlock(0x0e, true, true, PcRace.Human, wizard),
	cleric(0x0f, true, PcRace.Human, mage),
	bishop(0x10, true, PcRace.Human, cleric),
	prophet(0x11, true, PcRace.Human, cleric),
	
	elvenFighter(0x12, false, PcRace.Elf, null),
	elvenKnight(0x13, false, PcRace.Elf, elvenFighter),
	templeKnight(0x14, false, PcRace.Elf, elvenKnight),
	swordSinger(0x15, false, PcRace.Elf, elvenKnight),
	elvenScout(0x16, false, PcRace.Elf, elvenFighter),
	plainsWalker(0x17, false, PcRace.Elf, elvenScout),
	silverRanger(0x18, false, PcRace.Elf, elvenScout),
	
	elvenMage(0x19, true, PcRace.Elf, null),
	elvenWizard(0x1a, true, PcRace.Elf, elvenMage),
	spellsinger(0x1b, true, PcRace.Elf, elvenWizard),
	elementalSummoner(0x1c, true, true, PcRace.Elf, elvenWizard),
	oracle(0x1d, true, PcRace.Elf, elvenMage),
	elder(0x1e, true, PcRace.Elf, oracle),
	
	darkFighter(0x1f, false, PcRace.DarkElf, null),
	palusKnight(0x20, false, PcRace.DarkElf, darkFighter),
	shillienKnight(0x21, false, PcRace.DarkElf, palusKnight),
	bladedancer(0x22, false, PcRace.DarkElf, palusKnight),
	assassin(0x23, false, PcRace.DarkElf, darkFighter),
	abyssWalker(0x24, false, PcRace.DarkElf, assassin),
	phantomRanger(0x25, false, PcRace.DarkElf, assassin),
	
	darkMage(0x26, true, PcRace.DarkElf, null),
	darkWizard(0x27, true, PcRace.DarkElf, darkMage),
	spellhowler(0x28, true, PcRace.DarkElf, darkWizard),
	phantomSummoner(0x29, true, true, PcRace.DarkElf, darkWizard),
	shillienOracle(0x2a, true, PcRace.DarkElf, darkMage),
	shillenElder(0x2b, true, PcRace.DarkElf, shillienOracle),
	
	orcFighter(0x2c, false, PcRace.Orc, null),
	orcRaider(0x2d, false, PcRace.Orc, orcFighter),
	destroyer(0x2e, false, PcRace.Orc, orcRaider),
	orcMonk(0x2f, false, PcRace.Orc, orcFighter),
	tyrant(0x30, false, PcRace.Orc, orcMonk),
	
	orcMage(0x31, false, PcRace.Orc, null),
	orcShaman(0x32, true, PcRace.Orc, orcMage),
	overlord(0x33, true, PcRace.Orc, orcShaman),
	warcryer(0x34, true, PcRace.Orc, orcShaman),
	
	dwarvenFighter(0x35, false, PcRace.Dwarf, null),
	scavenger(0x36, false, PcRace.Dwarf, dwarvenFighter),
	bountyHunter(0x37, false, PcRace.Dwarf, scavenger),
	artisan(0x38, false, PcRace.Dwarf, dwarvenFighter),
	warsmith(0x39, false, PcRace.Dwarf, artisan),
	
	/*
	 * Dummy Entries (id's already in decimal format) btw FU NCSoft for the amount of work you put me through to do this!! <START>
	 */
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
	/*
	 * <END> Of Dummy entries
	 */
	
	/*
	 * Now the bad boys! new class ids :)) (3rd classes)
	 */
	duelist(0x58, false, PcRace.Human, gladiator),
	dreadnought(0x59, false, PcRace.Human, warlord),
	phoenixKnight(0x5a, false, PcRace.Human, paladin),
	hellKnight(0x5b, false, PcRace.Human, darkAvenger),
	sagittarius(0x5c, false, PcRace.Human, hawkeye),
	adventurer(0x5d, false, PcRace.Human, treasureHunter),
	archmage(0x5e, true, PcRace.Human, sorceror),
	soultaker(0x5f, true, PcRace.Human, necromancer),
	arcanaLord(0x60, true, true, PcRace.Human, warlock),
	cardinal(0x61, true, PcRace.Human, bishop),
	hierophant(0x62, true, PcRace.Human, prophet),
	
	evaTemplar(0x63, false, PcRace.Elf, templeKnight),
	swordMuse(0x64, false, PcRace.Elf, swordSinger),
	windRider(0x65, false, PcRace.Elf, plainsWalker),
	moonlightSentinel(0x66, false, PcRace.Elf, silverRanger),
	mysticMuse(0x67, true, PcRace.Elf, spellsinger),
	elementalMaster(0x68, true, true, PcRace.Elf, elementalSummoner),
	evaSaint(0x69, true, PcRace.Elf, elder),
	
	shillienTemplar(0x6a, false, PcRace.DarkElf, shillienKnight),
	spectralDancer(0x6b, false, PcRace.DarkElf, bladedancer),
	ghostHunter(0x6c, false, PcRace.DarkElf, abyssWalker),
	ghostSentinel(0x6d, false, PcRace.DarkElf, phantomRanger),
	stormScreamer(0x6e, true, PcRace.DarkElf, spellhowler),
	spectralMaster(0x6f, true, true, PcRace.DarkElf, phantomSummoner),
	shillienSaint(0x70, true, PcRace.DarkElf, shillenElder),
	
	titan(0x71, false, PcRace.Orc, destroyer),
	grandKhavatari(0x72, false, PcRace.Orc, tyrant),
	dominator(0x73, true, PcRace.Orc, overlord),
	doomcryer(0x74, true, PcRace.Orc, warcryer),
	
	fortuneSeeker(0x75, false, PcRace.Dwarf, bountyHunter),
	maestro(0x76, false, PcRace.Dwarf, warsmith),
	
	dummyEntry31(0x77, false, null, null),
	dummyEntry32(0x78, false, null, null),
	dummyEntry33(0x79, false, null, null),
	dummyEntry34(0x7a, false, null, null),
	
	maleSoldier(0x7b, false, PcRace.Kamael, null),
	femaleSoldier(0x7C, false, PcRace.Kamael, null),
	trooper(0x7D, false, PcRace.Kamael, maleSoldier),
	warder(0x7E, false, PcRace.Kamael, femaleSoldier),
	berserker(0x7F, false, PcRace.Kamael, trooper),
	maleSoulbreaker(0x80, false, PcRace.Kamael, trooper),
	femaleSoulbreaker(0x81, false, PcRace.Kamael, warder),
	arbalester(0x82, false, PcRace.Kamael, warder),
	doombringer(0x83, false, PcRace.Kamael, berserker),
	maleSoulhound(0x84, false, PcRace.Kamael, maleSoulbreaker),
	femaleSoulhound(0x85, false, PcRace.Kamael, femaleSoulbreaker),
	trickster(0x86, false, PcRace.Kamael, arbalester),
	inspector(0x87, false, PcRace.Kamael, warder), // DS: yes, both male/female inspectors use skills from warder
	judicator(0x88, false, PcRace.Kamael, inspector);
	
	/** The Identifier of the Class */
	private final int _id;
	
	/** True if the class is a mage class */
	private final boolean _isMage;
	
	/** True if the class is a summoner class */
	private final boolean _isSummoner;
	
	/** The Race object of the class */
	private final PcRace _race;
	
	/** The parent ClassId or null if this class is a root */
	private final ClassId _parent;
	
	/**
	 * Class constructor.
	 * @param pId the class Id.
	 * @param pIsMage {code true} if the class is mage class.
	 * @param pRace the race related to the class.
	 * @param pParent the parent class Id.
	 */
	private ClassId(int pId, boolean pIsMage, PcRace pRace, ClassId pParent)
	{
		_id = pId;
		_isMage = pIsMage;
		_isSummoner = false;
		_race = pRace;
		_parent = pParent;
	}
	
	/**
	 * Class constructor.
	 * @param pId the class Id.
	 * @param pIsMage {code true} if the class is mage class.
	 * @param pIsSummoner {code true} if the class is summoner class.
	 * @param pRace the race related to the class.
	 * @param pParent the parent class Id.
	 */
	private ClassId(int pId, boolean pIsMage, boolean pIsSummoner, PcRace pRace, ClassId pParent)
	{
		_id = pId;
		_isMage = pIsMage;
		_isSummoner = pIsSummoner;
		_race = pRace;
		_parent = pParent;
	}
	
	/**
	 * Gets the ID of the class.
	 * @return the ID of the class
	 */
	@Override
	public final int getId()
	{
		return _id;
	}
	
	/**
	 * @return {code true} if the class is a mage class.
	 */
	public final boolean isMage()
	{
		return _isMage;
	}
	
	/**
	 * @return {code true} if the class is a summoner class.
	 */
	public final boolean isSummoner()
	{
		return _isSummoner;
	}
	
	/**
	 * @return the Race object of the class.
	 */
	public final PcRace getRace()
	{
		return _race;
	}
	
	/**
	 * @param cid the parent ClassId to check.
	 * @return {code true} if this Class is a child of the selected ClassId.
	 */
	public final boolean childOf(ClassId cid)
	{
		if (_parent == null)
		{
			return false;
		}
		
		if (_parent == cid)
		{
			return true;
		}
		
		return _parent.childOf(cid);
		
	}
	
	/**
	 * @param cid the parent ClassId to check.
	 * @return {code true} if this Class is equal to the selected ClassId or a child of the selected ClassId.
	 */
	public final boolean equalsOrChildOf(ClassId cid)
	{
		return (this == cid) || childOf(cid);
	}
	
	/**
	 * @return the child level of this Class (0=root, 1=child leve 1...)
	 */
	public final int level()
	{
		if (_parent == null)
		{
			return 0;
		}
		
		return 1 + _parent.level();
	}
	
	/**
	 * @return its parent Class Id
	 */
	public final ClassId getParent()
	{
		return _parent;
	}
	
	public static ClassId getClassId(int cId)
	{
		try
		{
			return ClassId.values()[cId];
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
