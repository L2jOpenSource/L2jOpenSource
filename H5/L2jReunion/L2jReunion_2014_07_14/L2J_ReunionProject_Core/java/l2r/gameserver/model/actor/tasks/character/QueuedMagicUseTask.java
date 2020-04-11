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
package l2r.gameserver.model.actor.tasks.character;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

/**
 * Task dedicated to qued magic use of character
 * @author xban1x
 */
public final class QueuedMagicUseTask implements Runnable
{
	private final L2PcInstance _currPlayer;
	private final L2Skill _queuedSkill;
	private final boolean _isCtrlPressed;
	private final boolean _isShiftPressed;
	
	public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
	{
		_currPlayer = currPlayer;
		_queuedSkill = queuedSkill;
		_isCtrlPressed = isCtrlPressed;
		_isShiftPressed = isShiftPressed;
	}
	
	@Override
	public void run()
	{
		if (_currPlayer != null)
		{
			_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
		}
	}
}
