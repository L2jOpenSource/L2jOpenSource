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

import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.model.actor.L2Character;

/**
 * Task dedicated to notify character's AI
 * @author xban1x
 */
public final class NotifyAITask implements Runnable
{
	private final L2Character _character;
	private final CtrlEvent _event;
	
	public NotifyAITask(L2Character character, CtrlEvent event)
	{
		_character = character;
		_event = event;
	}
	
	@Override
	public void run()
	{
		if (_character != null)
		{
			_character.getAI().notifyEvent(_event, null);
		}
	}
}
