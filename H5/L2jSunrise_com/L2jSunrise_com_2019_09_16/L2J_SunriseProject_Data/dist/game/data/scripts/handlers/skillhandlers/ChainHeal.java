/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package handlers.skillhandlers;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import gr.sr.utils.ValueSortMap;

/**
 * @author GodFather
 */
public class ChainHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CHAIN_HEAL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// check for other effects
		ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
		
		if (handler != null)
		{
			handler.useSkill(activeChar, skill, targets);
		}
		
		double amount = 0;
		
		L2Character[] characters = getTargetsToHeal((L2Character[]) targets);
		double power = skill.getPower();
		
		// Get top 10 most damaged and iterate the heal over them
		for (L2Character character : characters)
		{
			if (power == 100.)
			{
				amount = character.getMaxHp();
			}
			else
			{
				amount = (character.getMaxHp() * power) / 100.0;
			}
			
			// Prevents overheal and negative amount
			amount = Math.max(Math.min(amount, character.getMaxRecoverableHp() - character.getCurrentHp()), 0);
			if (amount != 0)
			{
				character.setCurrentHp(amount + character.getCurrentHp());
			}
			SystemMessage sm;
			if (activeChar != character)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_C1);
				sm.addCharName(activeChar);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
			}
			sm.addInt(((int) amount));
			character.sendPacket(sm);
			
			if (power > 10)
			{
				power -= 3;
			}
		}
	}
	
	private L2Character[] getTargetsToHeal(L2Character[] targets)
	{
		Map<L2Character, Double> tmpTargets = new ConcurrentHashMap<>();
		Set<L2Character> sortedListToReturn = ConcurrentHashMap.newKeySet();
		int curTargets = 0;
		
		for (L2Character target : targets)
		{
			// Just in case
			// 1505 - sublime self sacrifice
			if ((target == null) || target.isDead() || target.isInvul())
			{
				continue;
			}
			
			if (target.getMaxHp() == target.getCurrentHp())
			{
				continue;
			}
			
			double hpPercent = target.getCurrentHp() / target.getMaxHp();
			tmpTargets.put(target, hpPercent);
			
			curTargets++;
			if (curTargets >= 10)
			{
				break;
			}
		}
		
		// Sort in ascending order then add the values to the list
		ValueSortMap.sortMapByValue(tmpTargets, true);
		sortedListToReturn.addAll(tmpTargets.keySet());
		
		return sortedListToReturn.toArray(new L2Character[sortedListToReturn.size()]);
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}