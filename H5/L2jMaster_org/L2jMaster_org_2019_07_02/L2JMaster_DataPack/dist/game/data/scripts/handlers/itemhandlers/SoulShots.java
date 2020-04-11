/*
 * Copyright (C) 2004-2019 L2J DataPack
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
package handlers.itemhandlers;

import java.util.logging.Level;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.ShotType;
import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.ActionType;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.util.Rnd;

public class SoulShots implements IItemHandler
{
	private static final int CP_POT_CD = 2;
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		final L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		final SkillHolder[] skills = item.getItem().getSkills();
		
		int itemId = item.getId();
		
		if (skills == null)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": is missing skills!");
			return false;
		}
		if ((itemId == 23022) || (itemId == 23023))
		{
			switch (itemId)
			{
				case 23022: // cp potion
				{
					if (!activeChar.isAffectedBySkill(922))
					{
						if (activeChar.isAutoPot(23022))
						{
							
							activeChar.sendPacket(new ExAutoSoulShot(23022, 0));
							activeChar.sendMessage("Deactivated auto cp potions.");
							activeChar.setAutoPot(23022, null, false);
						}
						else
						{
							if (activeChar.getInventory().getItemByItemId(23022) != null)
							{
								if (activeChar.getInventory().getItemByItemId(23022).getCount() >= 1)
								{
									activeChar.sendPacket(new ExAutoSoulShot(23022, 1));
									if (!activeChar.isAffectedBySkill(922))
									{
										activeChar.sendMessage("Activated auto cp potions.");
										activeChar.setAutoPot(23022, ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoPot(23022, activeChar), 1000, CP_POT_CD * 1000), true);
									}
									else
									{
										activeChar.deleteAutoPot(23022);
										activeChar.sendMessage("Deactivated auto cp potions.");
										activeChar.setAutoPot(23022, null, false);
										
									}
								}
								else
								{
									MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 80001, 1, 0, 100);
									activeChar.broadcastPacket(msu);
									
									ItemSkills is = new ItemSkills();
									is.useItem(activeChar, activeChar.getInventory().getItemByItemId(23022), true);
								}
							}
						}
					}
					break;
				}
				case 23023: // greater cp potion 5592
				{
					if (!activeChar.isAffectedBySkill(922))
					{
						if (activeChar.isAutoPot(23023))
						{
							
							activeChar.sendPacket(new ExAutoSoulShot(23023, 0));
							activeChar.sendMessage("Deactivated auto cp potions.");
							activeChar.setAutoPot(23023, null, false);
						}
						else
						{
							if (activeChar.getInventory().getItemByItemId(23023) != null)
							{
								if (activeChar.getInventory().getItemByItemId(23023).getCount() >= 1)
								{
									activeChar.sendPacket(new ExAutoSoulShot(23023, 1));
									if (!activeChar.isAffectedBySkill(922))
									{
										activeChar.sendMessage("Activated auto cp potions.");
										activeChar.setAutoPot(23023, ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoPot(23023, activeChar), 1000, CP_POT_CD * 1000), true);
									}
									else
									{
										activeChar.deleteAutoPot(23023);
										activeChar.sendMessage("Deactivated auto cp potions.");
										activeChar.setAutoPot(23023, null, false);
										
									}
								}
								else
								{
									MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 80001, 2, 0, 100);
									activeChar.broadcastPacket(msu);
									
									ItemSkills is = new ItemSkills();
									is.useItem(activeChar, activeChar.getInventory().getItemByItemId(23023), true);
								}
							}
						}
					}
					break;
				}
			}
			
			return true;
		}
		// Check if Soul shot can be used
		if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			}
			return false;
		}
		
		boolean gradeCheck = item.isEtcItem() && (item.getEtcItem().getDefaultAction() == ActionType.SOULSHOT) && (weaponInst.getItem().getItemGradeSPlus() == item.getItem().getItemGradeSPlus());
		
		if (!gradeCheck)
		{
			if (!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
			}
			return false;
		}
		
		activeChar.soulShotLock.lock();
		try
		{
			// Check if Soul shot is already active
			if (activeChar.isChargedShot(ShotType.SOULSHOTS))
			{
				return false;
			}
			
			// Consume Soul shots if player has enough of them
			int SSCount = weaponItem.getSoulShotCount();
			if ((weaponItem.getReducedSoulShot() > 0) && (Rnd.get(100) < weaponItem.getReducedSoulShotChance()))
			{
				SSCount = weaponItem.getReducedSoulShot();
			}
			
			if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
			{
				if (!activeChar.disableAutoShot(itemId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
				}
				return false;
			}
			// Charge soul shot
			weaponInst.setChargedShot(ShotType.SOULSHOTS, true);
		}
		finally
		{
			activeChar.soulShotLock.unlock();
		}
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1_);
		sm.addItemName(itemId);
		activeChar.sendPacket(sm);
		
		activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
		Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, skills[0].getSkillId(), skills[0].getSkillLvl(), 0, 0), 600);
		return true;
	}
	
	private class AutoPot implements Runnable
	{
		private final int id;
		private final L2PcInstance activeChar;
		
		public AutoPot(int id, L2PcInstance activeChar)
		{
			this.id = id;
			this.activeChar = activeChar;
		}
		
		@Override
		public void run()
		{
			if (activeChar.getInventory().getItemByItemId(id) == null)
			{
				activeChar.sendPacket(new ExAutoSoulShot(id, 0));
				activeChar.setAutoPot(id, null, false);
				return;
			}
			
			switch (id)
			{
				case 23022:
				{
					if ((activeChar.isAffectedBySkill(922)) || (activeChar.isAffectedBySkill(1418)))
					{
						break;
					}
					if (activeChar.getCurrentCp() < (0.95 * activeChar.getMaxCp()))
					{
						MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 80001, 1, 0, 100);
						activeChar.broadcastPacket(msu);
						
						ItemSkills is = new ItemSkills();
						is.useItem(activeChar, activeChar.getInventory().getItemByItemId(23022), true);
					}
					break;
				}
				case 23023:
				{
					if ((activeChar.isAffectedBySkill(922)) || (activeChar.isAffectedBySkill(1418)))
					{
						break;
					}
					if (activeChar.getCurrentCp() < (0.95 * activeChar.getMaxCp()))
					{
						MagicSkillUse msu = new MagicSkillUse(activeChar, activeChar, 80001, 2, 0, 100);
						activeChar.broadcastPacket(msu);
						
						ItemSkills is = new ItemSkills();
						is.useItem(activeChar, activeChar.getInventory().getItemByItemId(23023), true);
					}
					break;
				}
			}
			
			if (activeChar.getInventory().getItemByItemId(id) == null)
			{
				activeChar.sendPacket(new ExAutoSoulShot(id, 0));
				activeChar.setAutoPot(id, null, false);
			}
		}
	}
}
