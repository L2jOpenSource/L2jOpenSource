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
package l2r.gameserver.network.clientpackets;

import l2r.Config;
import l2r.gameserver.datatables.xml.AdminData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.items.type.EtcItemType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.util.GMAudit;
import l2r.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.11.2.1.2.7 $ $Date: 2005/04/02 21:25:21 $
 */
public final class RequestDropItem extends L2GameClientPacket
{
	private static final String _C__17_REQUESTDROPITEM = "[C] 17 RequestDropItem";
	
	private int _objectId;
	private long _count;
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if ((activeChar == null) || activeChar.isDead())
		{
			return;
		}
		// Flood protect drop to avoid packet lag
		if (!getClient().getFloodProtectors().getDropItem().tryPerformAction("drop item"))
		{
			return;
		}
		
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		
		if ((item == null) || (_count == 0) || !activeChar.validateItemManipulation(_objectId, "drop") || (!Config.ALLOW_DISCARDITEM && !activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS)) || (!item.isDropable() && !(activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS) && Config.GM_TRADE_RESTRICTED_ITEMS)) || ((item.getItemType() == EtcItemType.PET_COLLAR) && activeChar.havePetInvItems()) || activeChar.isInsideZone(ZoneIdType.NO_ITEM_DROP))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		if (item.isQuestItem() && !(activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS) && Config.GM_TRADE_RESTRICTED_ITEMS))
		{
			return;
		}
		
		if (_count > item.getCount())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if ((Config.PLAYER_SPAWN_PROTECTION > 0) && activeChar.isInvul() && !activeChar.isGM())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
			return;
		}
		
		if (_count < 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to drop item with oid " + _objectId + " but has count < 0!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!item.isStackable() && (_count > 1))
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestDropItem] Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tried to drop non-stackable item with oid " + _objectId + " but has count > 1!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.JAIL_DISABLE_TRANSACTION && activeChar.isJailed())
		{
			activeChar.sendMessage("You cannot drop items in Jail.");
			return;
		}
		
		if (!activeChar.getAccessLevel().allowTransaction())
		{
			activeChar.sendMessage("Transactions are disabled for your Access Level.");
			activeChar.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}
		
		if (activeChar.isProcessingTransaction() || (activeChar.getPrivateStoreType() != PrivateStoreType.NONE))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}
		if (activeChar.isFishing())
		{
			// You can't mount, dismount, break and drop items while fishing
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
			return;
		}
		if (activeChar.isFlying())
		{
			return;
		}
		
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingNow())
		{
			if ((activeChar.getCurrentSkill() != null) && (activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getId()))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}
		
		// Cannot discard item that the skill is consuming
		if (activeChar.isCastingSimultaneouslyNow())
		{
			if ((activeChar.getLastSimultaneousSkillCast() != null) && (activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getId()))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
				return;
			}
		}
		
		if ((L2Item.TYPE2_QUEST == item.getItem().getType2()) && !activeChar.canOverrideCond(PcCondOverride.DROP_ALL_ITEMS))
		{
			if (Config.DEBUG)
			{
				_log.info(activeChar.getObjectId() + ":player tried to drop quest item");
			}
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
			return;
		}
		
		if (!activeChar.isInsideRadius(_x, _y, 0, 150, false, false) || (Math.abs(_z - activeChar.getZ()) > 50))
		{
			if (Config.DEBUG)
			{
				_log.info(activeChar.getObjectId() + ": trying to drop too far away");
			}
			activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
			return;
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info("requested drop item " + _objectId + "(" + item.getCount() + ") at " + _x + "/" + _y + "/" + _z);
		}
		
		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped)
			{
				itm.unChargeAllShots();
				iu.addModifiedItem(itm);
			}
			activeChar.sendPacket(iu);
			activeChar.broadcastUserInfo();
			
			ItemList il = new ItemList(activeChar, true);
			activeChar.sendPacket(il);
		}
		
		L2ItemInstance dropedItem = activeChar.dropItem("Drop", _objectId, _count, _x, _y, _z, null, false, false);
		
		if (Config.DEBUG)
		{
			_log.info("dropping " + _objectId + " item(" + _count + ") at: " + _x + " " + _y + " " + _z);
		}
		
		// activeChar.broadcastUserInfo();
		
		if (activeChar.isGM())
		{
			String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
			GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", "Drop", target, "(id: " + dropedItem.getId() + " name: " + dropedItem.getItemName() + " objId: " + dropedItem.getObjectId() + " x: " + activeChar.getX() + " y: " + activeChar.getY() + " z: " + activeChar.getZ() + ")");
		}
		
		if ((dropedItem != null) && (dropedItem.getId() == Inventory.ADENA_ID) && (dropedItem.getCount() >= 1000000))
		{
			String msg = "Character (" + activeChar.getName() + ") has dropped (" + dropedItem.getCount() + ")adena at (" + _x + "," + _y + "," + _z + ")";
			if (Config.DEBUG)
			{
				_log.warn(msg);
			}
			AdminData.getInstance().broadcastMessageToGMs(msg);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__17_REQUESTDROPITEM;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
