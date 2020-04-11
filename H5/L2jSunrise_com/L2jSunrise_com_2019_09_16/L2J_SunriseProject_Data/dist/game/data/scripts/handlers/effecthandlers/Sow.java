/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package handlers.effecthandlers;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.model.L2Seed;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.util.Rnd;

/**
 * Sow effect implementation.
 * @author Adry_85, l3x, vGodFather
 */
public final class Sow extends L2Effect
{
	public Sow(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffector().isPlayer() || !getEffected().isMonster())
		{
			return false;
		}
		
		final L2PcInstance player = getEffector().getActingPlayer();
		final L2MonsterInstance target = (L2MonsterInstance) getEffected();
		
		if (target.isDead() || target.isSeeded() || (target.getSeederId() != player.getObjectId()))
		{
			return false;
		}
		
		// Consuming used seed
		final L2Seed seed = target.getSeed();
		if (!player.destroyItemByItemId("Consume", seed.getSeedId(), 1, target, false))
		{
			return false;
		}
		
		final SystemMessage sm;
		if (calcSuccess(player, target, seed))
		{
			player.sendPacket(QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
			target.setSeeded(player.getActingPlayer());
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
		}
		
		if (player.isInParty())
		{
			player.getParty().broadcastPacket(sm);
		}
		else
		{
			player.sendPacket(sm);
		}
		
		target.abortAttack();
		target.abortCast();
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		return true;
	}
	
	private static boolean calcSuccess(L2Character activeChar, L2Character target, L2Seed seed)
	{
		// vGodFather Chances confirmed
		final int minlevelSeed = seed.getLevel() - 5;
		final int maxlevelSeed = seed.getLevel() + 5;
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level
		int basicSuccess = seed.isAlternative() ? 20 : 90;
		
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
		Math.max(basicSuccess, 1);
		return Rnd.nextInt(99) < basicSuccess;
	}
}
