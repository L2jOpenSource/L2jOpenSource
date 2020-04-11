package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.ai.L2ControllableMobAI;
import com.l2jfrozen.gameserver.datatables.MobGroupTable;
import com.l2jfrozen.gameserver.datatables.sql.SpawnTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.spawn.L2GroupSpawn;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.util.random.Rnd;

/**
 * @author littlecrow
 */
public final class MobGroup
{
	private final L2NpcTemplate npcTemplate;
	private final int groupId;
	private final int maxMobCount;
	
	private List<L2ControllableMobInstance> mobs;
	
	public MobGroup(final int groupId, final L2NpcTemplate npcTemplate, final int maxMobCount)
	{
		this.groupId = groupId;
		this.npcTemplate = npcTemplate;
		this.maxMobCount = maxMobCount;
	}
	
	public int getActiveMobCount()
	{
		return getMobs().size();
	}
	
	public int getGroupId()
	{
		return groupId;
	}
	
	public int getMaxMobCount()
	{
		return maxMobCount;
	}
	
	public List<L2ControllableMobInstance> getMobs()
	{
		if (mobs == null)
		{
			mobs = new ArrayList<>();
		}
		
		return mobs;
	}
	
	public String getStatus()
	{
		try
		{
			final L2ControllableMobAI mobGroupAI = (L2ControllableMobAI) getMobs().get(0).getAI();
			
			switch (mobGroupAI.getAlternateAI())
			{
				case L2ControllableMobAI.AI_NORMAL:
					return "Idle";
				case L2ControllableMobAI.AI_FORCEATTACK:
					return "Force Attacking";
				case L2ControllableMobAI.AI_FOLLOW:
					return "Following";
				case L2ControllableMobAI.AI_CAST:
					return "Casting";
				case L2ControllableMobAI.AI_ATTACK_GROUP:
					return "Attacking Group";
				default:
					return "Idle";
			}
		}
		catch (final Exception e)
		{
			return "Unspawned";
		}
	}
	
	public L2NpcTemplate getTemplate()
	{
		return npcTemplate;
	}
	
	public boolean isGroupMember(final L2ControllableMobInstance mobInst)
	{
		for (final L2ControllableMobInstance groupMember : getMobs())
		{
			if (groupMember == null)
			{
				continue;
			}
			
			if (groupMember.getObjectId() == mobInst.getObjectId())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void spawnGroup(final int x, final int y, final int z)
	{
		if (getActiveMobCount() > 0)
		{
			return;
		}
		
		try
		{
			for (int i = 0; i < getMaxMobCount(); i++)
			{
				L2GroupSpawn spawn = new L2GroupSpawn(getTemplate());
				
				final int signX = Rnd.nextInt(2) == 0 ? -1 : 1;
				final int signY = Rnd.nextInt(2) == 0 ? -1 : 1;
				final int randX = Rnd.nextInt(MobGroupTable.RANDOM_RANGE);
				final int randY = Rnd.nextInt(MobGroupTable.RANDOM_RANGE);
				
				spawn.setLocx(x + signX * randX);
				spawn.setLocy(y + signY * randY);
				spawn.setLocz(z);
				spawn.stopRespawn();
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				getMobs().add((L2ControllableMobInstance) spawn.doGroupSpawn());
				spawn = null;
			}
		}
		catch (final ClassNotFoundException e)
		{
			// null
		}
		catch (final NoSuchMethodException e2)
		{
			// null
		}
	}
	
	public void spawnGroup(final L2PcInstance activeChar)
	{
		spawnGroup(activeChar.getX(), activeChar.getY(), activeChar.getZ());
	}
	
	public void teleportGroup(final L2PcInstance player)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				final int x = player.getX() + Rnd.nextInt(50);
				final int y = player.getY() + Rnd.nextInt(50);
				
				mobInst.teleToLocation(x, y, player.getZ(), true);
				L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
				ai.follow(player);
				ai = null;
			}
		}
	}
	
	public L2ControllableMobInstance getRandomMob()
	{
		removeDead();
		
		if (getActiveMobCount() == 0)
		{
			return null;
		}
		
		final int choice = Rnd.nextInt(getActiveMobCount());
		
		return getMobs().get(choice);
	}
	
	public void unspawnGroup()
	{
		removeDead();
		
		if (getActiveMobCount() == 0)
		{
			return;
		}
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				mobInst.deleteMe();
			}
			
			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn(), false);
		}
		
		getMobs().clear();
	}
	
	public void killGroup(final L2PcInstance activeChar)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			if (!mobInst.isDead())
			{
				mobInst.reduceCurrentHp(mobInst.getMaxHp() + 1, activeChar);
			}
			
			SpawnTable.getInstance().deleteSpawn(mobInst.getSpawn(), false);
		}
		
		getMobs().clear();
	}
	
	public void setAttackRandom()
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_NORMAL);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			ai = null;
		}
	}
	
	public void setAttackTarget(final L2Character target)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.forceAttack(target);
			ai = null;
		}
	}
	
	public void setIdleMode()
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.stop();
			ai = null;
		}
	}
	
	public void returnGroup(final L2Character activeChar)
	{
		setIdleMode();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			final int signX = Rnd.nextInt(2) == 0 ? -1 : 1;
			final int signY = Rnd.nextInt(2) == 0 ? -1 : 1;
			final int randX = Rnd.nextInt(MobGroupTable.RANDOM_RANGE);
			final int randY = Rnd.nextInt(MobGroupTable.RANDOM_RANGE);
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.move(activeChar.getX() + signX * randX, activeChar.getY() + signY * randY, activeChar.getZ());
			ai = null;
		}
	}
	
	public void setFollowMode(final L2Character character)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.follow(character);
			ai = null;
		}
	}
	
	public void setCastMode()
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setAlternateAI(L2ControllableMobAI.AI_CAST);
			ai = null;
		}
	}
	
	public void setNoMoveMode(final boolean enabled)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.setNotMoving(enabled);
			ai = null;
		}
	}
	
	protected void removeDead()
	{
		List<L2ControllableMobInstance> deadMobs = new ArrayList<>();
		
		for (L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst != null && mobInst.isDead())
			{
				deadMobs.add(mobInst);
			}
		}
		
		getMobs().removeAll(deadMobs);
		deadMobs = null;
	}
	
	public void setInvul(final boolean invulState)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst != null)
			{
				mobInst.setInvul(invulState);
			}
		}
	}
	
	public void setAttackGroup(final MobGroup otherGrp)
	{
		removeDead();
		
		for (final L2ControllableMobInstance mobInst : getMobs())
		{
			if (mobInst == null)
			{
				continue;
			}
			
			L2ControllableMobAI ai = (L2ControllableMobAI) mobInst.getAI();
			ai.forceAttackGroup(otherGrp);
			ai.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			ai = null;
		}
	}
}
