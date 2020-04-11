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
package com.l2jserver.gameserver.model;

import com.l2jserver.gameserver.data.xml.impl.AdminData;

/**
 * @author HorridoJoho
 */
public class L2AccessLevel
{
	/** The access level. */
	private int _accessLevel = 0;
	/** The access level name. */
	private String _name = null;
	/** Child access levels. */
	private L2AccessLevel _childsAccessLevel = null;
	/** Child access levels. */
	private int _child = 0;
	/** The name color for the access level. */
	private int _nameColor = 0;
	/** The title color for the access level. */
	private int _titleColor = 0;
	/** Flag to determine if the access level has GM access. */
	private boolean _isGm = false;
	/** Flag for peace zone attack */
	private boolean _allowPeaceAttack = false;
	/** Flag for fixed res */
	private boolean _allowFixedRes = false;
	/** Flag for transactions */
	private boolean _allowTransaction = false;
	/** Flag for AltG commands */
	private boolean _allowAltG = false;
	/** Flag to give damage */
	private boolean _giveDamage = false;
	/** Flag to take aggro */
	private boolean _takeAggro = false;
	/** Flag to gain exp in party */
	private boolean _gainExp = false;
	
	public L2AccessLevel(StatsSet set)
	{
		_accessLevel = set.getInt("level");
		_name = set.getString("name");
		_nameColor = Integer.decode("0x" + set.getString("nameColor", "FFFFFF"));
		_titleColor = Integer.decode("0x" + set.getString("titleColor", "FFFFFF"));
		_child = set.getInt("childAccess", 0);
		_isGm = set.getBoolean("isGM", false);
		_allowPeaceAttack = set.getBoolean("allowPeaceAttack", false);
		_allowFixedRes = set.getBoolean("allowFixedRes", false);
		_allowTransaction = set.getBoolean("allowTransaction", true);
		_allowAltG = set.getBoolean("allowAltg", false);
		_giveDamage = set.getBoolean("giveDamage", true);
		_takeAggro = set.getBoolean("takeAggro", true);
		_gainExp = set.getBoolean("gainExp", true);
	}
	
	public L2AccessLevel()
	{
		_accessLevel = 0;
		_name = "User";
		_nameColor = Integer.decode("0xFFFFFF");
		_titleColor = Integer.decode("0xFFFFFF");
		_child = 0;
		_isGm = false;
		_allowPeaceAttack = false;
		_allowFixedRes = false;
		_allowTransaction = true;
		_allowAltG = false;
		_giveDamage = true;
		_takeAggro = true;
		_gainExp = true;
	}
	
	/**
	 * Gets the access level.
	 * @return the access level
	 */
	public int getLevel()
	{
		return _accessLevel;
	}
	
	/**
	 * Gets the access level name.
	 * @return the access level name
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Gets the name color of the access level.
	 * @return the name color for the access level
	 */
	public int getNameColor()
	{
		return _nameColor;
	}
	
	/**
	 * Gets the title color color of the access level.
	 * @return the title color for the access level
	 */
	public int getTitleColor()
	{
		return _titleColor;
	}
	
	/**
	 * Verifies if the access level has GM access or not.
	 * @return {@code true} if access level have GM access, otherwise {@code false}
	 */
	public boolean isGm()
	{
		return _isGm;
	}
	
	/**
	 * Verifies if the access level is allowed to attack in peace zone or not.
	 * @return {@code true} if the access level is allowed to attack in peace zone, otherwise {@code false}
	 */
	public boolean allowPeaceAttack()
	{
		return _allowPeaceAttack;
	}
	
	/**
	 * Verifies if the access level is allowed to use fixed resurrection or not.
	 * @return {@ode true} if the access level is allowed to use fixed resurrection, otherwise {@code false}
	 */
	public boolean allowFixedRes()
	{
		return _allowFixedRes;
	}
	
	/**
	 * Verifies if the access level is allowed to perform transactions or not.
	 * @return {@ode true} if access level is allowed to perform transactions, otherwise {@code false}
	 */
	public boolean allowTransaction()
	{
		return _allowTransaction;
	}
	
	/**
	 * Verifies if the access level is allowed to use AltG commands or not.
	 * @return {@ode true} if access level is allowed to use AltG commands, otherwise {@code false}
	 */
	public boolean allowAltG()
	{
		return _allowAltG;
	}
	
	/**
	 * Verifies if the access level can give damage or not.
	 * @return {@ode true} if the access level can give damage, otherwise {@code false}
	 */
	public boolean canGiveDamage()
	{
		return _giveDamage;
	}
	
	/**
	 * Verifies if the access level can take aggro or not.
	 * @return {@ode true} if the access level can take aggro, otherwise {@code false}
	 */
	public boolean canTakeAggro()
	{
		return _takeAggro;
	}
	
	/**
	 * Verifies if the access level can gain exp or not.
	 * @return {@ode true} if the access level can gain exp, otherwise {@code false}
	 */
	public boolean canGainExp()
	{
		return _gainExp;
	}
	
	/**
	 * Returns if the access level contains allowedAccess as child.
	 * @param accessLevel the parent access level
	 * @return {@ode true} if a child access level is equals to allowedAccess, otherwise {@code false}
	 */
	public boolean hasChildAccess(L2AccessLevel accessLevel)
	{
		if (_childsAccessLevel == null)
		{
			if (_child <= 0)
			{
				return false;
			}
			
			_childsAccessLevel = AdminData.getInstance().getAccessLevel(_child);
		}
		return ((_childsAccessLevel.getLevel() == accessLevel.getLevel()) || _childsAccessLevel.hasChildAccess(accessLevel));
	}
}