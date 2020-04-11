package com.l2jfrozen.gameserver.model.zone.type;

import java.util.concurrent.Future;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2WorldRegion;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * A dynamic zone? Maybe use this for interlude skills like protection field :>
 * @author durgus
 */
public class L2DynamicZone extends L2ZoneType
{
	private final L2WorldRegion region;
	private final L2Character owner;
	private Future<?> task;
	private final L2Skill skill;
	
	protected void setTask(final Future<?> task)
	{
		this.task = task;
	}
	
	public L2DynamicZone(final L2WorldRegion region, final L2Character owner, final L2Skill skill)
	{
		super(-1);
		this.region = region;
		this.owner = owner;
		this.skill = skill;
		
		setTask(ThreadPoolManager.getInstance().scheduleGeneral(() -> remove(), skill.getBuffDuration()));
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		try
		{
			if (character instanceof L2PcInstance)
			{
				((L2PcInstance) character).sendMessage("You have entered a temporary zone!");
			}
			
			skill.getEffects(owner, character, false, false, false);
		}
		catch (final NullPointerException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendMessage("You have left a temporary zone!");
		}
		
		if (character == owner)
		{
			remove();
			return;
		}
		character.stopSkillEffects(skill.getId());
	}
	
	protected void remove()
	{
		if (task == null)
		{
			return;
		}
		
		task.cancel(false);
		task = null;
		
		region.removeZone(this);
		
		for (final L2Character member : characterList.values())
		{
			try
			{
				member.stopSkillEffects(skill.getId());
			}
			catch (final NullPointerException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		owner.stopSkillEffects(skill.getId());
		
	}
	
	@Override
	protected void onDieInside(final L2Character character)
	{
		if (character == owner)
		{
			remove();
		}
		else
		{
			character.stopSkillEffects(skill.getId());
		}
	}
	
	@Override
	protected void onReviveInside(final L2Character character)
	{
		skill.getEffects(owner, character, false, false, false);
	}
	
}
