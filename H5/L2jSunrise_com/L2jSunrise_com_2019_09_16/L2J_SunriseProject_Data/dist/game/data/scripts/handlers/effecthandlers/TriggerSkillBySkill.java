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

import l2r.gameserver.handler.ITargetTypeHandler;
import l2r.gameserver.handler.TargetHandler;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.impl.character.OnCreatureSkillUse;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.model.stats.Env;
import l2r.util.Rnd;

/**
 * Trigger Skill By Skill effect implementation.
 * @author Zealar
 */
public final class TriggerSkillBySkill extends L2Effect
{
	private final int _castSkillId;
	private final int _chance;
	private final SkillHolder _skill;
	private final L2TargetType _targetType;
	
	public TriggerSkillBySkill(Env env, EffectTemplate template)
	{
		super(env, template);
		
		_castSkillId = template.getParameters().getInt("castSkillId", 0);
		_chance = template.getParameters().getInt("chance", 100);
		_skill = new SkillHolder(template.getParameters().getInt("skillId", 0), template.getParameters().getInt("skillLevel", 0));
		_targetType = template.getParameters().getEnum("targetType", L2TargetType.class, L2TargetType.ONE);
	}
	
	public void onSkillUseEvent(OnCreatureSkillUse event)
	{
		if ((_chance == 0) || ((_skill.getSkillId() == 0) || (_skill.getSkillLvl() == 0) || (_castSkillId == 0)))
		{
			return;
		}
		
		if (_castSkillId != event.getSkill().getId())
		{
			return;
		}
		
		final ITargetTypeHandler targetHandler = TargetHandler.getInstance().getHandler(_targetType);
		if (targetHandler == null)
		{
			_log.warn("Handler for target type: " + _targetType + " does not exist.");
			return;
		}
		
		if (Rnd.get(1000) > (_chance * 10))
		{
			return;
		}
		
		final L2Skill triggerSkill = _skill.getSkill();
		final L2Object[] targets = targetHandler.getTargetList(triggerSkill, event.getCaster(), false, event.getTarget());
		
		for (L2Object triggerTarget : targets)
		{
			if ((triggerTarget == null) || !triggerTarget.isCharacter())
			{
				continue;
			}
			
			final L2Character targetChar = (L2Character) triggerTarget;
			if (!targetChar.isInvul())
			{
				event.getCaster().makeTriggerCast(triggerSkill, targetChar);
			}
		}
	}
	
	@Override
	public void onExit()
	{
		getEffected().removeListenerIf(EventType.ON_CREATURE_SKILL_USE, listener -> listener.getOwner() == this);
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().addListener(new ConsumerEventListener(getEffected(), EventType.ON_CREATURE_SKILL_USE, (OnCreatureSkillUse event) -> onSkillUseEvent(event), this));
		return true;
	}
}
