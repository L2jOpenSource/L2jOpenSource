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
package com.l2jserver.gameserver.data.sql.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author Nyaran
 */
public class SummonEffectsTable
{
	/** Servitors **/
	// Map tree
	// -> key: charObjectId, value: classIndex Map
	// --> key: classIndex, value: servitors Map
	// ---> key: servitorSkillId, value: Effects list
	private final Map<Integer, Map<Integer, Map<Integer, Map<Integer, SummonEffect>>>> _servitorEffects = new HashMap<>();
	/** Pets **/
	// key: petItemObjectId, value: Effects list
	private final Map<Integer, Map<Integer, SummonEffect>> _petEffects = new HashMap<>();
	
	private Map<Integer, Map<Integer, SummonEffect>> getServitorEffects(L2PcInstance owner)
	{
		final Map<Integer, Map<Integer, Map<Integer, SummonEffect>>> servitorMap = _servitorEffects.get(owner.getObjectId());
		if (servitorMap == null)
		{
			return null;
		}
		return servitorMap.get(owner.getClassIndex());
	}
	
	private Map<Integer, SummonEffect> getServitorEffects(L2PcInstance owner, int referenceSkill)
	{
		return containsOwner(owner) ? getServitorEffects(owner).get(referenceSkill) : null;
	}
	
	private boolean containsOwner(L2PcInstance owner)
	{
		return _servitorEffects.getOrDefault(owner.getObjectId(), Collections.emptyMap()).containsKey(owner.getClassIndex());
	}
	
	private void removeEffects(Map<Integer, SummonEffect> map, int skillId)
	{
		if (map != null)
		{
			map.remove(skillId);
		}
	}
	
	private void applyEffects(L2Summon summon, Map<Integer, SummonEffect> map)
	{
		if (map == null)
		{
			return;
		}
		for (SummonEffect se : map.values())
		{
			if (se != null)
			{
				se.getSkill().applyEffects(summon, summon, false, se.getEffectCurTime());
			}
		}
	}
	
	public boolean containsSkill(L2PcInstance owner, int referenceSkill)
	{
		return containsOwner(owner) && getServitorEffects(owner).containsKey(referenceSkill);
	}
	
	public void clearServitorEffects(L2PcInstance owner, int referenceSkill)
	{
		if (containsOwner(owner))
		{
			getServitorEffects(owner).getOrDefault(referenceSkill, Collections.emptyMap()).clear();
		}
	}
	
	public void addServitorEffect(L2PcInstance owner, int referenceSkill, Skill skill, int effectCurTime)
	{
		_servitorEffects.putIfAbsent(owner.getObjectId(), new HashMap<Integer, Map<Integer, Map<Integer, SummonEffect>>>());
		_servitorEffects.get(owner.getObjectId()).putIfAbsent(owner.getClassIndex(), new HashMap<Integer, Map<Integer, SummonEffect>>());
		getServitorEffects(owner).putIfAbsent(referenceSkill, new ConcurrentHashMap<Integer, SummonEffect>());
		getServitorEffects(owner).get(referenceSkill).put(skill.getId(), new SummonEffect(skill, effectCurTime));
	}
	
	public void removeServitorEffects(L2PcInstance owner, int referenceSkill, int skillId)
	{
		removeEffects(getServitorEffects(owner, referenceSkill), skillId);
	}
	
	public void applyServitorEffects(L2ServitorInstance servitor, L2PcInstance owner, int referenceSkill)
	{
		applyEffects(servitor, getServitorEffects(owner, referenceSkill));
	}
	
	public void addPetEffect(int controlObjectId, Skill skill, int effectCurTime)
	{
		_petEffects.computeIfAbsent(controlObjectId, k -> new ConcurrentHashMap<>()).put(skill.getId(), new SummonEffect(skill, effectCurTime));
	}
	
	public boolean containsPetId(int controlObjectId)
	{
		return _petEffects.containsKey(controlObjectId);
	}
	
	public void applyPetEffects(L2PetInstance l2PetInstance, int controlObjectId)
	{
		applyEffects(l2PetInstance, _petEffects.get(controlObjectId));
	}
	
	public void clearPetEffects(int controlObjectId)
	{
		final Map<Integer, SummonEffect> effects = _petEffects.get(controlObjectId);
		if (effects != null)
		{
			effects.clear();
		}
	}
	
	public void removePetEffects(int controlObjectId, int skillId)
	{
		removeEffects(_petEffects.get(controlObjectId), skillId);
	}
	
	private class SummonEffect
	{
		Skill _skill;
		int _effectCurTime;
		
		public SummonEffect(Skill skill, int effectCurTime)
		{
			_skill = skill;
			_effectCurTime = effectCurTime;
		}
		
		public Skill getSkill()
		{
			return _skill;
		}
		
		public int getEffectCurTime()
		{
			return _effectCurTime;
		}
	}
	
	public static SummonEffectsTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonEffectsTable INSTANCE = new SummonEffectsTable();
	}
}
