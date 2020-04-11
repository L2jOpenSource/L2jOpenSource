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
package l2r.gameserver.ai;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2DefenderInstance;

/**
 * @author BiggBoss
 */
public final class L2SpecialSiegeGuardAI extends L2SiegeGuardAI
{
	private final List<Integer> _allied = new ArrayList<>();
	
	/**
	 * @param creature
	 */
	public L2SpecialSiegeGuardAI(L2DefenderInstance creature)
	{
		super(creature);
	}
	
	public List<Integer> getAlly()
	{
		return _allied;
	}
	
	@Override
	protected boolean autoAttackCondition(L2Character target)
	{
		if (_allied.contains(target.getObjectId()))
		{
			return false;
		}
		
		return target.isPlayable();
	}
	
	/**
	 * Manage AI thinking actions of a L2Attackable.
	 */
	@Override
	protected void onEvtThink()
	{
		// if(getIntention() != AI_INTENTION_IDLE && (!_actor.isVisible() || !_actor.hasAI() || !_actor.isKnownPlayers()))
		// setIntention(AI_INTENTION_IDLE);
		
		// Check if the thinking action is already in progress
		if (_thinking || _actor.isCastingNow() || _actor.isAllSkillsDisabled())
		{
			return;
		}
		
		// Start thinking action
		_thinking = true;
		
		try
		{
			// Manage AI thinks of a L2Attackable
			switch (getIntention())
			{
				case AI_INTENTION_IDLE:
				case AI_INTENTION_ACTIVE:
				case AI_INTENTION_MOVE_TO:
					thinkActive();
					break;
				case AI_INTENTION_ATTACK:
					thinkAttack();
					break;
			}
		}
		finally
		{
			// Stop thinking action
			_thinking = false;
		}
	}
}
