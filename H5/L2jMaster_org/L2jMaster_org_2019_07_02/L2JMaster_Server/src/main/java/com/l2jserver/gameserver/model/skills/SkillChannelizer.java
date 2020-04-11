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
package com.l2jserver.gameserver.model.skills;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.ShotType;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jserver.gameserver.util.Util;

/**
 * Skill Channelizer implementation.
 * @author UnAfraid
 */
public class SkillChannelizer implements Runnable
{
	private static final Logger _log = Logger.getLogger(SkillChannelizer.class.getName());
	
	private final L2Character _channelizer;
	private List<L2Character> _channelized;
	
	private Skill _skill;
	private volatile ScheduledFuture<?> _task = null;
	
	public SkillChannelizer(L2Character channelizer)
	{
		_channelizer = channelizer;
	}
	
	public L2Character getChannelizer()
	{
		return _channelizer;
	}
	
	public List<L2Character> getChannelized()
	{
		return _channelized;
	}
	
	public boolean hasChannelized()
	{
		return _channelized != null;
	}
	
	public void startChanneling(Skill skill)
	{
		// Verify for same status.
		if (isChanneling())
		{
			_log.warning("Character: " + _channelizer + " is attempting to channel skill but he already does!");
			return;
		}
		
		// Start channeling.
		_skill = skill;
		_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, skill.getChannelingTickInitialDelay(), skill.getChannelingTickInterval());
	}
	
	public void stopChanneling()
	{
		// Verify for same status.
		if (!isChanneling())
		{
			_log.warning("Character: " + _channelizer + " is attempting to stop channel skill but he does not!");
			return;
		}
		
		// Cancel the task and unset it.
		_task.cancel(true);
		_task = null;
		
		// Cancel target channelization and unset it.
		if (_channelized != null)
		{
			for (L2Character chars : _channelized)
			{
				chars.getSkillChannelized().removeChannelizer(_skill.getChannelingSkillId(), getChannelizer());
			}
			_channelized = null;
		}
		
		// unset skill.
		_skill = null;
	}
	
	public Skill getSkill()
	{
		return _skill;
	}
	
	public boolean isChanneling()
	{
		return _task != null;
	}
	
	@Override
	public void run()
	{
		if (!isChanneling())
		{
			return;
		}
		if (_skill == null)
		{
			return;
		}
		try
		{
			if (_skill.getMpPerChanneling() > 0)
			{
				// Validate mana per tick.
				if (_channelizer.getCurrentMp() < _skill.getMpPerChanneling())
				{
					if (_channelizer.isPlayer())
					{
						_channelizer.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					}
					_channelizer.abortCast();
					return;
				}
				
				// Reduce mana per tick
				_channelizer.reduceCurrentMp(_skill.getMpPerChanneling());
			}
			
			// Apply channeling skills on the targets.
			if (_skill.getChannelingSkillId() > 0)
			{
				final Skill baseSkill = SkillData.getInstance().getSkill(_skill.getChannelingSkillId(), 1);
				if (baseSkill == null)
				{
					_log.warning(getClass().getSimpleName() + ": skill " + _skill + " couldn't find effect id skill: " + _skill.getChannelingSkillId() + " !");
					_channelizer.abortCast();
					return;
				}
				
				final List<L2Character> targetList = new ArrayList<>();
				
				for (L2Object chars : _skill.getTargetList(_channelizer))
				{
					if (chars.isCharacter())
					{
						targetList.add((L2Character) chars);
						((L2Character) chars).getSkillChannelized().addChannelizer(_skill.getChannelingSkillId(), getChannelizer());
					}
				}
				
				if (targetList.isEmpty())
				{
					return;
				}
				_channelized = targetList;
				
				for (L2Character character : _channelized)
				{
					if (!Util.checkIfInRange(_skill.getEffectRange(), _channelizer, character, true))
					{
						continue;
					}
					else if (!GeoData.getInstance().canSeeTarget(_channelizer, character))
					{
						continue;
					}
					else
					{
						final int maxSkillLevel = SkillData.getInstance().getMaxLevel(_skill.getChannelingSkillId());
						final int skillLevel = Math.min(character.getSkillChannelized().getChannerlizersSize(_skill.getChannelingSkillId()), maxSkillLevel);
						final BuffInfo info = character.getEffectList().getBuffInfoBySkillId(_skill.getChannelingSkillId());
						
						if ((info == null) || (info.getSkill().getLevel() < skillLevel))
						{
							final Skill skill = SkillData.getInstance().getSkill(_skill.getChannelingSkillId(), skillLevel);
							if (skill == null)
							{
								_log.warning(getClass().getSimpleName() + ": Non existent channeling skill requested: " + _skill);
								_channelizer.abortCast();
								return;
							}
							
							// Update PvP status
							if (character.isPlayable() && getChannelizer().isPlayer() && skill.isBad())
							{
								((L2PcInstance) getChannelizer()).updatePvPStatus(character);
							}
							
							skill.applyEffects(getChannelizer(), character);
							
							// Reduce shots.
							if (_skill.useSpiritShot())
							{
								_channelizer.setChargedShot(_channelizer.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
							}
							else
							{
								_channelizer.setChargedShot(ShotType.SOULSHOTS, false);
							}
							
							// Shots are re-charged every cast.
							_channelizer.rechargeShots(_skill.useSoulShot(), _skill.useSpiritShot());
						}
						_channelizer.broadcastPacket(new MagicSkillLaunched(_channelizer, _skill.getId(), _skill.getLevel(), character));
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.warning("Error while channelizing skill: " + _skill + " channelizer: " + _channelizer + " channelized: " + _channelized + "; " + e.getMessage());
		}
	}
}
