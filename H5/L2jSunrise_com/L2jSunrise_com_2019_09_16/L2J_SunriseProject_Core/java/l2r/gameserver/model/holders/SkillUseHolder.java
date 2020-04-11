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
package l2r.gameserver.model.holders;

import l2r.gameserver.model.skills.L2Skill;

/**
 * @author UnAfraid
 */
public class SkillUseHolder extends SkillHolder
{
	private final boolean _ctrlPressed;
	private final boolean _shiftPressed;
	
	public SkillUseHolder(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
	{
		super(skill);
		_ctrlPressed = ctrlPressed;
		_shiftPressed = shiftPressed;
	}
	
	public boolean isCtrlPressed()
	{
		return _ctrlPressed;
	}
	
	public boolean isShiftPressed()
	{
		return _shiftPressed;
	}
}
