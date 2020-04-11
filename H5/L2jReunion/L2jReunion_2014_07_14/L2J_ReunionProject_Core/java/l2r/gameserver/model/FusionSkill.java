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

import java.util.concurrent.Future;

import l2r.gameserver.GeoData;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kombat, Forsaiken
 */
public final class FusionSkill
{
	protected static final Logger _log = LoggerFactory.getLogger(FusionSkill.class);
	
	protected int _skillCastRange;
	protected int _fusionId;
	protected int _fusionLevel;
	protected L2Character _caster;
	protected L2Character _target;
	protected Future<?> _geoCheckTask;
	
	public L2Character getCaster()
	{
		return _caster;
	}
	
	public L2Character getTarget()
	{
		return _target;
	}
	
	public FusionSkill(L2Character caster, L2Character target, L2Skill skill)
	{
		_skillCastRange = skill.getCastRange();
		_caster = caster;
		_target = target;
		_fusionId = skill.getTriggeredId();
		_fusionLevel = skill.getTriggeredLevel();
		
		L2Effect effect = _target.getFirstEffect(_fusionId);
		if (effect != null)
		{
			effect.increaseEffect();
		}
		else
		{
			L2Skill force = SkillData.getInstance().getInfo(_fusionId, _fusionLevel);
			if (force != null)
			{
				force.getEffects(_caster, _target, null);
			}
			else
			{
				_log.warn("Triggered skill [" + _fusionId + ";" + _fusionLevel + "] not found!");
			}
		}
		_geoCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new GeoCheckTask(), 1000, 1000);
	}
	
	public void onCastAbort()
	{
		_caster.setFusionSkill(null);
		L2Effect effect = _target.getFirstEffect(_fusionId);
		if (effect != null)
		{
			effect.decreaseForce();
		}
		
		_geoCheckTask.cancel(true);
	}
	
	public class GeoCheckTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if (!Util.checkIfInRange(_skillCastRange, _caster, _target, true))
				{
					_caster.abortCast();
				}
				
				if (!GeoData.getInstance().canSeeTarget(_caster, _target))
				{
					_caster.abortCast();
				}
			}
			catch (Exception e)
			{
				// ignore
			}
		}
	}
}