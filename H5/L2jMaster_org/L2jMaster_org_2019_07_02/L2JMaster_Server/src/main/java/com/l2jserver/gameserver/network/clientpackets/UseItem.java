/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.network.clientpackets;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.l2jserver.Config;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlEvent;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.ai.NextAction;
import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.instancemanager.FortSiegeManager;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.L2EtcItem;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.ArmorType;
import com.l2jserver.gameserver.model.items.type.ItemType2;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ExUseSharedGroupItem;
import com.l2jserver.gameserver.network.serverpackets.ItemList;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

public final class UseItem extends L2GameClientPacket
{
	private static final String _C__19_USEITEM = "[C] 19 UseItem";
	
	private int _objectId;
	private boolean _ctrlPressed;
	private int _itemId;
	
	/** Weapon Equip Task */
	private static class WeaponEquipTask implements Runnable
	{
		private final L2ItemInstance item;
		private final L2PcInstance activeChar;
		
		protected WeaponEquipTask(L2ItemInstance it, L2PcInstance character)
		{
			item = it;
			activeChar = character;
		}
		
		@Override
		public void run()
		{
			// Equip or unEquip
			activeChar.useEquippableItem(item, false);
		}
	}
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_ctrlPressed = readD() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.log(Level.INFO, activeChar + ": use item " + _objectId);
		}
		
		// Flood protect UseItem
		if (!getClient().getFloodProtectors().getUseItem().tryPerformAction("use item"))
		{
			return;
		}
		
		if (activeChar.getActiveTradeList() != null)
		{
			activeChar.cancelActiveTrade();
		}
		
		if (activeChar.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if (item == null)
		{
			return;
		}
		
		if (item.getItem().getType2() == ItemType2.QUEST)
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_USE_QUEST_ITEMS);
			return;
		}
		
		// No UseItem is allowed while the player is in special conditions
		if (activeChar.isStunned() || activeChar.isParalyzed() || activeChar.isSleeping() || activeChar.isAfraid() || activeChar.isAlikeDead())
		{
			return;
		}
		
		_itemId = item.getId();
		
		if (!Config.ALLOW_HEAVY_USE_LIGHT)
		{
			if (Config.NOTALLOWEDUSELIGHT.contains(activeChar.getClassId().getId()))
			{
				if (item.getItemType() == ArmorType.LIGHT)
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Armor Protection System", " " + activeChar.getName() + " this item can not be equipped by your class!");
					activeChar.sendPacket(cs);
					
					return;
				}
			}
		}
		if (!Config.ALLOW_LIGHT_USE_HEAVY)
		{
			if (Config.NOTALLOWEDUSEHEAVY.contains(activeChar.getClassId().getId()))
			{
				if (item.getItemType() == ArmorType.HEAVY)
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Armor Protection System", " " + activeChar.getName() + " this item can not be equipped by your class!");
					activeChar.sendPacket(cs);
					
					return;
				}
			}
		}
		
		// Weapon Restriction
		if (Config.ALT_DISABLE_BOW_CLASSES)
		{
			if ((item.getItem() instanceof L2Weapon) && (((L2Weapon) item.getItem()).getItemType() == WeaponType.BOW))
			{
				if (Config.DISABLE_BOW_CLASSES.contains(activeChar.getClassId().getId()))
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Weapon Protection System", " " + activeChar.getName() + " this item can not be equipped by your class");
					activeChar.sendPacket(cs);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					
					return;
				}
			}
		}
		if (Config.ALT_DISABLE_DAGGER_CLASSES)
		{
			if ((item.getItem() instanceof L2Weapon) && (((L2Weapon) item.getItem()).getItemType() == WeaponType.DAGGER))
			{
				if (Config.DISABLE_DAGGER_CLASSES.contains(activeChar.getClassId().getId()))
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Weapon Protection System", " " + activeChar.getName() + " this item can not be equipped by your class");
					activeChar.sendPacket(cs);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					
					return;
				}
			}
		}
		
		if (Config.ALT_DISABLE_SWORD_CLASSES)
		{
			if ((item.getItem() instanceof L2Weapon) && (((L2Weapon) item.getItem()).getItemType() == WeaponType.SWORD))
			{
				if (Config.DISABLE_SWORD_CLASSES.contains(activeChar.getClassId().getId()))
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Weapon Protection System", " " + activeChar.getName() + " this item can not be equipped by your class");
					activeChar.sendPacket(cs);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					
					return;
				}
			}
		}
		if (Config.ALT_DISABLE_BLUNT_CLASSES)
		{
			if ((item.getItem() instanceof L2Weapon) && (((L2Weapon) item.getItem()).getItemType() == WeaponType.BLUNT))
			{
				if (Config.DISABLE_BLUNT_CLASSES.contains(activeChar.getClassId().getId()))
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Weapon Protection System", " " + activeChar.getName() + " this item can not be equipped by your class");
					activeChar.sendPacket(cs);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					
					return;
				}
			}
		}
		
		if (Config.ALT_DISABLE_DUAL_CLASSES)
		{
			if ((item.getItem() instanceof L2Weapon) && (((L2Weapon) item.getItem()).getItemType() == WeaponType.DUAL))
			{
				if (Config.DISABLE_DUAL_CLASSES.contains(activeChar.getClassId().getId()))
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Weapon Protection System", " " + activeChar.getName() + " this item can not be equipped by your class");
					activeChar.sendPacket(cs);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					
					return;
				}
			}
		}
		
		if (Config.ALT_DISABLE_POLE_CLASSES)
		{
			if ((item.getItem() instanceof L2Weapon) && (((L2Weapon) item.getItem()).getItemType() == WeaponType.POLE))
			{
				if (Config.DISABLE_POLE_CLASSES.contains(activeChar.getClassId().getId()))
				{
					CreatureSay cs = new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "Weapon Protection System", " " + activeChar.getName() + " this item can not be equipped by your class");
					activeChar.sendPacket(cs);
					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
					
					return;
				}
			}
		}
		
		// Char cannot use item when dead
		if (activeChar.isDead() || !activeChar.getInventory().canManipulateWithItemId(_itemId))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(item);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (!item.isEquipped() && !item.getItem().checkCondition(activeChar, activeChar, true))
		{
			return;
		}
		
		if (activeChar.isFishing() && ((_itemId < 6535) || (_itemId > 6540)))
		{
			// You cannot do anything else while fishing
			activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
			return;
		}
		
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && (activeChar.getKarma() > 0))
		{
			SkillHolder[] skills = item.getItem().getSkills();
			if (skills != null)
			{
				for (SkillHolder sHolder : skills)
				{
					Skill skill = sHolder.getSkill();
					if ((skill != null) && skill.hasEffectType(L2EffectType.TELEPORT))
					{
						return;
					}
				}
			}
		}
		
		// If the item has reuse time and it has not passed.
		// Message from reuse delay must come from item.
		final int reuseDelay = item.getReuseDelay();
		final int sharedReuseGroup = item.getSharedReuseGroup();
		if (reuseDelay > 0)
		{
			final long reuse = activeChar.getItemRemainingReuseTime(item.getObjectId());
			if (reuse > 0)
			{
				reuseData(activeChar, item, reuse);
				sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuse, reuseDelay);
				return;
			}
			
			final long reuseOnGroup = activeChar.getReuseDelayOnGroup(sharedReuseGroup);
			if (reuseOnGroup > 0)
			{
				reuseData(activeChar, item, reuseOnGroup);
				sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseOnGroup, reuseDelay);
				return;
			}
		}
		
		if (item.isEquipable())
		{
			// Don't allow to put formal wear while a cursed weapon is equipped.
			if (activeChar.isCursedWeaponEquipped() && (_itemId == 6408))
			{
				return;
			}
			
			// Equip or unEquip
			if (FortSiegeManager.getInstance().isCombat(_itemId))
			{
				return; // no message
			}
			
			if (activeChar.isCombatFlagEquipped())
			{
				return;
			}
			
			switch (item.getItem().getBodyPart())
			{
				case L2Item.SLOT_LR_HAND:
				case L2Item.SLOT_L_HAND:
				case L2Item.SLOT_R_HAND:
				{
					// Prevent players to equip weapon while wearing combat flag
					if ((activeChar.getActiveWeaponItem() != null) && (activeChar.getActiveWeaponItem().getId() == 9819))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					if (activeChar.isMounted())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					if (activeChar.isDisarmed())
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					
					// Don't allow weapon/shield equipment if a cursed weapon is equipped.
					if (activeChar.isCursedWeaponEquipped())
					{
						return;
					}
					
					// Don't allow other Race to Wear Kamael exclusive Weapons.
					if (!item.isEquipped() && item.isWeapon() && !activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS))
					{
						L2Weapon wpn = (L2Weapon) item.getItem();
						
						switch (activeChar.getRace())
						{
							case KAMAEL:
							{
								switch (wpn.getItemType())
								{
									case NONE:
										activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
										return;
								}
								break;
							}
							case HUMAN:
							case DWARF:
							case ELF:
							case DARK_ELF:
							case ORC:
							{
								switch (wpn.getItemType())
								{
									case RAPIER:
									case CROSSBOW:
									case ANCIENTSWORD:
										activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
										return;
								}
								break;
							}
						}
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
					if ((activeChar.getRace() == Race.KAMAEL) && ((item.getItem().getItemType() == ArmorType.HEAVY) || (item.getItem().getItemType() == ArmorType.MAGIC)))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
					break;
				}
				case L2Item.SLOT_DECO:
				{
					if (!item.isEquipped() && (activeChar.getInventory().getTalismanSlots() == 0))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
						return;
					}
				}
			}
			
			if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
			{
				// Creating next action class.
				final NextAction nextAction = new NextAction(CtrlEvent.EVT_FINISH_CASTING, CtrlIntention.AI_INTENTION_CAST, () -> activeChar.useEquippableItem(item, true));
				
				// Binding next action to AI.
				activeChar.getAI().setNextAction(nextAction);
			}
			else if (activeChar.isAttackingNow())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new WeaponEquipTask(item, activeChar), TimeUnit.MILLISECONDS.convert(activeChar.getAttackEndTime() - System.nanoTime(), TimeUnit.NANOSECONDS));
			}
			else
			{
				activeChar.useEquippableItem(item, true);
			}
		}
		else
		{
			final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			if (((weaponItem != null) && (weaponItem.getItemType() == WeaponType.FISHINGROD)) && (((_itemId >= 6519) && (_itemId <= 6527)) || ((_itemId >= 7610) && (_itemId <= 7613)) || ((_itemId >= 7807) && (_itemId <= 7809)) || ((_itemId >= 8484) && (_itemId <= 8486)) || ((_itemId >= 8505) && (_itemId <= 8513))))
			{
				activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
				activeChar.broadcastUserInfo();
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipment.
				sendPacket(new ItemList(activeChar, false));
				return;
			}
			
			final L2EtcItem etcItem = item.getEtcItem();
			final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
			if (handler == null)
			{
				if ((etcItem != null) && (etcItem.getHandlerName() != null))
				{
					_log.log(Level.WARNING, "Unmanaged Item handler: " + etcItem.getHandlerName() + " for Item Id: " + _itemId + "!");
				}
				else if (Config.DEBUG)
				{
					_log.log(Level.WARNING, "No Item handler registered for Item Id: " + _itemId + "!");
				}
				return;
			}
			
			// Item reuse time should be added if the item is successfully used.
			// Skill reuse delay is done at handlers.itemhandlers.ItemSkillsTemplate;
			if (handler.useItem(activeChar, item, _ctrlPressed))
			{
				if (reuseDelay > 0)
				{
					activeChar.addTimeStampItem(item, reuseDelay);
					sendSharedGroupUpdate(activeChar, sharedReuseGroup, reuseDelay, reuseDelay);
				}
			}
		}
	}
	
	private void reuseData(L2PcInstance activeChar, L2ItemInstance item, long remainingTime)
	{
		final int hours = (int) (remainingTime / 3600000L);
		final int minutes = (int) (remainingTime % 3600000L) / 60000;
		final int seconds = (int) ((remainingTime / 1000) % 60);
		final SystemMessage sm;
		if (hours > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
			sm.addInt(hours);
			sm.addInt(minutes);
		}
		else if (minutes > 0)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
			sm.addInt(minutes);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
			sm.addItemName(item);
		}
		sm.addInt(seconds);
		activeChar.sendPacket(sm);
	}
	
	private void sendSharedGroupUpdate(L2PcInstance activeChar, int group, long remaining, int reuse)
	{
		if (group > 0)
		{
			activeChar.sendPacket(new ExUseSharedGroupItem(_itemId, group, remaining, reuse));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__19_USEITEM;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return !Config.SPAWN_PROTECTION_ALLOWED_ITEMS.contains(_itemId);
	}
}
