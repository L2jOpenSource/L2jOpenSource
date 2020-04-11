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
package com.l2jserver.gameserver.enums;

/**
 * @author Zealar
 */
public enum StatFunction
{
	ADD("Add", 30),
	DIV("Div", 20),
	ENCHANT("Enchant", 0),
	ENCHANTHP("EnchantHp", 40),
	MUL("Mul", 20),
	SET("Set", 0),
	SHARE("Share", 30),
	SUB("Sub", 30);
	
	String name;
	int order;
	
	StatFunction(String name, int order)
	{
		this.name = name;
		this.order = order;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getOrder()
	{
		return order;
	}
}
