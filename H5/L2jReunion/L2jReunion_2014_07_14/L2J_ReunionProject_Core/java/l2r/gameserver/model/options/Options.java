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
package l2r.gameserver.model.options;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.funcs.Func;
import l2r.gameserver.model.skills.funcs.FuncTemplate;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.serverpackets.SkillCoolTime;

/**
 * @author UnAfraid
 */
public class Options
{
	private final int _id;
	private static final Func[] _emptyFunctionSet = new Func[0];
	private final List<FuncTemplate> _funcs = new ArrayList<>();
	
	private SkillHolder _activeSkill = null;
	private SkillHolder _passiveSkill = null;
	
	private final List<OptionsSkillHolder> _activationSkills = new ArrayList<>();
	
	/**
	 * @param id
	 */
	public Options(int id)
	{
		_id = id;
	}
	
	public final int getId()
	{
		return _id;
	}
	
	public boolean hasFuncs()
	{
		return !_funcs.isEmpty();
	}
	
	public Func[] getStatFuncs(L2ItemInstance item, L2Character player)
	{
		if (_funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		
		List<Func> funcs = new ArrayList<>(_funcs.size());
		
		Env env = new Env();
		env.setCharacter(player);
		env.setTarget(player);
		env.setItem(item);
		
		Func f;
		for (FuncTemplate t : _funcs)
		{
			f = t.getFunc(env, this);
			if (f != null)
			{
				funcs.add(f);
			}
			player.sendDebugMessage("Adding stats: " + t.stat + " val: " + t.lambda.calc(env));
		}
		
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public void addFunc(FuncTemplate template)
	{
		_funcs.add(template);
	}
	
	public boolean hasActiveSkill()
	{
		return _activeSkill != null;
	}
	
	public SkillHolder getActiveSkill()
	{
		return _activeSkill;
	}
	
	public void setActiveSkill(SkillHolder holder)
	{
		_activeSkill = holder;
	}
	
	public boolean hasPassiveSkill()
	{
		return _passiveSkill != null;
	}
	
	public SkillHolder getPassiveSkill()
	{
		return _passiveSkill;
	}
	
	public void setPassiveSkill(SkillHolder holder)
	{
		_passiveSkill = holder;
	}
	
	public boolean hasActivationSkills()
	{
		return !_activationSkills.isEmpty();
	}
	
	public boolean hasActivationSkills(OptionsSkillType type)
	{
		for (OptionsSkillHolder holder : _activationSkills)
		{
			if (holder.getSkillType() == type)
			{
				return true;
			}
		}
		return false;
	}
	
	public List<OptionsSkillHolder> getActivationsSkills()
	{
		return _activationSkills;
	}
	
	public List<OptionsSkillHolder> getActivationsSkills(OptionsSkillType type)
	{
		List<OptionsSkillHolder> temp = new ArrayList<>();
		for (OptionsSkillHolder holder : _activationSkills)
		{
			if (holder.getSkillType() == type)
			{
				temp.add(holder);
			}
		}
		return temp;
	}
	
	public void addActivationSkill(OptionsSkillHolder holder)
	{
		_activationSkills.add(holder);
	}
	
	public void apply(L2PcInstance player)
	{
		player.sendDebugMessage("Activating option id: " + _id);
		if (hasFuncs())
		{
			player.addStatFuncs(getStatFuncs(null, player));
		}
		if (hasActiveSkill())
		{
			addSkill(player, getActiveSkill().getSkill());
			player.sendDebugMessage("Adding active skill: " + getActiveSkill());
		}
		if (hasPassiveSkill())
		{
			addSkill(player, getPassiveSkill().getSkill());
			player.sendDebugMessage("Adding passive skill: " + getPassiveSkill());
		}
		if (hasActivationSkills())
		{
			for (OptionsSkillHolder holder : _activationSkills)
			{
				player.addTriggerSkill(holder);
				player.sendDebugMessage("Adding trigger skill: " + holder);
			}
		}
		
		player.sendSkillList();
	}
	
	public void remove(L2PcInstance player)
	{
		player.sendDebugMessage("Deactivating option id: " + _id);
		if (hasFuncs())
		{
			player.removeStatsOwner(this);
		}
		if (hasActiveSkill())
		{
			player.removeSkill(getActiveSkill().getSkill(), false, false);
			player.sendDebugMessage("Removing active skill: " + getActiveSkill());
		}
		if (hasPassiveSkill())
		{
			player.removeSkill(getPassiveSkill().getSkill(), false, true);
			player.sendDebugMessage("Removing passive skill: " + getPassiveSkill());
		}
		if (hasActivationSkills())
		{
			for (OptionsSkillHolder holder : _activationSkills)
			{
				player.removeTriggerSkill(holder);
				player.sendDebugMessage("Removing trigger skill: " + holder);
			}
		}
		player.sendSkillList();
	}
	
	private final void addSkill(L2PcInstance player, L2Skill skill)
	{
		boolean updateTimeStamp = false;
		
		player.addSkill(skill, false);
		
		if (skill.isActive())
		{
			final long remainingTime = player.getSkillRemainingReuseTime(skill.getReuseHashCode());
			if (remainingTime > 0)
			{
				player.addTimeStamp(skill, remainingTime);
				player.disableSkill(skill, remainingTime);
			}
			updateTimeStamp = true;
		}
		if (updateTimeStamp)
		{
			player.sendPacket(new SkillCoolTime(player));
		}
	}
}
