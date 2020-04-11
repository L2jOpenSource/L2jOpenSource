package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;

public class RequestUnEquipItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestUnEquipItem.class);
	
	// cd
	private int slot;
	
	/**
	 * packet type id 0x11 format: cd
	 */
	@Override
	protected void readImpl()
	{
		slot = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			LOGGER.debug("request unequip slot " + slot);
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.haveFlagCTF)
		{
			activeChar.sendMessage("You can't unequip a CTF flag.");
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(slot);
		if (item != null && item.isWear())
		{
			// Wear-items are not to be unequipped
			return;
		}
		
		// Prevent of unequiping a cursed weapon
		if (slot == L2Item.SLOT_LR_HAND && activeChar.isCursedWeaponEquiped())
		{
			// Message ?
			return;
		}
		
		// Prevent player from unequipping items in special conditions
		if (activeChar.isStunned() || activeChar.isConfused() || activeChar.isAway() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isAlikeDead())
		{
			activeChar.sendMessage("Your status does not allow you to do that.");
			return;
		}
		
		if (/* activeChar.isAttackingNow() || */activeChar.isCastingNow() || activeChar.isCastingPotionNow())
		{
			return;
		}
		
		if (activeChar.isMoving() && activeChar.isAttackingNow() && (slot == L2Item.SLOT_LR_HAND || slot == L2Item.SLOT_L_HAND || slot == L2Item.SLOT_R_HAND))
		{
			final L2Object target = activeChar.getTarget();
			activeChar.setTarget(null);
			activeChar.stopMove(null);
			activeChar.setTarget(target);
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
		}
		
		// Remove augmentation bonus
		if (item != null && item.isAugmented())
		{
			item.getAugmentation().removeBoni(activeChar);
		}
		
		final L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
		
		// show the update in the inventory
		final InventoryUpdate iu = new InventoryUpdate();
		
		for (final L2ItemInstance element : unequiped)
		{
			activeChar.checkSSMatch(null, element);
			
			iu.addModifiedItem(element);
		}
		
		activeChar.sendPacket(iu);
		
		activeChar.broadcastUserInfo();
		
		// this can be 0 if the user pressed the right mouse button twice very fast
		if (unequiped.length > 0)
		{
			
			SystemMessage sm = null;
			if (unequiped[0].getEnchantLevel() > 0)
			{
				sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0].getItemId());
			}
			else
			{
				sm = new SystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequiped[0].getItemId());
			}
			activeChar.sendPacket(sm);
			sm = null;
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 11 RequestUnequipItem";
	}
}
