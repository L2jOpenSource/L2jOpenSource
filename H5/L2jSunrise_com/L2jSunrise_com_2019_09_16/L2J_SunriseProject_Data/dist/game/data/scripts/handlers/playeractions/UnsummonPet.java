/*
 * This file is part of the L2J Sunirse project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.playeractions;

import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.network.SystemMessageId;

/**
 * Unsummon Pet player action handler.
 * @author St3eT
 */
public final class UnsummonPet implements IPlayerActionHandler
{
	@Override
	public void useAction(L2PcInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		final L2Summon pet = activeChar.getSummon();
		
		if (pet == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_PET);
		}
		else if (((L2PetInstance) pet).isUncontrollable())
		{
			activeChar.sendPacket(SystemMessageId.WHEN_YOUR_PET_S_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET);
		}
		else if (pet.isBetrayed())
		{
			activeChar.sendPacket(SystemMessageId.WHEN_YOUR_PET_S_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET);
		}
		else if (pet.isDead())
		{
			activeChar.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED);
		}
		else if (pet.isAttackingNow() || pet.isInCombat() || pet.isMovementDisabled())
		{
			activeChar.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE);
		}
		else if (pet.isHungry())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS);
		}
		else
		{
			pet.unSummon(activeChar);
		}
	}
}
