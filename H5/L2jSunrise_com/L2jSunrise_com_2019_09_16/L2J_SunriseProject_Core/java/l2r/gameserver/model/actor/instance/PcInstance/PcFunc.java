/*
 * Copyright (C) 2004-2016 L2J Server
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
package l2r.gameserver.model.actor.instance.PcInstance;

import l2r.Config;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.model.holders.SummonRequestHolder;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import gr.sr.interf.SunriseEvents;

/**
 * @author vGodFather<br>
 *         This class holds some useful functions for L2PcInstance
 */
public class PcFunc
{
	/**
	 * Handles pc recall skill
	 * @param activeChar
	 * @param holder
	 */
	public static void teleToTarget(L2PcInstance activeChar, SummonRequestHolder holder)
	{
		if ((activeChar == null) || (holder == null))
		{
			return;
		}
		
		if (!checkSummonTargetStatus(activeChar, holder.getRequester()))
		{
			return;
		}
		
		int itemId = holder.getItemId();
		int itemCount = holder.getItemCount();
		if ((itemId != 0) && (holder.getItemCount() != 0))
		{
			if (activeChar.getInventory().getInventoryItemCount(itemId, 0) < itemCount)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING);
				sm.addItemName(itemId);
				activeChar.sendPacket(sm);
				return;
			}
			
			activeChar.getInventory().destroyItemByItemId("Consume", itemId, itemCount, holder.getRequester(), activeChar);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addItemName(itemId);
			activeChar.sendPacket(sm);
		}
		activeChar.teleToLocation(holder.getRequester().getLocation(), true);
	}
	
	public static boolean checkSummonTargetStatus(L2PcInstance target, L2PcInstance activeChar)
	{
		if (target == activeChar)
		{
			return false;
		}
		
		if (target.isAlikeDead())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInStoreMode())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isRooted() || target.isInCombat())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInOlympiadMode() || target.isInOlympiad() || target.inObserverMode() || OlympiadManager.getInstance().isRegistered(target))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}
		
		if (target.isFestivalParticipant() || target.isFlyingMounted() || target.isCombatFlagEquipped())
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (target.inObserverMode())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_STATE_FORBIDS_SUMMONING);
			sm.addCharName(target);
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (target.isInsideZone(ZoneIdType.NO_SUMMON_FRIEND) || target.isInsideZone(ZoneIdType.JAIL))
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			return false;
		}
		
		if (activeChar.getInstanceId() > 0)
		{
			Instance summonerInstance = InstanceManager.getInstance().getInstance(activeChar.getInstanceId());
			if (!Config.ALLOW_SUMMON_IN_INSTANCE || !summonerInstance.isSummonAllowed())
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
		}
		
		if (activeChar.isIn7sDungeon())
		{
			int targetCabal = SevenSigns.getInstance().getPlayerCabal(target.getObjectId());
			if (SevenSigns.getInstance().isSealValidationPeriod())
			{
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore())
				{
					activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					return false;
				}
			}
			else if (targetCabal == SevenSigns.CABAL_NULL)
			{
				activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
				return false;
			}
		}
		
		if (SunriseEvents.isInEvent(activeChar) || SunriseEvents.isInEvent(target))
		{
			activeChar.sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		return true;
	}
}
