package com.l2jfrozen.gameserver.model;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author programmos, scoria dev
 */

public class CombatFlag
{
	// private static final Logger LOGGER = Logger.getLogger(CombatFlag.class);
	
	protected L2PcInstance playerFlag = null;
	public int playerId = 0;
	private L2ItemInstance combatItem = null;
	
	private final Location location;
	public L2ItemInstance itemInstance;
	
	private final int itemId;
	
	// private int heading;
	// private int fortId;
	
	// =========================================================
	// Constructor
	public CombatFlag(/* int fort_id, */final int x, final int y, final int z, final int heading, final int item_id)
	{
		// fortId = fort_id;
		location = new Location(x, y, z, heading);
		// heading = heading;
		itemId = item_id;
	}
	
	public synchronized void spawnMe()
	{
		L2ItemInstance i;
		
		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		i = ItemTable.getInstance().createItem("Combat", itemId, 1, null, null);
		i.spawnMe(location.getX(), location.getY(), location.getZ());
		itemInstance = i;
		i = null;
	}
	
	public synchronized void unSpawnMe()
	{
		if (playerFlag != null)
		{
			dropIt();
		}
		
		if (itemInstance != null)
		{
			itemInstance.decayMe();
		}
	}
	
	public void activate(final L2PcInstance player, final L2ItemInstance item)
	{
		// if the player is mounted, attempt to unmount first. Only allow picking up
		// the comabt flag if unmounting is successful.
		if (player.isMounted())
		{
			// TODO: dismount
			if (!player.dismount())
			{
				// TODO: correct this custom message.
				player.sendMessage("You may not pick up this item while riding in this territory");
				return;
			}
		}
		
		// Player holding it data
		playerFlag = player;
		playerId = playerFlag.getObjectId();
		itemInstance = null;
		
		// Add skill
		giveSkill();
		
		// Equip with the weapon
		combatItem = item;
		playerFlag.getInventory().equipItemAndRecord(combatItem);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(combatItem.getItemId());
		playerFlag.sendPacket(sm);
		sm = null;
		
		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(combatItem);
			playerFlag.sendPacket(iu);
			iu = null;
		}
		else
		{
			playerFlag.sendPacket(new ItemList(playerFlag, false));
		}
		
		// Refresh player stats
		playerFlag.broadcastUserInfo();
		// player.setCombatFlagEquipped(true);
		
	}
	
	public void dropIt()
	{
		// Reset player stats
		// player.setCombatFlagEquipped(false);
		removeSkill();
		playerFlag.destroyItem("DieDrop", combatItem, null, false);
		combatItem = null;
		playerFlag.broadcastUserInfo();
		playerFlag = null;
		playerId = 0;
	}
	
	public void giveSkill()
	{
		playerFlag.addSkill(SkillTable.getInstance().getInfo(3318, 1), false);
		playerFlag.addSkill(SkillTable.getInstance().getInfo(3358, 1), false);
		playerFlag.sendSkillList();
	}
	
	public void removeSkill()
	{
		playerFlag.removeSkill(SkillTable.getInstance().getInfo(3318, 1), false);
		playerFlag.removeSkill(SkillTable.getInstance().getInfo(3358, 1), false);
		playerFlag.sendSkillList();
	}
	
}
