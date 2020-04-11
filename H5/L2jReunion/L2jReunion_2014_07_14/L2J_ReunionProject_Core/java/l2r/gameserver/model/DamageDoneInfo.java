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
package l2r.gameserver.model;

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author xban1x
 */
public final class DamageDoneInfo
{
	private final L2PcInstance _attacker;
	private int _damage = 0;
	
	public DamageDoneInfo(L2PcInstance attacker, int damage)
	{
		_attacker = attacker;
		_damage = damage;
	}
	
	public L2PcInstance getAttacker()
	{
		return _attacker;
	}
	
	public void addDamage(int damage)
	{
		_damage += damage;
	}
	
	public int getDamage()
	{
		return _damage;
	}
	
	@Override
	public final boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj instanceof DamageDoneInfo)
		{
			return (((DamageDoneInfo) obj).getAttacker() == _attacker);
		}
		
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _attacker.getObjectId();
	}
}
