
package com.l2jfrozen.gameserver.model.zone.type;

import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

public class L2PoisonZone extends L2ZoneType
{
	protected final Logger LOGGER = Logger.getLogger(L2PoisonZone.class);
	protected int skillId;
	private int chance;
	private int initialDelay;
	protected int skillLvl;
	private int reuse;
	private boolean enabled;
	private String target;
	private Future<?> task;
	
	public L2PoisonZone(final int id)
	{
		super(id);
		skillId = 4070;
		skillLvl = 1;
		chance = 100;
		initialDelay = 0;
		reuse = 30000;
		enabled = true;
		target = "pc";
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		switch (name)
		{
			case "skillId":
				skillId = Integer.parseInt(value);
				break;
			case "skillLvl":
				skillLvl = Integer.parseInt(value);
				break;
			case "chance":
				chance = Integer.parseInt(value);
				break;
			case "initialDelay":
				initialDelay = Integer.parseInt(value);
				break;
			case "default_enabled":
				enabled = Boolean.parseBoolean(value);
				break;
			case "target":
				target = String.valueOf(value);
				break;
			case "reuse":
				reuse = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}
	
	@Override
	protected void onEnter(final L2Character character)
	{
		if ((character instanceof L2PlayableInstance && target.equalsIgnoreCase("pc") || character instanceof L2PcInstance && target.equalsIgnoreCase("pc_only") || character instanceof L2MonsterInstance && target.equalsIgnoreCase("npc")) && task == null)
		{
			task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplySkill(/* this */), initialDelay, reuse);
		}
	}
	
	@Override
	protected void onExit(final L2Character character)
	{
		if (characterList.isEmpty() && task != null)
		{
			task.cancel(true);
			task = null;
		}
	}
	
	public L2Skill getSkill()
	{
		return SkillTable.getInstance().getInfo(skillId, skillLvl);
	}
	
	public String getTargetType()
	{
		return target;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public int getChance()
	{
		return chance;
	}
	
	public void setZoneEnabled(final boolean val)
	{
		enabled = val;
	}
	
	class ApplySkill implements Runnable
	{
		@Override
		public void run()
		{
			if (isEnabled())
			{
				for (final L2Character temp : characterList.values())
				{
					if (temp != null && !temp.isDead())
					{
						if ((temp instanceof L2PlayableInstance && getTargetType().equalsIgnoreCase("pc") || temp instanceof L2PcInstance && getTargetType().equalsIgnoreCase("pc_only") || temp instanceof L2MonsterInstance && getTargetType().equalsIgnoreCase("npc")) && Rnd.get(100) < getChance())
						{
							L2Skill skill = null;
							if ((skill = getSkill()) == null)
							{
								LOGGER.warn("ATTENTION: error on zone with id " + getZoneId());
								LOGGER.warn("Skill " + skillId + "," + skillLvl + " not present between skills");
							}
							else
							{
								skill.getEffects(temp, temp, false, false, false);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onDieInside(final L2Character l2character)
	{
	}
	
	@Override
	public void onReviveInside(final L2Character l2character)
	{
	}
}
