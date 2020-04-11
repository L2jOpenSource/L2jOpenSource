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
package l2r.gameserver.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Env;

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
	private final Map<Integer, Map<Integer, Map<Integer, List<SummonEffect>>>> _servitorEffects = new HashMap<>();
	
	private Map<Integer, List<SummonEffect>> getServitorEffects(L2PcInstance owner)
	{
		final Map<Integer, Map<Integer, List<SummonEffect>>> servitorMap = _servitorEffects.get(owner.getObjectId());
		if (servitorMap == null)
		{
			return null;
		}
		return servitorMap.get(owner.getClassIndex());
	}
	
	private List<SummonEffect> getServitorEffects(L2PcInstance owner, int referenceSkill)
	{
		return containsOwner(owner) ? getServitorEffects(owner).get(referenceSkill) : null;
	}
	
	private boolean containsOwner(L2PcInstance owner)
	{
		return _servitorEffects.getOrDefault(owner.getObjectId(), Collections.emptyMap()).containsKey(owner.getClassIndex());
	}
	
	private void removeEffects(List<SummonEffect> effects, int skillId)
	{
		if ((effects != null) && !effects.isEmpty())
		{
			for (SummonEffect effect : effects)
			{
				final L2Skill skill = effect.getSkill();
				if ((skill != null) && (skill.getId() == skillId))
				{
					effects.remove(effect);
				}
			}
		}
	}
	
	private void applyEffects(L2Summon summon, List<SummonEffect> summonEffects)
	{
		if (summonEffects == null)
		{
			return;
		}
		
		for (SummonEffect se : summonEffects)
		{
			if ((se != null) && se.getSkill().hasEffects())
			{
				Env env = new Env();
				env.setCharacter(summon);
				env.setTarget(summon);
				env.setSkill(se.getSkill());
				L2Effect ef;
				for (EffectTemplate et : se.getSkill().getEffectTemplates())
				{
					ef = et.getEffect(env);
					if (ef != null)
					{
						ef.setCount(se.getEffectCount());
						ef.setFirstTime(se.getEffectCurTime());
						ef.scheduleEffect();
					}
				}
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
			getServitorEffects(owner).getOrDefault(referenceSkill, Collections.emptyList()).clear();
		}
	}
	
	public void addServitorEffect(L2PcInstance owner, int referenceSkill, L2Skill skill, int effectCount, int effectTime)
	{
		_servitorEffects.putIfAbsent(owner.getObjectId(), new HashMap<Integer, Map<Integer, List<SummonEffect>>>());
		_servitorEffects.get(owner.getObjectId()).putIfAbsent(owner.getClassIndex(), new HashMap<Integer, List<SummonEffect>>());
		getServitorEffects(owner).putIfAbsent(referenceSkill, new CopyOnWriteArrayList<SummonEffect>());
		getServitorEffects(owner).get(referenceSkill).add(new SummonEffect(skill, effectCount, effectTime));
	}
	
	public void removeServitorEffects(L2PcInstance owner, int referenceSkill, int skillId)
	{
		removeEffects(getServitorEffects(owner, referenceSkill), skillId);
	}
	
	public void applyServitorEffects(L2ServitorInstance l2ServitorInstance, L2PcInstance owner, int referenceSkill)
	{
		applyEffects(l2ServitorInstance, getServitorEffects(owner, referenceSkill));
	}
	
	/** Pets **/
	private final Map<Integer, List<SummonEffect>> _petEffects = new ConcurrentHashMap<>(); // key: petItemObjectId, value: Effects list
	
	public void addPetEffect(int controlObjectId, L2Skill skill, int effectCount, int effectTime)
	{
		_petEffects.computeIfAbsent(controlObjectId, k -> new CopyOnWriteArrayList<>()).add(new SummonEffect(skill, effectCount, effectTime));
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
		_petEffects.getOrDefault(controlObjectId, Collections.emptyList()).clear();
	}
	
	public void removePetEffects(int controlObjectId, int skillId)
	{
		removeEffects(_petEffects.get(controlObjectId), skillId);
	}
	
	private class SummonEffect
	{
		L2Skill _skill;
		int _effectCount;
		int _effectCurTime;
		
		public SummonEffect(L2Skill skill, int effectCount, int effectCurTime)
		{
			_skill = skill;
			_effectCount = effectCount;
			_effectCurTime = effectCurTime;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
		
		public int getEffectCount()
		{
			return _effectCount;
		}
		
		public int getEffectCurTime()
		{
			return _effectCurTime;
		}
	}
	
	public static SummonEffectsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final SummonEffectsTable _instance = new SummonEffectsTable();
	}
}
