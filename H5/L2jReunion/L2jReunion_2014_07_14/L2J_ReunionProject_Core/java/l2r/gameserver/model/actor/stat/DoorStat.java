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
package l2r.gameserver.model.actor.stat;

import l2r.gameserver.model.actor.instance.L2DoorInstance;

/**
 * @author malyelfik
 */
public class DoorStat extends CharStat
{
	private int _upgradeHpRatio = 1;
	
	public DoorStat(L2DoorInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2DoorInstance getActiveChar()
	{
		return (L2DoorInstance) super.getActiveChar();
	}
	
	@Override
	public int getMaxHp()
	{
		return super.getMaxHp() * _upgradeHpRatio;
	}
	
	public void setUpgradeHpRatio(int ratio)
	{
		_upgradeHpRatio = ratio;
	}
	
	public int getUpgradeHpRatio()
	{
		return _upgradeHpRatio;
	}
}
