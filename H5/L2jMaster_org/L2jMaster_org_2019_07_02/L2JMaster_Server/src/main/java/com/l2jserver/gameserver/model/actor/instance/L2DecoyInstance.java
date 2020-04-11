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
package com.l2jserver.gameserver.model.actor.instance;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Decoy;
import com.l2jserver.gameserver.model.actor.knownlist.DecoyKnownList;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;

public class L2DecoyInstance extends L2Decoy
{
	private int _totalLifeTime;
	private int _timeRemaining;
	private Future<?> _DecoyLifeTask;
	private Future<?> _HateSpam;
	
	/**
	 * Creates a decoy.
	 * @param template the decoy NPC template
	 * @param owner the owner
	 * @param totalLifeTime the total life time
	 */
	public L2DecoyInstance(L2NpcTemplate template, L2PcInstance owner, int totalLifeTime)
	{
		super(template, owner);
		setInstanceType(InstanceType.L2DecoyInstance);
		_totalLifeTime = totalLifeTime;
		_timeRemaining = _totalLifeTime;
		int skilllevel = getTemplate().getDisplayId() - 13070;
		_DecoyLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DecoyLifetime(getOwner(), this), 1000, 1000);
		_HateSpam = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new HateSpam(this, SkillData.getInstance().getSkill(5272, skilllevel)), 2000, 5000);
	}
	
	@Override
	public boolean doDie(L2Character killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (_HateSpam != null)
		{
			_HateSpam.cancel(true);
			_HateSpam = null;
		}
		_totalLifeTime = 0;
		DecayTaskManager.getInstance().add(this);
		return true;
	}
	
	@Override
	public DecoyKnownList getKnownList()
	{
		return (DecoyKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new DecoyKnownList(this));
	}
	
	static class DecoyLifetime implements Runnable
	{
		private static final Logger LOG = LoggerFactory.getLogger(DecoyLifetime.class);
		private final L2PcInstance _activeChar;
		private final L2DecoyInstance _Decoy;
		
		DecoyLifetime(L2PcInstance activeChar, L2DecoyInstance Decoy)
		{
			_activeChar = activeChar;
			_Decoy = Decoy;
		}
		
		@Override
		public void run()
		{
			try
			{
				_Decoy.decTimeRemaining(1000);
				double newTimeRemaining = _Decoy.getTimeRemaining();
				if (newTimeRemaining < 0)
				{
					_Decoy.unSummon(_activeChar);
				}
			}
			catch (Exception e)
			{
				LOG.error("Decoy Error: {}", e);
			}
		}
	}
	
	private static class HateSpam implements Runnable
	{
		private static final Logger LOG = LoggerFactory.getLogger(HateSpam.class);
		private final L2DecoyInstance _activeChar;
		private final Skill _skill;
		
		HateSpam(L2DecoyInstance activeChar, Skill Hate)
		{
			_activeChar = activeChar;
			_skill = Hate;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.setTarget(_activeChar);
				_activeChar.doCast(_skill);
			}
			catch (Throwable e)
			{
				LOG.error("Decoy Error: {}", e);
			}
		}
	}
	
	@Override
	public void unSummon(L2PcInstance owner)
	{
		if (_DecoyLifeTask != null)
		{
			_DecoyLifeTask.cancel(true);
			_DecoyLifeTask = null;
		}
		if (_HateSpam != null)
		{
			_HateSpam.cancel(true);
			_HateSpam = null;
		}
		super.unSummon(owner);
	}
	
	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}
	
	public int getTimeRemaining()
	{
		return _timeRemaining;
	}
	
	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}
}
