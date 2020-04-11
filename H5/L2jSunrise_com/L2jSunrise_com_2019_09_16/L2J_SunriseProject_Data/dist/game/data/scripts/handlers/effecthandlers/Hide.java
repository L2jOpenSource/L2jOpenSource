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
package handlers.effecthandlers;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;

/**
 * @author ZaKaX, nBd
 */
public class Hide extends L2Effect
{
	public Hide(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public Hide(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HIDE;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected().isPlayer())
		{
			L2PcInstance activeChar = getEffected().getActingPlayer();
			activeChar.setInvisible(true);
			activeChar.startAbnormalEffect(AbnormalEffect.STEALTH);
			
			if ((activeChar.getAI().getNextIntention() != null) && (activeChar.getAI().getNextIntention().getCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK))
			{
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
			
			for (L2Character target : activeChar.getKnownList().getKnownCharacters())
			{
				if ((target != null) && (target.getTarget() == activeChar))
				{
					target.setTarget(null);
					target.abortAttack();
					target.abortCast();
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected().isPlayer())
		{
			L2PcInstance activeChar = getEffected().getActingPlayer();
			if (!activeChar.inObserverMode())
			{
				activeChar.setInvisible(false);
			}
			activeChar.stopAbnormalEffect(AbnormalEffect.STEALTH);
		}
	}
}