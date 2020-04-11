/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model;

import javolution.util.FastMap;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.interfaces.IChanceSkillTrigger;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CT2.3: Added support for allowing effect as a chance skill trigger (DrHouse)
 * @author kombat
 */
public class ChanceSkillList extends FastMap<IChanceSkillTrigger, ChanceCondition>
{
	protected static final Logger _log = LoggerFactory.getLogger(ChanceSkillList.class);
	private static final long serialVersionUID = 1L;
	
	private final L2Character _owner;
	
	public ChanceSkillList(L2Character owner)
	{
		super();
		shared();
		_owner = owner;
	}
	
	public L2Character getOwner()
	{
		return _owner;
	}
	
	public void onHit(L2Character target, int damage, boolean ownerWasHit, boolean wasCrit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
			}
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if (wasCrit)
			{
				event |= ChanceCondition.EVT_CRIT;
			}
		}
		
		onEvent(event, damage, target, null, Elementals.NONE);
	}
	
	public void onEvadedHit(L2Character attacker)
	{
		onEvent(ChanceCondition.EVT_EVADED_HIT, 0, attacker, null, Elementals.NONE);
	}
	
	public void onSkillHit(L2Character target, L2Skill skill, boolean ownerWasHit)
	{
		int event;
		if (ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if (skill.isOffensive())
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
				event |= ChanceCondition.EVT_ATTACKED_HIT;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= skill.isMagic() ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= skill.isOffensive() ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}
		
		onEvent(event, 0, target, skill, skill.getElement());
	}
	
	public void onStart(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_START, 0, _owner, null, element);
	}
	
	public void onActionTime(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_ACTION_TIME, 0, _owner, null, element);
	}
	
	public void onExit(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_EXIT, 0, _owner, null, element);
	}
	
	public void onEvent(int event, int damage, L2Character target, L2Skill skill, byte element)
	{
		if (_owner.isDead())
		{
			return;
		}
		
		final boolean playable = target instanceof L2Playable;
		for (FastMap.Entry<IChanceSkillTrigger, ChanceCondition> e = head(), end = tail(); (e = e.getNext()) != end;)
		{
			if ((e.getValue() != null) && e.getValue().trigger(event, damage, element, playable, skill))
			{
				if (e.getKey() instanceof L2Skill)
				{
					_owner.makeTriggerCast((L2Skill) e.getKey(), target);
				}
				else
				{
					makeCast((L2Effect) e.getKey(), target);
				}
			}
		}
	}
	
	private void makeCast(L2Effect effect, L2Character target)
	{
		try
		{
			if ((effect == null) || !effect.triggersChanceSkill())
			{
				return;
			}
			
			L2Skill triggered = SkillData.getInstance().getInfo(effect.getTriggeredChanceId(), effect.getTriggeredChanceLevel());
			if (triggered == null)
			{
				return;
			}
			L2Character caster = triggered.getTargetType() == L2TargetType.SELF ? _owner : effect.getEffector();
			
			if ((caster == null) || (triggered.getSkillType() == L2SkillType.NOTDONE) || caster.isSkillDisabled(triggered))
			{
				return;
			}
			
			if (triggered.getReuseDelay() > 0)
			{
				caster.disableSkill(triggered, triggered.getReuseDelay());
			}
			
			L2Object[] targets = triggered.getTargetList(caster, false, target);
			
			if (targets.length == 0)
			{
				return;
			}
			
			L2Character firstTarget = (L2Character) targets[0];
			
			ISkillHandler handler = SkillHandler.getInstance().getHandler(triggered.getSkillType());
			
			_owner.broadcastPacket(new MagicSkillLaunched(_owner, triggered.getDisplayId(), triggered.getDisplayLevel(), targets));
			_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, triggered.getDisplayId(), triggered.getDisplayLevel(), 0, 0));
			
			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if (handler != null)
			{
				handler.useSkill(caster, triggered, targets);
			}
			else
			{
				triggered.useSkill(caster, targets);
			}
		}
		catch (Exception e)
		{
			_log.warn(String.valueOf(e));
		}
	}
}