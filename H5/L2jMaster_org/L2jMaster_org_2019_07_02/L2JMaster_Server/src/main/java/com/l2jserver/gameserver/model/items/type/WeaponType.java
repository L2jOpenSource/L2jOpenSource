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
package com.l2jserver.gameserver.model.items.type;

import com.l2jserver.gameserver.model.stats.TraitType;

/**
 * Weapon Type enumerated.
 * @author mkizub
 */
public enum WeaponType implements ItemType
{
	SWORD(TraitType.SWORD),
	BLUNT(TraitType.BLUNT),
	DAGGER(TraitType.DAGGER),
	BOW(TraitType.BOW),
	POLE(TraitType.POLE),
	NONE(TraitType.NONE),
	DUAL(TraitType.DUAL),
	ETC(TraitType.ETC),
	FIST(TraitType.FIST),
	DUALFIST(TraitType.DUALFIST),
	FISHINGROD(TraitType.NONE),
	RAPIER(TraitType.RAPIER),
	ANCIENTSWORD(TraitType.ANCIENTSWORD),
	CROSSBOW(TraitType.CROSSBOW),
	FLAG(TraitType.NONE),
	OWNTHING(TraitType.NONE),
	DUALDAGGER(TraitType.DUALDAGGER);
	
	private final int _mask;
	private final TraitType _traitType;
	
	/**
	 * Constructor of the WeaponType.
	 * @param traitType
	 */
	private WeaponType(TraitType traitType)
	{
		_mask = 1 << ordinal();
		_traitType = traitType;
	}
	
	/**
	 * @return the ID of the item after applying the mask.
	 */
	@Override
	public int mask()
	{
		return _mask;
	}
	
	/**
	 * @return TraitType the type of the WeaponType
	 */
	public TraitType getTraitType()
	{
		return _traitType;
	}
}