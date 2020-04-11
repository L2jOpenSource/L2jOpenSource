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
import l2r.gameserver.model.actor.events.listeners.IDeathEventListener;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Death/Kill listener<br>
 * Works for NPCs and Players
 * @author TheOne
 */
public abstract class DeathListener extends L2JListener implements IDeathEventListener
{
	private L2Character _character = null;
	
	/**
	 * constructor To have a global listener, set character to null
	 * @param character
	 */
	public DeathListener(L2Character character)
	{
		_character = character;
		register();
	}
	
	/**
	 * Returns the character
	 * @return
	 */
	public L2Character getCharacter()
	{
		return _character;
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
}
