/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.actor.tasks.cubic;

import java.util.concurrent.atomic.AtomicInteger;

import l2r.Config;
import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2CubicInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.l2skills.L2SkillDrain;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;
import l2r.util.Rnd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cubic action task.
 * @author GodFather
 */
public class CubicAction implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(CubicAction.class);
	private final L2CubicInstance _cubic;
	private final AtomicInteger _currentCount = new AtomicInteger();
	private final int _chance;
	
	public CubicAction(L2CubicInstance cubic, int chance)
	{
		_cubic = cubic;
		_chance = chance;
	}
	
	@Override
	public void run()
	{
		if (_cubic == null)
		{
			return;
		}
		
		try
		{
			if (_cubic.getOwner().isDead() || !_cubic.getOwner().isOnline())
			{
				_cubic.stopAction();
				_cubic.getOwner().getCubics().remove(_cubic.getId());
				_cubic.getOwner().broadcastUserInfo();
				_cubic.cancelDisappear();
				return;
			}
			if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_cubic.getOwner()))
			{
				if (_cubic.getOwner().hasSummon())
				{
					if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(_cubic.getOwner().getSummon()))
					{
						_cubic.stopAction();
						return;
					}
				}
				else
				{
					_cubic.stopAction();
					return;
				}
			}
			
			// The cubic has already reached its limit and it will stay idle until its duration ends.
			if ((_cubic.getCubicMaxCount() > -1) && (_currentCount.get() >= _cubic.getCubicMaxCount()))
			{
				_cubic.stopAction();
				return;
			}
			
			// Smart Cubic debuff cancel is 100%
			boolean UseCubicCure = false;
			L2Skill skill = null;
			if ((_cubic.getId() >= L2CubicInstance.SMART_CUBIC_EVATEMPLAR) && (_cubic.getId() <= L2CubicInstance.SMART_CUBIC_SPECTRALMASTER))
			{
				for (L2Effect e : _cubic.getOwner().getEffectList().getDebuffs())
				{
					if ((e != null) && !e.getSkill().hasSelfEffects() && e.getSkill().canBeDispeled())
					{
						UseCubicCure = true;
						e.exit();
					}
				}
			}
			
			if (UseCubicCure)
			{
				// Smart Cubic debuff cancel is needed, no other skill is used in this
				// activation period
				MagicSkillUse msu = new MagicSkillUse(_cubic.getOwner(), _cubic.getOwner(), L2CubicInstance.SKILL_CUBIC_CURE, 1, 0, 0);
				_cubic.getOwner().broadcastPacket(msu);
				
				// The cubic has done an action, increase the current count
				_currentCount.incrementAndGet();
			}
			else if (Rnd.get(1, 100) < _chance)
			{
				skill = _cubic.getSkills().get(Rnd.get(_cubic.getSkills().size()));
				if (skill != null)
				{
					if (skill.getId() == L2CubicInstance.SKILL_CUBIC_HEAL)
					{
						// friendly skill, so we look a target in owner's party
						_cubic.cubicTargetForHeal();
					}
					else
					{
						// offensive skill, we look for an enemy target
						_cubic.getCubicTarget();
					}
					
					// vGodFather: proper checks for range
					final L2Character target = _cubic.getTarget();
					if ((target == null) || !L2CubicInstance.isInCubicRange(_cubic.getOwner(), target))
					{
						_cubic.setTarget(null);
						return;
					}
					
					if (!target.isDead())
					{
						if (Config.DEBUG)
						{
							_log.info("L2CubicInstance: Action.run();");
							_log.info("Cubic Id: " + _cubic.getId() + " Target: " + target.getName() + " distance: " + Math.sqrt(target.getDistanceSq(_cubic.getOwner().getX(), _cubic.getOwner().getY(), _cubic.getOwner().getZ())));
						}
						
						_cubic.getOwner().broadcastPacket(new MagicSkillUse(_cubic.getOwner(), target, skill.getId(), skill.getLevel(), 0, 0));
						
						L2SkillType type = skill.getSkillType();
						ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
						L2Character[] targets =
						{
							target
						};
						
						if ((type == L2SkillType.PARALYZE) || (type == L2SkillType.STUN) || (type == L2SkillType.ROOT) || (type == L2SkillType.AGGDAMAGE))
						{
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run() handler " + type);
							}
							
							_cubic.useCubicDisabler(type, _cubic, skill, targets);
						}
						else if (type == L2SkillType.MDAM)
						{
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run() handler " + type);
							}
							
							_cubic.useCubicMdam(_cubic, skill, targets);
						}
						else if ((type == L2SkillType.POISON) || (type == L2SkillType.DEBUFF) || (type == L2SkillType.DOT))
						{
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run() handler " + type);
							}
							
							_cubic.useCubicContinuous(_cubic, skill, targets);
						}
						else if (type == L2SkillType.DRAIN)
						{
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run() skill " + type);
							}
							
							((L2SkillDrain) skill).useCubicSkill(_cubic, targets);
						}
						else
						{
							handler.useSkill(_cubic.getOwner(), skill, targets);
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run(); other handler");
							}
						}
						
						if (skill.hasEffectType(L2EffectType.DMG_OVER_TIME, L2EffectType.DMG_OVER_TIME_PERCENT))
						{
							if (Config.DEBUG)
							{
								_log.info("L2CubicInstance: Action.run() handler " + type);
							}
							
							_cubic.useCubicContinuous(_cubic, skill, targets);
						}
						
						// The cubic has done an action, increase the current count
						_currentCount.incrementAndGet();
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error("", e);
		}
	}
}
