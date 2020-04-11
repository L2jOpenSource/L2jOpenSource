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

import l2r.gameserver.handler.ISkillHandler;
import l2r.gameserver.handler.SkillHandler;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2CubicInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.serverpackets.MagicSkillUse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cubic heal task.
 * @author GodFather
 */
public class CubicHeal implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(CubicHeal.class);
	private final L2CubicInstance _cubic;
	
	public CubicHeal(L2CubicInstance cubic)
	{
		_cubic = cubic;
	}
	
	@Override
	public void run()
	{
		if (_cubic == null)
		{
			return;
		}
		
		if (_cubic.getOwner().isDead() || !_cubic.getOwner().isOnline())
		{
			_cubic.stopAction();
			_cubic.getOwner().getCubics().remove(_cubic.getId());
			_cubic.getOwner().broadcastUserInfo();
			_cubic.cancelDisappear();
			return;
		}
		try
		{
			L2Skill skill = null;
			for (L2Skill sk : _cubic.getSkills())
			{
				if (sk.getId() == L2CubicInstance.SKILL_CUBIC_HEAL)
				{
					skill = sk;
					break;
				}
			}
			
			if (skill != null)
			{
				_cubic.cubicTargetForHeal();
				
				// vGodFather: proper checks for range
				final L2Character target = _cubic.getTarget();
				if ((target == null) || !L2CubicInstance.isInCubicRange(_cubic.getOwner(), target))
				{
					_cubic.setTarget(null);
					return;
				}
				
				if (!target.isDead())
				{
					if ((target.getMaxHp() - target.getCurrentHp()) > skill.getPower())
					{
						L2Character[] targets =
						{
							target
						};
						ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
						if (handler != null)
						{
							handler.useSkill(_cubic.getOwner(), skill, targets);
						}
						else
						{
							skill.useSkill(_cubic.getOwner(), targets);
						}
						
						MagicSkillUse msu = new MagicSkillUse(_cubic.getOwner(), target, skill.getId(), skill.getLevel(), 0, 0);
						_cubic.getOwner().broadcastPacket(msu);
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
