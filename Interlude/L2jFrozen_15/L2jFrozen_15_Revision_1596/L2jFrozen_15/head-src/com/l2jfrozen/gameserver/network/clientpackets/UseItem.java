package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.ShotsTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.handler.ItemHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.ClanHallManager;
import com.l2jfrozen.gameserver.model.Inventory;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.ShowCalculator;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.gameserver.util.Util;

public final class UseItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(UseItem.class);
	private int objectId;
	
	@Override
	protected void readImpl()
	{
		objectId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
		
		if (item == null)
		{
			return;
		}
		
		// Like L2OFF you can't use soulshots while sitting
		if (activeChar.isSitting() && ShotsTable.getInstance().isShot(item.getItemId()))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_AUTO_USE_LACK_OF_S1);
			sm.addItemName(item.getItemId());
			activeChar.sendPacket(sm);
			return;
		}
		
		// Flood protect UseItem
		if (item.isPotion())
		{
			if (!getClient().getFloodProtectors().getUsePotion().tryPerformAction("use potion"))
			{
				return;
			}
		}
		else
		{
			if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("use item"))
			{
				return;
			}
		}
		if (activeChar.isStunned() || activeChar.isConfused() || activeChar.isAway() || activeChar.isParalyzed() || activeChar.isSleeping())
		{
			activeChar.sendMessage("You Cannot Use Items Right Now.");
			return;
		}
		
		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}
		
		// NOTE: disabled due to deadlocks
		// synchronized (activeChar.getInventory())
		// {
		
		if (item.isWear())
		{
			// No unequipping wear-items
			return;
		}
		
		if (item.getItem().getType2() == L2Item.TYPE2_QUEST)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		final int itemId = item.getItemId();
		/*
		 * Alt game - Karma punishment // SOE 736 Scroll of Escape 1538 Blessed Scroll of Escape 1829 Scroll of Escape: Clan Hall 1830 Scroll of Escape: Castle 3958 L2Day - Blessed Scroll of Escape 5858 Blessed Scroll of Escape: Clan Hall 5859 Blessed Scroll of Escape: Castle 6663 Scroll of Escape: Orc
		 * Village 6664 Scroll of Escape: Silenos Village 7117 Scroll of Escape to Talking Island 7118 Scroll of Escape to Elven Village 7119 Scroll of Escape to Dark Elf Village 7120 Scroll of Escape to Orc Village 7121 Scroll of Escape to Dwarven Village 7122 Scroll of Escape to Gludin Village 7123 Scroll
		 * of Escape to the Town of Gludio 7124 Scroll of Escape to the Town of Dion 7125 Scroll of Escape to Floran 7126 Scroll of Escape to Giran Castle Town 7127 Scroll of Escape to Hardin's Private Academy 7128 Scroll of Escape to Heine 7129 Scroll of Escape to the Town of Oren 7130 Scroll of Escape to
		 * Ivory Tower 7131 Scroll of Escape to Hunters Village 7132 Scroll of Escape to Aden Castle Town 7133 Scroll of Escape to the Town of Goddard 7134 Scroll of Escape to the Rune Township 7135 Scroll of Escape to the Town of Schuttgart. 7554 Scroll of Escape to Talking Island 7555 Scroll of Escape to
		 * Elven Village 7556 Scroll of Escape to Dark Elf Village 7557 Scroll of Escape to Orc Village 7558 Scroll of Escape to Dwarven Village 7559 Scroll of Escape to Giran Castle Town 7618 Scroll of Escape - Ketra Orc Village 7619 Scroll of Escape - Varka Silenos Village 10129 Scroll of Escape :
		 * Fortress 10130 Blessed Scroll of Escape : Fortress
		 */
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0 && (itemId == 736 || itemId == 1538 || itemId == 1829 || itemId == 1830 || itemId == 3958 || itemId == 5858 || itemId == 5859 || itemId == 6663 || itemId == 6664 || itemId >= 7117 && itemId <= 7135 || itemId >= 7554 && itemId <= 7559 || itemId == 7618 || itemId == 7619 || itemId == 10129 || itemId == 10130))
		{
			return;
		}
		
		// Items that cannot be used
		if (itemId == 57)
		{
			return;
		}
		
		if ((itemId == 5858) && (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()) == null))
		{
			activeChar.sendMessage("Blessed Scroll of Escape: Clan Hall cannot be used due to unsuitable terms.");
			return;
		}
		else if ((itemId == 5859) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) == null))
		{
			activeChar.sendMessage("Blessed Scroll of Escape: Castle cannot be used due to unsuitable terms.");
			return;
		}
		
		if (activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			// You cannot do anything else while fishing
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getPkKills() > 0 && (itemId >= 7816 && itemId <= 7831))
		{
			// Retail messages... same L2OFF
			activeChar.sendMessage("You do not meet the required condition to equip that item.");
			activeChar.sendMessage("You are unable to equip this item when your PK count is greater than or equal to one.");
			return;
		}
		
		final L2Clan cl = activeChar.getClan();
		// A shield that can only be used by the members of a clan that owns a castle.
		if ((cl == null || cl.getCastleId() == 0) && itemId == 7015 && Config.CASTLE_SHIELD && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}
		
		// A shield that can only be used by the members of a clan that owns a clan hall.
		if ((cl == null || cl.getHasHideout() == 0) && itemId == 6902 && Config.CLANHALL_SHIELD && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}
		
		// Apella armor used by clan members may be worn by a Baron or a higher level Aristocrat.
		if (itemId >= 7860 && itemId <= 7879 && Config.APELLA_ARMORS && (cl == null || activeChar.getPledgeClass() < 5) && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}
		
		// Clan Oath armor used by all clan members
		if (itemId >= 7850 && itemId <= 7859 && Config.OATH_ARMORS && cl == null && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}
		
		// The Lord's Crown used by castle lords only
		if (itemId == 6841 && Config.CASTLE_CROWN && (cl == null || cl.getCastleId() == 0 || !activeChar.isClanLeader()) && !activeChar.isGM())
		{
			activeChar.sendMessage("You can't equip that");
			return;
		}
		
		// Scroll of resurrection like L2OFF if you are casting you can't use them
		if ((itemId == 737 || itemId == 3936 || itemId == 3959 || itemId == 6387) && activeChar.isCastingNow())
		{
			return;
		}
		
		// Castle circlets used by the members of a clan that owns a castle, academy members are excluded.
		if (Config.CASTLE_CIRCLETS && (itemId >= 6834 && itemId <= 6840 || itemId == 8182 || itemId == 8183))
		{
			if (cl == null)
			{
				activeChar.sendMessage("You can't equip that");
				return;
			}
			
			final int circletId = CastleManager.getInstance().getCircletByCastleId(cl.getCastleId());
			if (activeChar.getPledgeType() == -1 || circletId != itemId)
			{
				activeChar.sendMessage("You can't equip that");
				return;
			}
		}
		
		/*
		 * //You can't equip Shield if you have specific weapon equiped, not retail L2Weapon curwep = activeChar.getActiveWeaponItem(); if(curwep != null) { if(curwep.getItemType() == L2WeaponType.DUAL && item.getItemType() == L2WeaponType.NONE) { activeChar.sendMessage("You are not allowed to do this.");
		 * return; } else if(curwep.getItemType() == L2WeaponType.BOW && item.getItemType() == L2WeaponType.NONE) { activeChar.sendMessage("You are not allowed to do this."); return; } else if(curwep.getItemType() == L2WeaponType.BIGBLUNT && item.getItemType() == L2WeaponType.NONE) {
		 * activeChar.sendMessage("You are not allowed to do this."); return; } else if(curwep.getItemType() == L2WeaponType.BIGSWORD && item.getItemType() == L2WeaponType.NONE) { activeChar.sendMessage("You are not allowed to do this."); return; } else if(curwep.getItemType() == L2WeaponType.POLE &&
		 * item.getItemType() == L2WeaponType.NONE) { activeChar.sendMessage("You are not allowed to do this."); return; } else if(curwep.getItemType() == L2WeaponType.DUALFIST && item.getItemType() == L2WeaponType.NONE) { activeChar.sendMessage("You are not allowed to do this."); return; } }
		 */
		
		// Char cannot use item when dead
		if (activeChar.isDead())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(itemId);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}
		
		// Char cannot use pet items
		if (item.getItem().isForWolf() || item.getItem().isForHatchling() || item.getItem().isForStrider() || item.getItem().isForBabyPet())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM); // You cannot equip a pet item.
			sm.addItemName(itemId);
			getClient().getActiveChar().sendPacket(sm);
			sm = null;
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug(activeChar.getObjectId() + ": use item " + objectId);
		}
		
		if (item.isEquipable())
		{
			// No unequipping/equipping while the player is in special conditions
			if (activeChar.isFishing() || activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAlikeDead())
			{
				activeChar.sendMessage("Your status does not allow you to do that.");
				return;
			}
			
			// SECURE FIX - Anti Overenchant Cheat!!
			if (Config.MAX_ITEM_ENCHANT_KICK > 0 && !activeChar.isGM() && item.getEnchantLevel() > Config.MAX_ITEM_ENCHANT_KICK)
			{
				activeChar.sendMessage("You have been kicked for using an item overenchanted!");
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has item Overenchanted! Kicked ", Config.DEFAULT_PUNISH);
				// activeChar.closeNetConnection();
				return;
			}
			
			int bodyPart = item.getItem().getBodyPart();
			
			// Like L2OFF you can't use equips while you are casting
			if ((activeChar.isCastingNow() || activeChar.isCastingPotionNow() || activeChar.isMounted() || (activeChar.inEventCTF && activeChar.haveFlagCTF)))
			{
				if (activeChar.inEventCTF && activeChar.haveFlagCTF)
				{
					activeChar.sendMessage("This item can not be equipped when you have the flag.");
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEM_WHILE_USING_MAGIC);
					activeChar.sendPacket(sm);
				}
				return;
			}
			
			// Like L2OFF, since c5 you can equip weapon
			// Don't allow weapon/shield equipment if wearing formal wear
			/*
			 * if (activeChar.isWearingFormalWear() && (bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND)) { SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR); activeChar.sendPacket(sm); return; }
			 */
			
			// Over enchant protection
			if (Config.PROTECTED_ENCHANT)
			{
				switch (bodyPart)
				{
					case L2Item.SLOT_LR_HAND:
					case L2Item.SLOT_L_HAND:
					case L2Item.SLOT_R_HAND:
					{
						if ((item.getEnchantLevel() > Config.NORMAL_WEAPON_ENCHANT_LEVEL.size() || item.getEnchantLevel() > Config.BLESS_WEAPON_ENCHANT_LEVEL.size() || item.getEnchantLevel() > Config.CRYSTAL_WEAPON_ENCHANT_LEVEL.size()) && !activeChar.isGM())
						{
							// activeChar.setAccountAccesslevel(-1); //ban
							activeChar.sendMessage("You have been banned for using an item wich is over enchanted!"); // message
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has item Overenchanted! ", Config.DEFAULT_PUNISH);
							// activeChar.closeNetConnection(); //kick
							return;
						}
						break;
					}
					case L2Item.SLOT_CHEST:
					case L2Item.SLOT_BACK:
					case L2Item.SLOT_GLOVES:
					case L2Item.SLOT_FEET:
					case L2Item.SLOT_HEAD:
					case L2Item.SLOT_FULL_ARMOR:
					case L2Item.SLOT_LEGS:
					{
						if ((item.getEnchantLevel() > Config.NORMAL_ARMOR_ENCHANT_LEVEL.size() || item.getEnchantLevel() > Config.BLESS_ARMOR_ENCHANT_LEVEL.size() || item.getEnchantLevel() > Config.CRYSTAL_ARMOR_ENCHANT_LEVEL.size()) && !activeChar.isGM())
						{
							// activeChar.setAccountAccesslevel(-1); //ban
							activeChar.sendMessage("You have been banned for using an item wich is over enchanted!"); // message
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has item Overenchanted! ", Config.DEFAULT_PUNISH);
							// activeChar.closeNetConnection(); //kick
							return;
						}
						break;
					}
					case L2Item.SLOT_R_EAR:
					case L2Item.SLOT_L_EAR:
					case L2Item.SLOT_NECK:
					case L2Item.SLOT_R_FINGER:
					case L2Item.SLOT_L_FINGER:
					{
						if ((item.getEnchantLevel() > Config.NORMAL_JEWELRY_ENCHANT_LEVEL.size() || item.getEnchantLevel() > Config.BLESS_JEWELRY_ENCHANT_LEVEL.size() || item.getEnchantLevel() > Config.CRYSTAL_JEWELRY_ENCHANT_LEVEL.size()) && !activeChar.isGM())
						{
							// activeChar.setAccountAccesslevel(-1); //ban
							activeChar.sendMessage("You have been banned for using an item wich is over enchanted!"); // message
							Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " has item Overenchanted! ", Config.DEFAULT_PUNISH);
							// activeChar.closeNetConnection(); //kick
							return;
						}
						break;
					}
				}
			}
			
			// Don't allow weapon/shield equipment if a cursed weapon is equiped
			if (activeChar.isCursedWeaponEquiped() && (bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND))
			{
				return;
			}
			
			// Don't allow weapon/shield hero equipment during Olimpia
			if (activeChar.isInOlympiadMode() && ((bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND) && (item.getItemId() >= 6611 && item.getItemId() <= 6621 || item.getItemId() == 6842) || Config.LIST_OLY_RESTRICTED_ITEMS.contains(item.getItemId())))
			{
				return;
			}
			
			// Don't allow Hero items equipment if not a hero
			if (!activeChar.isHero() && (item.getItemId() >= 6611 && item.getItemId() <= 6621 || item.getItemId() == 6842) && !activeChar.isGM())
			{
				return;
			}
			
			if (activeChar.isMoving() && activeChar.isAttackingNow() && (bodyPart == L2Item.SLOT_LR_HAND || bodyPart == L2Item.SLOT_L_HAND || bodyPart == L2Item.SLOT_R_HAND))
			{
				final L2Object target = activeChar.getTarget();
				activeChar.setTarget(null);
				activeChar.stopMove(null);
				activeChar.setTarget(target);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK);
			}
			
			// Don't allow to put formal wear
			if (activeChar.isCursedWeaponEquipped() && itemId == 6408)
			{
				return;
			}
			
			// Elrokian Trap like L2OFF, add skills
			if (itemId == 8763)
			{
				if (!item.isEquipped())
				{
					activeChar.addSkill(SkillTable.getInstance().getInfo(3626, 1));
					activeChar.addSkill(SkillTable.getInstance().getInfo(3627, 1));
					activeChar.addSkill(SkillTable.getInstance().getInfo(3628, 1));
					activeChar.sendSkillList();
				}
			}
			
			// Equip or unEquip
			L2ItemInstance[] items = null;
			final boolean isEquiped = item.isEquipped();
			SystemMessage sm = null;
			
			if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
			{ // if used item is a weapon
				
				L2ItemInstance wep = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
				if (wep == null)
				{
					wep = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				}
				
				activeChar.checkSSMatch(item, wep);
				
			}
			
			// Remove the item if it's equiped
			if (isEquiped)
			{
				// Elrokian Trap like L2OFF, remove skills
				if (itemId == 8763)
				{
					activeChar.removeSkill(3626, true);
					activeChar.removeSkill(3627, true);
					activeChar.removeSkill(3628, true);
					activeChar.sendSkillList();
				}
				
				if (item.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(itemId);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(itemId);
				}
				
				activeChar.sendPacket(sm);
				
				// Remove augementation bonus on unequipment
				if (item.isAugmented())
				{
					item.getAugmentation().removeBoni(activeChar);
				}
				
				switch (item.getEquipSlot())
				{
					case 1:
						bodyPart = L2Item.SLOT_L_EAR;
						break;
					case 2:
						bodyPart = L2Item.SLOT_R_EAR;
						break;
					case 4:
						bodyPart = L2Item.SLOT_L_FINGER;
						break;
					case 5:
						bodyPart = L2Item.SLOT_R_FINGER;
						break;
					default:
						break;
				}
				
				// remove cupid's bow skills on unequip
				if (item.isCupidBow())
				{
					if (item.getItemId() == 9140)
					{
						activeChar.removeSkill(SkillTable.getInstance().getInfo(3261, 1));
					}
					else
					{
						activeChar.removeSkill(SkillTable.getInstance().getInfo(3260, 0));
						activeChar.removeSkill(SkillTable.getInstance().getInfo(3262, 0));
					}
				}
				
				items = activeChar.getInventory().unEquipItemInBodySlotAndRecord(bodyPart);
			}
			else
			{
				// Restrict bow weapon for class except Cupid bow.
				if (item.getItem() instanceof L2Weapon && ((L2Weapon) item.getItem()).getItemType() == L2WeaponType.BOW && !item.isCupidBow())
				{
					// Restriction not valid on Olympiad matches
					if (Config.DISABLE_BOW_CLASSES.contains(activeChar.getClassId().getId()) && !activeChar.isInOlympiadMode())
					{
						activeChar.sendMessage("This item can not be equipped by your class!");
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}
				
				final int tempBodyPart = item.getItem().getBodyPart();
				L2ItemInstance tempItem = activeChar.getInventory().getPaperdollItemByL2ItemId(tempBodyPart);
				
				// remove augmentation stats for replaced items
				// currently weapons only..
				if (tempItem != null && tempItem.isAugmented())
				{
					tempItem.getAugmentation().removeBoni(activeChar);
				}
				
				// check if the item replaces a wear-item
				if (tempItem != null && tempItem.isWear())
				{
					// dont allow an item to replace a wear-item
					return;
				}
				else if (tempBodyPart == 0x4000) // left+right hand equipment
				{
					// this may not remove left OR right hand equipment
					tempItem = activeChar.getInventory().getPaperdollItem(7);
					if (tempItem != null && tempItem.isWear())
					{
						return;
					}
					
					tempItem = activeChar.getInventory().getPaperdollItem(8);
					if (tempItem != null && tempItem.isWear())
					{
						return;
					}
				}
				else if (tempBodyPart == 0x8000) // fullbody armor
				{
					// this may not remove chest or leggins
					tempItem = activeChar.getInventory().getPaperdollItem(10);
					if (tempItem != null && tempItem.isWear())
					{
						return;
					}
					
					tempItem = activeChar.getInventory().getPaperdollItem(11);
					if (tempItem != null && tempItem.isWear())
					{
						return;
					}
				}
				
				// Left hand
				tempItem = activeChar.getInventory().getPaperdollItem(7);
				// Elrokian Trap like L2OFF, remove skills
				if (tempItem != null && tempItem.getItemId() == 8763)
				{
					activeChar.removeSkill(3626, true);
					activeChar.removeSkill(3627, true);
					activeChar.removeSkill(3628, true);
					activeChar.sendSkillList();
				}
				
				if (item.getEnchantLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(itemId);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
					sm.addItemName(itemId);
				}
				activeChar.sendPacket(sm);
				
				// Apply augementation boni on equip
				if (item.isAugmented())
				{
					item.getAugmentation().applyBoni(activeChar);
				}
				
				// Apply cupid's bow skills on equip
				if (item.isCupidBow())
				{
					if (item.getItemId() == 9140)
					{
						activeChar.addSkill(SkillTable.getInstance().getInfo(3261, 1));
					}
					else
					{
						activeChar.addSkill(SkillTable.getInstance().getInfo(3260, 0));
					}
					
					activeChar.addSkill(SkillTable.getInstance().getInfo(3262, 0));
				}
				
				items = activeChar.getInventory().equipItemAndRecord(item);
				
				if (item.getItem() instanceof L2Weapon)
				{
					
					// charge Soulshot/Spiritshot like L2OFF
					activeChar.rechargeAutoSoulShot(true, true, false, 0);
				}
				// Consume mana - will start a task if required; returns if item is not a shadow item
				item.decreaseMana(false);
			}
			
			sm = null;
			
			/*
			 * if(item.getItem().getType2() == L2Item.TYPE2_WEAPON) { activeChar.checkIfWeaponIsAllowed(); }
			 */
			
			activeChar.abortAttack();
			
			activeChar.sendPacket(new EtcStatusUpdate(activeChar));
			// if an "invisible" item has changed (Jewels, helmet),
			// we dont need to send broadcast packet to all other users
			if (!((item.getItem().getBodyPart() & L2Item.SLOT_HEAD) > 0 || (item.getItem().getBodyPart() & L2Item.SLOT_NECK) > 0 || (item.getItem().getBodyPart() & L2Item.SLOT_L_EAR) > 0 || (item.getItem().getBodyPart() & L2Item.SLOT_R_EAR) > 0 || (item.getItem().getBodyPart() & L2Item.SLOT_L_FINGER) > 0 || (item.getItem().getBodyPart() & L2Item.SLOT_R_FINGER) > 0))
			{
				activeChar.broadcastUserInfo();
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				activeChar.sendPacket(iu);
			}
			else if ((item.getItem().getBodyPart() & L2Item.SLOT_HEAD) > 0)
			{
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				activeChar.sendPacket(iu);
				activeChar.sendPacket(new UserInfo(activeChar));
			}
			else
			{
				// because of complicated jewels problem i'm forced to resend the item list :(
				activeChar.sendPacket(new ItemList(activeChar, true));
				activeChar.sendPacket(new UserInfo(activeChar));
			}
		}
		else
		{
			final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			final int itemid = item.getItemId();
			// LOGGER.warn( "item not equipable id:"+ item.getItemId());
			if (itemid == 4393)
			{
				activeChar.sendPacket(new ShowCalculator(4393));
			}
			else if (weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD && (itemid >= 6519 && itemid <= 6527 || itemid >= 7610 && itemid <= 7613 || itemid >= 7807 && itemid <= 7809 || itemid >= 8484 && itemid <= 8486 || itemid >= 8505 && itemid <= 8513))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				final ItemList il = new ItemList(activeChar, false);
				sendPacket(il);
				return;
			}
			else
			{
				final IItemHandler handler = ItemHandler.getInstance().getItemHandler(itemId);
				if (handler == null)
				{
					if (Config.DEBUG)
					{
						LOGGER.warn("No item handler registered for item ID " + itemId + ".");
					}
				}
				else
				{
					handler.useItem(activeChar, item);
				}
			}
		}
		// }
	}
	
	@Override
	public String getType()
	{
		return "[C] 14 UseItem";
	}
	
}