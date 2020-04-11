package com.l2jfrozen.gameserver.network.clientpackets;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2EtcItemType;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestDropItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestDropItem.class);
	
	private int objectId;
	private int count;
	private int x;
	private int y;
	private int z;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
		count = readD();
		x = readD();
		y = readD();
		z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || activeChar.isDead())
		{
			return;
		}
		
		if (activeChar.isGM() && activeChar.getAccessLevel().getLevel() > 2)
		{ // just head gm and admin can drop items on the ground
			sendPacket(SystemMessage.sendString("You have not right to discard anything from inventory"));
			return;
		}
		
		// Fix against safe enchant exploit
		if (activeChar.getActiveEnchantItem() != null)
		{
			sendPacket(SystemMessage.sendString("You can't discard items during enchant."));
			return;
		}
		
		// Flood protect drop to avoid packet lag
		if (!getClient().getFloodProtectors().getDropItem().tryPerformAction("drop item"))
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
		
		if (item == null || count == 0 || !activeChar.validateItemManipulation(objectId, "drop"))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if ((!Config.ALLOW_DISCARDITEM && !activeChar.isGM()) || (!item.isDropable()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if (item.isAugmented())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED));
			return;
		}
		
		if (item.getItemType() == L2EtcItemType.QUEST && !(activeChar.isGM()))
		{
			return;
		}
		
		// Drop item disabled by config
		if (activeChar.isGM() && !activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Drop item disabled for GM by config!");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final int itemId = item.getItemId();
		
		// Cursed Weapons cannot be dropped
		if (CursedWeaponsManager.getInstance().isCursed(itemId))
		{
			return;
		}
		
		if (count > item.getCount())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if (Config.PLAYER_SPAWN_PROTECTION > 0 && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
			return;
		}
		
		if (count <= 0)
		{
			activeChar.setAccessLevel(-1); // ban
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count <= 0! ban! oid: " + objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (!item.isStackable() && count > 1)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] count > 1 but item is not stackable! ban! oid: " + objectId + " owner: " + activeChar.getName(), IllegalPlayerAction.PUNISH_KICK);
			return;
		}
		
		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Unsufficient privileges.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isProcessingTransaction() || activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			return;
		}
		
		if (activeChar.isFishing())
		{
			// You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_2));
			return;
		}
		
		// Cannot discard item that the skill is consumming
		if (activeChar.isCastingNow())
		{
			if (activeChar.getCurrentSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_THIS_ITEM));
				return;
			}
		}
		
		if (L2Item.TYPE2_QUEST == item.getItem().getType2() && !activeChar.isGM())
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(activeChar.getObjectId() + ":player tried to drop quest item");
			}
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM));
			return;
		}
		
		if (!activeChar.isInsideRadius(x, y, 150, false) || Math.abs(z - activeChar.getZ()) > 50)
		{
			if (Config.DEBUG)
			{
				LOGGER.debug(activeChar.getObjectId() + ": trying to drop too far away");
			}
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR));
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug("requested drop item " + objectId + "(" + item.getCount() + ") at " + x + "/" + y + "/" + z);
		}
		
		if (item.isEquipped())
		{
			// Remove augementation boni on unequip
			if (item.isAugmented())
			{
				item.getAugmentation().removeBoni(activeChar);
			}
			
			final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			final InventoryUpdate iu = new InventoryUpdate();
			
			for (final L2ItemInstance element : unequiped)
			{
				activeChar.checkSSMatch(null, element);
				
				iu.addModifiedItem(element);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
			
			final ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}
		
		final L2ItemInstance dropedItem = activeChar.dropItem("Drop", objectId, count, x, y, z, null, false, false);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("dropping " + objectId + " item(" + count + ") at: " + x + " " + y + " " + z);
		}
		
		if (dropedItem != null && dropedItem.getItemId() == 57 && dropedItem.getCount() >= 1000000 && Config.RATE_DROP_ADENA <= 200)
		{
			String msg = "Character (" + activeChar.getName() + ") has dropped (" + NumberFormat.getInstance(Locale.ENGLISH).format(dropedItem.getCount()) + ")adena at " + x + "," + y + "," + z + " coordinates";
			LOGGER.warn(msg);
			GmListTable.broadcastMessageToGMs(msg);
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[C] 12 RequestDropItem";
	}
}
