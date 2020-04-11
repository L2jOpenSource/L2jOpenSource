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
package l2r.gameserver.scripting.scriptengine.listeners.player;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripting.scriptengine.events.EquipmentEvent;
import l2r.gameserver.scripting.scriptengine.impl.L2JListener;

/**
 * Equip and unequip listener.<br>
 * This listener can be either global or player-based<br>
 * Use the boolean in the constructor!
 * @author TheOne
 */
public abstract class EquipmentListener extends L2JListener
{
	/**
	 * Constructor To set a global listener, set the L2PcInstance value to null
	 * @param activeChar
	 */
	public EquipmentListener(L2PcInstance activeChar)
	{
		super(activeChar);
		register();
	}
	
	/**
	 * The item has just been equipped or unequipped
	 * @param event
	 * @return
	 */
	public abstract boolean onEquip(EquipmentEvent event);
	
	@Override
	public void register()
	{
		if (getPlayer() == null)
		{
			L2PcInstance.addGlobalEquipmentListener(this);
		}
		else
		{
			getPlayer().addEquipmentListener(this);
		}
	}
	
	@Override
	public void unregister()
	{
		if (getPlayer() == null)
		{
			L2PcInstance.removeGlobalEquipmentListener(this);
		}
		else
		{
			getPlayer().removeEquipmentListener(this);
		}
	}
	
}
