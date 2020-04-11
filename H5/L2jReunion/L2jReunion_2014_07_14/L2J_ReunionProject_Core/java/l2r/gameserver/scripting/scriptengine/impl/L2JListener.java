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
package l2r.gameserver.scripting.scriptengine.impl;

import l2r.gameserver.model.actor.instance.L2PcInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience superclass for all the L2J listeners.<br>
 * Forces having the register() and unregister() methods in all its children.<br>
 * The register() method is called automatically when the listener is instanciated.<br>
 * These are 2 convenience methods to avoid having to register the listeners ourselves. This is particularly useful for our less advanced coders.
 * @author TheOne
 */
public abstract class L2JListener
{
	public static Logger log = LoggerFactory.getLogger(L2JListener.class);
	
	private L2PcInstance _player = null;
	
	public L2JListener()
	{
	}
	
	public L2JListener(L2PcInstance player)
	{
		_player = player;
	}
	
	/**
	 * Convenience method to add this listener in its proper place.<br>
	 * Called automatically by the superconstructor when the class is instanciated.
	 */
	public abstract void register();
	
	/**
	 * Convenience method to remove this listener
	 */
	public abstract void unregister();
	
	/**
	 * Returns the player attached to this listener
	 * @return
	 */
	public L2PcInstance getPlayer()
	{
		return _player;
	}
}
