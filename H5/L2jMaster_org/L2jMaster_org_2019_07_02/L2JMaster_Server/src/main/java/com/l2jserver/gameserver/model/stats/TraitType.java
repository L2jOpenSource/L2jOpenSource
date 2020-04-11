/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.stats;

/**
 * @author UnAfraid, NosBit
 */
public enum TraitType
{
	NONE(0, 0),
	SWORD(1, 1),
	BLUNT(2, 1),
	DAGGER(3, 1),
	POLE(4, 1),
	FIST(5, 1),
	BOW(6, 1),
	ETC(7, 1),
	UNK_8(8, 0),
	POISON(9, 3),
	HOLD(10, 3),
	BLEED(11, 3),
	SLEEP(12, 3),
	SHOCK(13, 3),
	DERANGEMENT(14, 3),
	BUG_WEAKNESS(15, 2),
	ANIMAL_WEAKNESS(16, 2),
	PLANT_WEAKNESS(17, 2),
	BEAST_WEAKNESS(18, 2),
	DRAGON_WEAKNESS(19, 2),
	PARALYZE(20, 3),
	DUAL(21, 1),
	DUALFIST(22, 1),
	BOSS(23, 3),
	GIANT_WEAKNESS(24, 2),
	CONSTRUCT_WEAKNESS(25, 2),
	DEATH(26, 3),
	VALAKAS(27, 2),
	ANESTHESIA(28, 2),
	CRITICAL_POISON(29, 3),
	ROOT_PHYSICALLY(30, 3),
	ROOT_MAGICALLY(31, 3),
	RAPIER(32, 1),
	CROSSBOW(33, 1),
	ANCIENTSWORD(34, 1),
	TURN_STONE(35, 3),
	GUST(36, 3),
	PHYSICAL_BLOCKADE(37, 3),
	TARGET(38, 3),
	PHYSICAL_WEAKNESS(39, 3),
	MAGICAL_WEAKNESS(40, 3),
	DUALDAGGER(41, 1),
	DEMONIC_WEAKNESS(42, 2), // CT26_P4
	DIVINE_WEAKNESS(43, 2),
	ELEMENTAL_WEAKNESS(44, 2),
	FAIRY_WEAKNESS(45, 2),
	HUMAN_WEAKNESS(46, 2),
	HUMANOID_WEAKNESS(47, 2),
	UNDEAD_WEAKNESS(48, 2);
	
	private final int _id;
	private final int _type; // 1 = weapon, 2 = weakness, 3 = resistance
	
	TraitType(int id, int type)
	{
		_id = id;
		_type = type;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getType()
	{
		return _type;
	}
}