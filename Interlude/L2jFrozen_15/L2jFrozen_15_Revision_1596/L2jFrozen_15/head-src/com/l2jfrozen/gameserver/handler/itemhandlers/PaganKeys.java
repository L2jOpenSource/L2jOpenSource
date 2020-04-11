package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author chris
 */
public class PaganKeys implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		8273,
		8274,
		8275
	};
	public static final int INTERACTION_DISTANCE = 100;
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		
		final int itemId = item.getItemId();
		
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		L2Object target = activeChar.getTarget();
		
		if (!(target instanceof L2DoorInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		final L2DoorInstance door = (L2DoorInstance) target;
		
		target = null;
		
		if (!activeChar.isInsideRadius(door, INTERACTION_DISTANCE, false, false))
		{
			activeChar.sendMessage("Too far.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getAbnormalEffect() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You cannot use the key now.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int openChance = 35;
		
		if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false))
		{
			return;
		}
		
		switch (itemId)
		{
			case 8273: // AnteroomKey
				if (door.getDoorName().startsWith("Anteroom"))
				{
					if (openChance > 0 && Rnd.get(100) < openChance)
					{
						activeChar.sendMessage("You opened Anterooms Door.");
						door.openMe();
						door.onOpen(); // Closes the door after 60sec
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					}
					else
					{
						// test with: activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_UNLOCK_DOOR));
						activeChar.sendMessage("You failed to open Anterooms Door.");
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
						final PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 8274: // Chapelkey, Capel Door has a Gatekeeper?? I use this key for Altar Entrance and Chapel_Door
				if (door.getDoorName().startsWith("Altar_Entrance") || door.getDoorName().startsWith("Chapel_Door"))
				{
					if (openChance > 0 && Rnd.get(100) < openChance)
					{
						activeChar.sendMessage("You opened Altar Entrance.");
						door.openMe();
						door.onOpen();
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					}
					else
					{
						activeChar.sendMessage("You failed to open Altar Entrance.");
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
						final PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
			case 8275: // Key of Darkness
				if (door.getDoorName().startsWith("Door_of_Darkness"))
				{
					if (openChance > 0 && Rnd.get(100) < openChance)
					{
						activeChar.sendMessage("You opened Door of Darkness.");
						door.openMe();
						door.onOpen();
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
					}
					else
					{
						activeChar.sendMessage("You failed to open Door of Darkness.");
						activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
						final PlaySound playSound = new PlaySound("interfacesound.system_close_01");
						activeChar.sendPacket(playSound);
					}
				}
				else
				{
					activeChar.sendMessage("Incorrect Door.");
				}
				break;
		}
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}