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
package l2r.gameserver.datatables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

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
	
	public Map<Integer, Map<Integer, Map<Integer, List<SummonEffect>>>> getServitorEffectsOwner()
	{
		return _servitorEffects;
	}
	
	public Map<Integer, List<SummonEffect>> getServitorEffects(L2PcInstance owner)
	{
		final Map<Integer, Map<Integer, List<SummonEffect>>> servitorMap = _servitorEffects.get(owner.getObjectId());
		if (servitorMap == null)
		{
			return null;
		}
		return servitorMap.get(owner.getClassIndex());
	}
	
	/** Pets **/
	private final Map<Integer, List<SummonEffect>> _petEffects = new HashMap<>(); // key: petItemObjectId, value: Effects list
	
	public Map<Integer, List<SummonEffect>> getPetEffects()
	{
		return _petEffects;
	}
	
	public class SummonEffect
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
