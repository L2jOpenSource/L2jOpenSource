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
package l2r.gameserver.enums;

import l2r.gameserver.model.Elementals;

public enum ElementalItems
{
	fireStone(Elementals.FIRE, 9546, ElementalItemType.Stone),
	waterStone(Elementals.WATER, 9547, ElementalItemType.Stone),
	windStone(Elementals.WIND, 9549, ElementalItemType.Stone),
	earthStone(Elementals.EARTH, 9548, ElementalItemType.Stone),
	divineStone(Elementals.HOLY, 9551, ElementalItemType.Stone),
	darkStone(Elementals.DARK, 9550, ElementalItemType.Stone),
	
	fireRoughtore(Elementals.FIRE, 10521, ElementalItemType.Roughore),
	waterRoughtore(Elementals.WATER, 10522, ElementalItemType.Roughore),
	windRoughtore(Elementals.WIND, 10524, ElementalItemType.Roughore),
	earthRoughtore(Elementals.EARTH, 10523, ElementalItemType.Roughore),
	divineRoughtore(Elementals.HOLY, 10526, ElementalItemType.Roughore),
	darkRoughtore(Elementals.DARK, 10525, ElementalItemType.Roughore),
	
	fireCrystal(Elementals.FIRE, 9552, ElementalItemType.Crystal),
	waterCrystal(Elementals.WATER, 9553, ElementalItemType.Crystal),
	windCrystal(Elementals.WIND, 9555, ElementalItemType.Crystal),
	earthCrystal(Elementals.EARTH, 9554, ElementalItemType.Crystal),
	divineCrystal(Elementals.HOLY, 9557, ElementalItemType.Crystal),
	darkCrystal(Elementals.DARK, 9556, ElementalItemType.Crystal),
	
	fireJewel(Elementals.FIRE, 9558, ElementalItemType.Jewel),
	waterJewel(Elementals.WATER, 9559, ElementalItemType.Jewel),
	windJewel(Elementals.WIND, 9561, ElementalItemType.Jewel),
	earthJewel(Elementals.EARTH, 9560, ElementalItemType.Jewel),
	divineJewel(Elementals.HOLY, 9563, ElementalItemType.Jewel),
	darkJewel(Elementals.DARK, 9562, ElementalItemType.Jewel),
	
	// not yet supported by client (Freya pts)
	fireEnergy(Elementals.FIRE, 9564, ElementalItemType.Energy),
	waterEnergy(Elementals.WATER, 9565, ElementalItemType.Energy),
	windEnergy(Elementals.WIND, 9567, ElementalItemType.Energy),
	earthEnergy(Elementals.EARTH, 9566, ElementalItemType.Energy),
	divineEnergy(Elementals.HOLY, 9569, ElementalItemType.Energy),
	darkEnergy(Elementals.DARK, 9568, ElementalItemType.Energy);
	
	public byte _element;
	public int _itemId;
	public ElementalItemType _type;
	
	private ElementalItems(byte element, int itemId, ElementalItemType type)
	{
		_element = element;
		_itemId = itemId;
		_type = type;
	}
}
