/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.handlers.itemhandlers;

import l2r.gameserver.datatables.xml.DoorData;
import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;

/**
 * @author chris
 */
public class PaganKeys implements IItemHandler
{
	public static final int INTERACTION_DISTANCE = 100;
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final int itemId = item.getId();
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		L2DoorInstance door = (L2DoorInstance) target;
		
		if (!(activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false)))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		if ((activeChar.getAbnormalEffect() > 0) || activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot use the key now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return false;
		}
		
		// TODO: Unhardcode these!
		switch (itemId)
		{
			case 9698:
				if (door.getId() == 24220020)
				{
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						final L2DoorInstance instanceDoor = InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoor(door.getId());
						if (instanceDoor != null)
						{
							instanceDoor.openMe();
						}
					}
					else
					{
						door.openMe();
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 9699:
				if (door.getId() == 24220022)
				{
					if (activeChar.getInstanceId() != door.getInstanceId())
					{
						final L2DoorInstance instanceDoor = InstanceManager.getInstance().getInstance(activeChar.getInstanceId()).getDoor(door.getId());
						if (instanceDoor != null)
						{
							instanceDoor.openMe();
						}
					}
					else
					{
						door.openMe();
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 8056:
				if ((door.getId() == 23150004) || (door.getId() == 23150003))
				{
					DoorData.getInstance().getDoor(23150003).openMe();
					DoorData.getInstance().getDoor(23150004).openMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
		}
		return true;
	}
}
