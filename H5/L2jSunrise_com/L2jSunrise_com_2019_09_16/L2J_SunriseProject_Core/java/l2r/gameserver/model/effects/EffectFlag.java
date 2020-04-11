/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.effects;

/**
 * @author UnAfraid
 */
public enum EffectFlag
{
	NONE,
	BLOCK_BUFF,
	BLOCK_DEBUFF,
	BLOCK_HP,
	BLOCK_MP,
	RESURRECTION_SPECIAL,
	NOBLESS_BLESSING,
	SILENT_MOVE,
	PROTECTION_BLESSING,
	RELAXING,
	FEAR,
	CONFUSED,
	MUTED,
	PSYCHICAL_MUTED,
	PSYCHICAL_ATTACK_MUTED,
	DISARMED,
	ROOTED,
	SLEEP,
	STUNNED,
	BETRAYED,
	INVUL,
	PARALYZED,
	SINGLE_TARGET,
	BLOCK_RESURRECTION,
	SERVITOR_SHARE;
	
	public int getMask()
	{
		return 1 << ordinal();
	}
}
