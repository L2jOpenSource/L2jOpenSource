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

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.stats.Env;

/**
 * @author UnAfraid
 */
public class ResistSkill extends L2Effect
{
	private final List<SkillHolder> _skills = new ArrayList<>();
	
	public ResistSkill(Env env, EffectTemplate template)
	{
		super(env, template);
		
		for (int i = 1;; i++)
		{
			int skillId = template.getParameters().getInt("skillId" + i, 0);
			int skillLvl = template.getParameters().getInt("skillLvl" + i, 0);
			if (skillId == 0)
			{
				break;
			}
			_skills.add(new SkillHolder(skillId, skillLvl));
		}
		
		if (_skills.isEmpty())
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + ": Without parameters!");
		}
	}
	
	@Override
	public boolean onStart()
	{
		final L2Character effected = getEffected();
		for (SkillHolder holder : _skills)
		{
			effected.addInvulAgainst(holder);
			effected.sendDebugMessage("Applying invul against " + holder.getSkill());
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		final L2Character effected = getEffected();
		for (SkillHolder holder : _skills)
		{
			getEffected().removeInvulAgainst(holder);
			effected.sendDebugMessage("Removing invul against " + holder.getSkill());
		}
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
}
