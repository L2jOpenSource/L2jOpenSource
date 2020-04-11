package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author Julian
 */
public final class L2ChestInstance extends L2MonsterInstance
{
	private volatile boolean isInteracted;
	private volatile boolean specialDrop;
	
	public L2ChestInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		isInteracted = false;
		specialDrop = false;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		isInteracted = false;
		specialDrop = false;
		setMustRewardExpSp(true);
	}
	
	public synchronized boolean isInteracted()
	{
		return isInteracted;
	}
	
	public synchronized void setInteracted()
	{
		isInteracted = true;
	}
	
	public synchronized boolean isSpecialDrop()
	{
		return specialDrop;
	}
	
	public synchronized void setSpecialDrop()
	{
		specialDrop = true;
	}
	
	@Override
	public void doItemDrop(final L2NpcTemplate npcTemplate, final L2Character lastAttacker)
	{
		int id = getTemplate().npcId;
		
		if (!specialDrop)
		{
			if (id >= 18265 && id <= 18286)
			{
				id += 3536;
			}
			else if (id == 18287 || id == 18288)
			{
				id = 21671;
			}
			else if (id == 18289 || id == 18290)
			{
				id = 21694;
			}
			else if (id == 18291 || id == 18292)
			{
				id = 21717;
			}
			else if (id == 18293 || id == 18294)
			{
				id = 21740;
			}
			else if (id == 18295 || id == 18296)
			{
				id = 21763;
			}
			else if (id == 18297 || id == 18298)
			{
				id = 21786;
			}
		}
		
		super.doItemDrop(NpcTable.getInstance().getTemplate(id), lastAttacker);
	}
	
	// cast - trap chest
	public void chestTrap(final L2Character player)
	{
		int trapSkillId = 0;
		final int rnd = Rnd.get(120);
		
		if (getTemplate().level >= 61)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139;// explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 4118;// area paralysys
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;// poison cloud
			}
			else
			{
				trapSkillId = 223;// sting
			}
		}
		else if (getTemplate().level >= 41)
		{
			if (rnd >= 90)
			{
				trapSkillId = 4139;// explosion
			}
			else if (rnd >= 60)
			{
				trapSkillId = 96;// bleed
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;// poison cloud
			}
			else
			{
				trapSkillId = 4118;// area paralysys
			}
		}
		else if (getTemplate().level >= 21)
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139;// explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96;// bleed
			}
			else if (rnd >= 20)
			{
				trapSkillId = 1167;// poison cloud
			}
			else
			{
				trapSkillId = 129;// poison
			}
		}
		else
		{
			if (rnd >= 80)
			{
				trapSkillId = 4139;// explosion
			}
			else if (rnd >= 50)
			{
				trapSkillId = 96;// bleed
			}
			else
			{
				trapSkillId = 129;// poison
			}
		}
		
		player.sendPacket(SystemMessage.sendString("There was a trap!"));
		handleCast(player, trapSkillId);
	}
	
	// <--
	// cast casse
	// <--
	private boolean handleCast(final L2Character player, final int skillId)
	{
		int skillLevel = 1;
		final byte lvl = getTemplate().level;
		if (lvl > 20 && lvl <= 40)
		{
			skillLevel = 3;
		}
		else if (lvl > 40 && lvl <= 60)
		{
			skillLevel = 5;
		}
		else if (lvl > 60)
		{
			skillLevel = 6;
		}
		
		if (player.isDead() || !player.isVisible() || !player.isInsideRadius(this, getDistanceToWatchObject(player), false, false))
		{
			return false;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
		
		if (player.getFirstEffect(skill) == null)
		{
			skill.getEffects(this, player, false, false, false);
			broadcastPacket(new MagicSkillUser(this, player, skill.getId(), skillLevel, skill.getHitTime(), 0));
			skill = null;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isMovementDisabled()
	{
		if (super.isMovementDisabled())
		{
			return true;
		}
		
		if (isInteracted())
		{
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}
