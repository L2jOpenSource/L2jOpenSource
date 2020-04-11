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
package l2r.gameserver.scripts.handlers.skillhandlers;

import l2r.Config;
import l2r.gameserver.datatables.xml.ManorData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author l3x
 */
public class Sow implements ISkillHandler
{
	private static Logger _log = LoggerFactory.getLogger(Sow.class);
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.SOW
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!activeChar.isPlayer())
		{
			return;
		}
		
		final L2Object[] targetList = skill.getTargetList(activeChar);
		if ((targetList == null) || (targetList.length == 0))
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info("Casting sow");
		}
		
		L2MonsterInstance target;
		
		for (L2Object tgt : targetList)
		{
			if (!tgt.isMonster())
			{
				continue;
			}
			
			target = (L2MonsterInstance) tgt;
			if (target.isDead() || target.isSeeded() || (target.getSeederId() != activeChar.getObjectId()))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			final int seedId = target.getSeedType();
			if (seedId == 0)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				continue;
			}
			
			// Consuming used seed
			if (!activeChar.destroyItemByItemId("Consume", seedId, 1, target, false))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			SystemMessage sm;
			if (calcSuccess(activeChar, target, seedId))
			{
				activeChar.sendPacket(QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
				target.setSeeded(activeChar.getActingPlayer());
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
			}
			
			if (activeChar.getParty() == null)
			{
				activeChar.sendPacket(sm);
			}
			else
			{
				activeChar.getParty().broadcastPacket(sm);
			}
			
			// TODO: Mob should not aggro on player, this way doesn't work really nice
			target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	private boolean calcSuccess(L2Character activeChar, L2Character target, int seedId)
	{
		// TODO: check all the chances
		int basicSuccess = (ManorData.getInstance().isAlternative(seedId) ? 20 : 90);
		final int minlevelSeed = ManorData.getInstance().getSeedMinLevel(seedId);
		final int maxlevelSeed = ManorData.getInstance().getSeedMaxLevel(seedId);
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level
		
		// seed level
		if (levelTarget < minlevelSeed)
		{
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if (levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}
		
		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		if (diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}
		
		// chance can't be less than 1%
		if (basicSuccess < 1)
		{
			basicSuccess = 1;
		}
		
		return Rnd.nextInt(99) < basicSuccess;
	}
	
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
