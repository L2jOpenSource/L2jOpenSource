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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2r.gameserver.model.actor.events.listeners.IEventListener;

/**
 * @author UnAfraid
 */
public abstract class AbstractCharEvents
{
	private static volatile List<IEventListener> _staticListeners = null;
	private volatile List<IEventListener> _listeners = null;
	
	public static void registerStaticListener(IEventListener listener)
	{
		if (_staticListeners == null)
		{
			synchronized (AbstractCharEvents.class)
			{
				if (_staticListeners == null)
				{
					_staticListeners = new CopyOnWriteArrayList<>();
				}
			}
		}
		
		_staticListeners.add(listener);
	}
	
	public final void registerListener(IEventListener listener)
	{
		if (_listeners == null)
		{
			synchronized (this)
			{
				if (_listeners == null)
				{
					_listeners = new CopyOnWriteArrayList<>();
				}
			}
		}
		
		_listeners.add(listener);
	}
	
	public static void unregisterStaticListener(IEventListener listener)
	{
		if (_staticListeners == null)
		{
			return;
		}
		
		if (_staticListeners.contains(listener))
		{
			_staticListeners.remove(listener);
		}
		
		if (_staticListeners.isEmpty())
		{
			synchronized (AbstractCharEvents.class)
			{
				if (_staticListeners.isEmpty())
				{
					_staticListeners = null;
				}
			}
		}
	}
	
	public final void unregisterListener(IEventListener listener)
	{
		if (_listeners == null)
		{
			return;
		}
		
		if (_listeners.contains(listener))
		{
			_listeners.remove(listener);
		}
		
		if (_listeners.isEmpty())
		{
			synchronized (this)
			{
				if (_listeners.isEmpty())
				{
					_listeners = null;
				}
			}
		}
	}
	
	protected static boolean hasStaticEventListeners()
	{
		return _staticListeners != null;
	}
	
	protected final boolean hasEventListeners()
	{
		return _listeners != null;
	}
	
	protected final boolean hasListeners()
	{
		return (_listeners != null) || (_staticListeners != null);
	}
	
	protected final <T> List<T> getEventListeners(Class<T> clazz)
	{
		if (!hasListeners())
		{
			return Collections.<T> emptyList();
		}
		
		final List<T> listeners = new ArrayList<>();
		if (hasEventListeners())
		{
			for (IEventListener listener : _listeners)
			{
				if (clazz.isInstance(listener))
				{
					listeners.add(clazz.cast(listener));
				}
			}
		}
		
		if (hasStaticEventListeners())
		{
			for (IEventListener listener : _staticListeners)
			{
				if (clazz.isInstance(listener))
				{
					listeners.add(clazz.cast(listener));
				}
			}
		}
		return listeners;
	}
}
