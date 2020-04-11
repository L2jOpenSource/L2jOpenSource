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
package l2r.gameserver.ai;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ACTIVE;
import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.Collection;
import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.MobGroup;
import l2r.gameserver.model.MobGroupTable;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Character.AIAccessor;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2ControllableMobInstance;
import l2r.gameserver.model.actor.instance.L2DoorInstance;
import l2r.gameserver.model.actor.instance.L2NpcInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * AI for controllable mobs
 * @author littlecrow
 */
public class L2ControllableMobAI extends L2AttackableAI
{
	public static final int AI_IDLE = 1;
	public static final int AI_NORMAL = 2;
	public static final int AI_FORCEATTACK = 3;
	public static final int AI_FOLLOW = 4;
	public static final int AI_CAST = 5;
	public static final int AI_ATTACK_GROUP = 6;
	
	private int _alternateAI;
	
	private boolean _isThinking; // to prevent thinking recursively
	private boolean _isNotMoving;
	
	private L2Character _forcedTarget;
	private MobGroup _targetGroup;
	
	protected void thinkFollow()
	{
		L2Attackable me = (L2Attackable) _actor;
		
		if (!Util.checkIfInRange(MobGroupTable.FOLLOW_RANGE, me, getForcedTarget(), true))
		{
			int signX = (Rnd.nextInt(2) == 0) ? -1 : 1;
			int signY = (Rnd.nextInt(2) == 0) ? -1 : 1;
			int randX = Rnd.nextInt(MobGroupTable.FOLLOW_RANGE);
			int randY = Rnd.nextInt(MobGroupTable.FOLLOW_RANGE);
			
			moveTo(getForcedTarget().getX() + (signX * randX), getForcedTarget().getY() + (signY * randY), getForcedTarget().getZ());
		}
	}
	
	@Override
	protected void onEvtThink()
	{
		if (isThinking())
		{
			return;
		}
		
		setThinking(true);
		
		try
		{
			switch (getAlternateAI())
			{
				case AI_IDLE:
					if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
					{
						setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					}
					break;
				case AI_FOLLOW:
					thinkFollow();
					break;
				case AI_CAST:
					thinkCast();
					break;
				case AI_FORCEATTACK:
					thinkForceAttack();
					break;
				case AI_ATTACK_GROUP:
					thinkAttackGroup();
					break;
				default:
					if (getIntention() == AI_INTENTION_ACTIVE)
					{
						thinkActive();
					}
					else if (getIntention() == AI_INTENTION_ATTACK)
					{
						thinkAttack();
					}
					break;
			}
		}
		finally
		{
			setThinking(false);
		}
	}
	
	protected void thinkCast()
	{
		L2Attackable npc = (L2Attackable) _actor;
		if ((getAttackTarget() == null) || getAttackTarget().isAlikeDead())
		{
			setAttackTarget(findNextRndTarget());
			clientStopMoving(null);
		}
		
		if (getAttackTarget() == null)
		{
			return;
		}
		
		npc.setTarget(getAttackTarget());
		
		if (!_actor.isMuted())
		{
			int max_range = 0;
			// check distant skills
			
			for (L2Skill sk : _actor.getAllSkills())
			{
				if (Util.checkIfInRange(sk.getCastRange(), _actor, getAttackTarget(), true) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_accessor.doCast(sk);
					return;
				}
				
				max_range = Math.max(max_range, sk.getCastRange());
			}
			
			if (!isNotMoving())
			{
				moveToPawn(getAttackTarget(), max_range);
			}
			
			return;
		}
	}
	
	protected void thinkAttackGroup()
	{
		L2Character target = getForcedTarget();
		if ((target == null) || target.isAlikeDead())
		{
			// try to get next group target
			setForcedTarget(findNextGroupTarget());
			clientStopMoving(null);
		}
		
		if (target == null)
		{
			return;
		}
		
		_actor.setTarget(target);
		// as a response, we put the target in a forcedattack mode
		L2ControllableMobInstance theTarget = (L2ControllableMobInstance) target;
		L2ControllableMobAI ctrlAi = (L2ControllableMobAI) theTarget.getAI();
		ctrlAi.forceAttack(_actor);
		
		double dist2 = _actor.getPlanDistanceSq(target.getX(), target.getY());
		int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + target.getTemplate().getCollisionRadius();
		int max_range = range;
		
		if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
		{
			// check distant skills
			for (L2Skill sk : _actor.getAllSkills())
			{
				int castRange = sk.getCastRange();
				
				if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_accessor.doCast(sk);
					return;
				}
				
				max_range = Math.max(max_range, castRange);
			}
			
			if (!isNotMoving())
			{
				moveToPawn(target, range);
			}
			
			return;
		}
		_accessor.doAttack(target);
	}
	
	protected void thinkForceAttack()
	{
		if ((getForcedTarget() == null) || getForcedTarget().isAlikeDead())
		{
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			setAlternateAI(AI_IDLE);
		}
		
		_actor.setTarget(getForcedTarget());
		double dist2 = _actor.getPlanDistanceSq(getForcedTarget().getX(), getForcedTarget().getY());
		int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + getForcedTarget().getTemplate().getCollisionRadius();
		int max_range = range;
		
		if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
		{
			// check distant skills
			for (L2Skill sk : _actor.getAllSkills())
			{
				int castRange = sk.getCastRange();
				
				if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
				{
					_accessor.doCast(sk);
					return;
				}
				
				max_range = Math.max(max_range, castRange);
			}
			
			if (!isNotMoving())
			{
				moveToPawn(getForcedTarget(), _actor.getPhysicalAttackRange()/* range */);
			}
			
			return;
		}
		
		_accessor.doAttack(getForcedTarget());
	}
	
	protected void thinkAttack()
	{
		if ((getAttackTarget() == null) || getAttackTarget().isAlikeDead())
		{
			if (getAttackTarget() != null)
			{
				// stop hating
				L2Attackable npc = (L2Attackable) _actor;
				npc.stopHating(getAttackTarget());
			}
			
			setIntention(AI_INTENTION_ACTIVE);
		}
		else
		{
			// notify aggression
			if (((L2Npc) _actor).getFactionId() != null)
			{
				String faction_id = ((L2Npc) _actor).getFactionId();
				
				Collection<L2Object> objs = _actor.getKnownList().getKnownObjects().values();
				
				for (L2Object obj : objs)
				{
					if (!(obj instanceof L2Npc))
					{
						continue;
					}
					
					L2Npc npc = (L2Npc) obj;
					
					if (!faction_id.equals(npc.getFactionId()))
					{
						continue;
					}
					
					if (_actor.isInsideRadius(npc, npc.getFactionRange(), false, true) && (Math.abs(getAttackTarget().getZ() - npc.getZ()) < 200))
					{
						npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
					}
				}
			}
			
			_actor.setTarget(getAttackTarget());
			double dist2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
			int range = _actor.getPhysicalAttackRange() + _actor.getTemplate().getCollisionRadius() + getAttackTarget().getTemplate().getCollisionRadius();
			int max_range = range;
			
			if (!_actor.isMuted() && (dist2 > ((range + 20) * (range + 20))))
			{
				// check distant skills
				for (L2Skill sk : _actor.getAllSkills())
				{
					int castRange = sk.getCastRange();
					
					if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)))
					{
						_accessor.doCast(sk);
						return;
					}
					
					max_range = Math.max(max_range, castRange);
				}
				
				moveToPawn(getAttackTarget(), range);
				return;
			}
			
			// Force mobs to attack anybody if confused.
			L2Character hated;
			
			if (_actor.isConfused())
			{
				hated = findNextRndTarget();
			}
			else
			{
				hated = getAttackTarget();
			}
			
			if (hated == null)
			{
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}
			
			if (hated != getAttackTarget())
			{
				setAttackTarget(hated);
			}
			
			if (!_actor.isMuted() && (Rnd.nextInt(5) == 3))
			{
				for (L2Skill sk : _actor.getAllSkills())
				{
					int castRange = sk.getCastRange();
					
					if (((castRange * castRange) >= dist2) && !_actor.isSkillDisabled(sk) && (_actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)))
					{
						_accessor.doCast(sk);
						return;
					}
				}
			}
			
			_accessor.doAttack(getAttackTarget());
		}
	}
	
	private void thinkActive()
	{
		setAttackTarget(findNextRndTarget());
		L2Character hated;
		
		if (_actor.isConfused())
		{
			hated = findNextRndTarget();
		}
		else
		{
			hated = getAttackTarget();
		}
		
		if (hated != null)
		{
			_actor.setRunning();
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
		}
	}
	
	private boolean autoAttackCondition(L2Character target)
	{
		if ((target == null) || !(_actor instanceof L2Attackable))
		{
			return false;
		}
		L2Attackable me = (L2Attackable) _actor;
		
		if ((target instanceof L2NpcInstance) || (target instanceof L2DoorInstance))
		{
			return false;
		}
		
		if (target.isAlikeDead() || !me.isInsideRadius(target, me.getAggroRange(), false, false) || (Math.abs(_actor.getZ() - target.getZ()) > 100))
		{
			return false;
		}
		
		// Check if the target isn't invulnerable
		if (target.isInvul())
		{
			return false;
		}
		
		// Spawn protection (only against mobs)
		if ((target instanceof L2PcInstance) && ((L2PcInstance) target).isSpawnProtected())
		{
			return false;
		}
		
		// Check if the target is a L2Playable
		if (target.isPlayable())
		{
			// Check if the target isn't in silent move mode
			if (((L2Playable) target).isSilentMoving())
			{
				return false;
			}
		}
		
		if (target instanceof L2Npc)
		{
			return false;
		}
		
		return me.isAggressive();
	}
	
	private L2Character findNextRndTarget()
	{
		int aggroRange = ((L2Attackable) _actor).getAggroRange();
		L2Attackable npc = (L2Attackable) _actor;
		int npcX, npcY, targetX, targetY;
		double dy, dx;
		double dblAggroRange = aggroRange * aggroRange;
		
		List<L2Character> potentialTarget = new FastList<>();
		
		Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		for (L2Object obj : objs)
		{
			if (!(obj instanceof L2Character))
			{
				continue;
			}
			
			npcX = npc.getX();
			npcY = npc.getY();
			targetX = obj.getX();
			targetY = obj.getY();
			
			dx = npcX - targetX;
			dy = npcY - targetY;
			
			if (((dx * dx) + (dy * dy)) > dblAggroRange)
			{
				continue;
			}
			
			L2Character target = (L2Character) obj;
			
			if (autoAttackCondition(target))
			{
				potentialTarget.add(target);
			}
		}
		
		if (potentialTarget.isEmpty())
		{
			return null;
		}
		
		// we choose a random target
		int choice = Rnd.nextInt(potentialTarget.size());
		L2Character target = potentialTarget.get(choice);
		
		return target;
	}
	
	private L2ControllableMobInstance findNextGroupTarget()
	{
		return getGroupTarget().getRandomMob();
	}
	
	public L2ControllableMobAI(AIAccessor accessor)
	{
		super(accessor);
		setAlternateAI(AI_IDLE);
	}
	
	public int getAlternateAI()
	{
		return _alternateAI;
	}
	
	public void setAlternateAI(int _alternateai)
	{
		_alternateAI = _alternateai;
	}
	
	public void forceAttack(L2Character target)
	{
		setAlternateAI(AI_FORCEATTACK);
		setForcedTarget(target);
	}
	
	public void forceAttackGroup(MobGroup group)
	{
		setForcedTarget(null);
		setGroupTarget(group);
		setAlternateAI(AI_ATTACK_GROUP);
	}
	
	public void stop()
	{
		setAlternateAI(AI_IDLE);
		clientStopMoving(null);
	}
	
	public void move(int x, int y, int z)
	{
		moveTo(x, y, z);
	}
	
	public void follow(L2Character target)
	{
		setAlternateAI(AI_FOLLOW);
		setForcedTarget(target);
	}
	
	public boolean isThinking()
	{
		return _isThinking;
	}
	
	public boolean isNotMoving()
	{
		return _isNotMoving;
	}
	
	public void setNotMoving(boolean isNotMoving)
	{
		_isNotMoving = isNotMoving;
	}
	
	public void setThinking(boolean isThinking)
	{
		_isThinking = isThinking;
	}
	
	private L2Character getForcedTarget()
	{
		return _forcedTarget;
	}
	
	private MobGroup getGroupTarget()
	{
		return _targetGroup;
	}
	
	private void setForcedTarget(L2Character forcedTarget)
	{
		_forcedTarget = forcedTarget;
	}
	
	private void setGroupTarget(MobGroup targetGroup)
	{
		_targetGroup = targetGroup;
	}
}
