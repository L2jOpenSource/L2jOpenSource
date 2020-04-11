/*
 * This file is part of the L2J Sunrise project.
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

import l2r.gameserver.data.xml.impl.PetData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2BabyPetInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.model.skills.CommonSkill;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.util.Rnd;

/**
 * Pet skill use player action handler.
 * @author Nik
 */
public final class PetSkillUse implements IPlayerActionHandler
{
	private static final NpcStringId[] NPC_STRINGS =
	{
		NpcStringId.USING_A_SPECIAL_SKILL_HERE_COULD_TRIGGER_A_BLOODBATH,
		NpcStringId.HEY_WHAT_DO_YOU_EXPECT_OF_ME,
		NpcStringId.UGGGGGH_PUSH_ITS_NOT_COMING_OUT,
		NpcStringId.AH_I_MISSED_THE_MARK
	};
	
	@Override
	public void useAction(L2PcInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		if (activeChar.getTarget() == null)
		{
			return;
		}
		
		if ((activeChar.getSummon() == null) || !activeChar.getSummon().isPet())
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_PET);
			return;
		}
		
		final L2PetInstance pet = (L2PetInstance) activeChar.getSummon();
		if (pet == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_PET);
		}
		else if (pet.isUncontrollable())
		{
			activeChar.sendPacket(SystemMessageId.WHEN_YOUR_PET_S_HUNGER_GAUGE_IS_AT_0_YOU_CANNOT_USE_YOUR_PET);
		}
		else if (pet.isBetrayed())
		{
			activeChar.sendPacket(SystemMessageId.YOUR_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
		}
		else if (!canControl(activeChar, pet) && (data.getOptionId() != CommonSkill.PET_SWITCH_STANCE.getId()))
		{
			activeChar.sendPacket(SystemMessageId.PET_AUXILIARY_MODE_CANNOT_USE_SKILLS);
		}
		else if (((pet.getLevel() - activeChar.getLevel()) > 20) && (data.getOptionId() != CommonSkill.PET_SWITCH_STANCE.getId()))
		{
			activeChar.sendPacket(SystemMessageId.YOUR_PET_IS_TOO_HIGH_LEVEL_TO_CONTROL);
		}
		else
		{
			final int skillLevel = PetData.getInstance().getPetData(pet.getId()).getAvailableLevel(data.getOptionId(), pet.getLevel());
			if (skillLevel > 0)
			{
				pet.setTarget(activeChar.getTarget());
				pet.useMagic(SkillData.getInstance().getSkill(data.getOptionId(), skillLevel), ctrlPressed, shiftPressed);
			}
			
			if (data.getOptionId() == CommonSkill.PET_SWITCH_STANCE.getId())
			{
				pet.switchMode();
			}
			
			if (data.getOptionId() == CommonSkill.SIN_EATER_ID.getId())
			{
				pet.broadcastPacket(new NpcSay(pet.getObjectId(), Say2.NPC_ALL, pet.getId(), NPC_STRINGS[Rnd.get(NPC_STRINGS.length)]));
			}
		}
	}
	
	private boolean canControl(L2PcInstance activeChar, L2Summon summon)
	{
		if (summon instanceof L2BabyPetInstance)
		{
			if (!((L2BabyPetInstance) summon).isInSupportMode())
			{
				return false;
			}
		}
		return true;
	}
}
