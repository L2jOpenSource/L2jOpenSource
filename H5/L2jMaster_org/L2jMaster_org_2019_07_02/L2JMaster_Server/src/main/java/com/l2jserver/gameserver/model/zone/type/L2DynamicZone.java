/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.zone.type;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.zone.AbstractZoneSettings;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.model.zone.TaskZoneSettings;

/**
 * A dynamic zone? Maybe use this for interlude skills like protection field :>
 * @author durgus
 */
public class L2DynamicZone extends L2ZoneType
{
	private final L2WorldRegion _region;
	private final L2Character _owner;
	private final Skill _skill;
	
	public L2DynamicZone(L2WorldRegion region, L2Character owner, Skill skill)
	{
		super(-1);
		_region = region;
		_owner = owner;
		_skill = skill;
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);
		
		getSettings().setTask(ThreadPoolManager.getInstance().scheduleGeneral(() -> remove(), skill.getAbnormalTime() * 1000));
	}
	
	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character.isPlayer())
		{
			character.sendMessage("You have entered a temporary zone!");
		}
		if (_owner != null)
		{
			_skill.applyEffects(_owner, character);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character.isPlayer())
		{
			character.sendMessage("You have left a temporary zone!"); // TODO: Custom message?
		}
		
		if (character == _owner)
		{
			remove();
			return;
		}
		
		character.stopSkillEffects(true, _skill.getId());
	}
	
	protected void remove()
	{
		if ((getSettings().getTask() == null) || (_skill == null))
		{
			return;
		}
		
		getSettings().getTask().cancel(false);
		
		_region.removeZone(this);
		for (L2Character member : getCharactersInside())
		{
			member.stopSkillEffects(true, _skill.getId());
		}
		_owner.stopSkillEffects(true, _skill.getId());
		
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		if (character == _owner)
		{
			remove();
		}
		else
		{
			character.stopSkillEffects(true, _skill.getId());
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
		_skill.applyEffects(_owner, character);
	}
}
