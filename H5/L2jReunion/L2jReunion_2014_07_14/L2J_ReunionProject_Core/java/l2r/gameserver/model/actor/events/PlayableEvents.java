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
package l2r.gameserver.model.actor.events;

import java.util.logging.Level;

import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.events.annotations.PlayerOnly;
import l2r.gameserver.model.actor.events.listeners.IExperienceReceivedEventListener;
import l2r.gameserver.model.actor.events.listeners.ILevelChangeEventListener;

/**
 * @author UnAfraid
 */
public class PlayableEvents extends CharEvents
{
	public PlayableEvents(L2Playable activeChar)
	{
		super(activeChar);
	}
	
	/**
	 * Fired whenever current char receives any exp.<br>
	 * Supported annotations:<br>
	 * <ul>
	 * <li>{@link PlayerOnly}</li>
	 * </ul>
	 * @param exp
	 * @return {@code true} if experience can be received, {@code false} otherwise.
	 */
	public boolean onExperienceReceived(long exp)
	{
		if (hasListeners())
		{
			for (IExperienceReceivedEventListener listener : getEventListeners(IExperienceReceivedEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !getActingPlayer().isPlayer())
					{
						continue;
					}
					
					if (!listener.onExperienceReceived(getActingPlayer(), exp))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * Fired whenever current playable's level has change.<br>
	 * Supported annotations:<br>
	 * <ul>
	 * <li>{@link PlayerOnly}</li>
	 * </ul>
	 * @param levels
	 * @return {@code true} if level change is possible, {@code false} otherwise.
	 */
	public boolean onLevelChange(byte levels)
	{
		if (hasListeners())
		{
			for (ILevelChangeEventListener listener : getEventListeners(ILevelChangeEventListener.class))
			{
				try
				{
					if (listener.getClass().isAnnotationPresent(PlayerOnly.class) && !getActingPlayer().isPlayer())
					{
						continue;
					}
					
					if (!listener.onLevelChange(getActingPlayer(), levels))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	@Override
	public L2Playable getActingPlayer()
	{
		return (L2Playable) super.getActingPlayer();
	}
}
