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
package l2r.gameserver.scripting.scriptengine.listeners.character;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.events.AbstractCharEvents;
import l2r.gameserver.model.actor.events.listeners.ISkillUseEventListener;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * @author TheOne
 */
public abstract class SkillUseListener extends L2JListener implements ISkillUseEventListener
{
	private L2Character _character = null;
	
	/**
	 * constructor L2Character specific, will only be fired when this L2Character uses the specified skill Use skillId = -1 to be notified of all skills used
	 * @param character
	 */
	public SkillUseListener(L2Character character)
	{
		_character = character;
		register();
	}
	
	@Override
	public void register()
	{
		if (_character == null)
		{
			AbstractCharEvents.registerStaticListener(this);
		}
		else
		{
			_character.getEvents().registerListener(this);
		}
	}
	
	@Override
	public void unregister()
	{
		if (_character == null)
		{
			AbstractCharEvents.unregisterStaticListener(this);
		}
		else
		{
			_character.getEvents().unregisterListener(this);
		}
	}
	
	/**
	 * Returns the L2Character this listener is attached to
	 * @return
	 */
	public L2Character getCharacter()
	{
		return _character;
	}
}
