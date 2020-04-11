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
package l2r.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.QuestEventType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.knownlist.TrapKnownList;
import l2r.gameserver.model.actor.tasks.npc.trap.TrapTask;
import l2r.gameserver.model.actor.tasks.npc.trap.TrapTriggerTask;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.items.L2Weapon;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.olympiad.OlympiadGameManager;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.Quest.TrapAction;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.AbstractNpcInfo.TrapInfo;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.DecayTaskManager;

/**
 * Trap instance.
 * @author Zoey76
 */
public final class L2TrapInstance extends L2Npc
{
	private static final int TICK = 1000; // 1s
	private boolean _hasLifeTime;
	private boolean _isInArena = false;
	private boolean _isTriggered;
	private final int _lifeTime;
	private L2PcInstance _owner;
	private final List<Integer> _playersWhoDetectedMe = new ArrayList<>();
	private L2Skill _skill;
	private int _remainingTime;
	// Tasks
	private ScheduledFuture<?> _trapTask = null;
	
	public L2TrapInstance(int objectId, L2NpcTemplate template, int instanceId, int lifeTime)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2TrapInstance);
		setInstanceId(instanceId);
		setName(template.getName());
		setIsInvul(false);
		
		_isTriggered = false;
		// TODO: Manage this properly when NPC templates are complete and in XML.
		for (L2Skill skill : template.getSkills().values())
		{
			_skill = skill; // Last skill
		}
		// _skill = getTemplate().getParameters().getObject("trap_skill", SkillHolder.class);
		_hasLifeTime = lifeTime >= 0;
		_lifeTime = lifeTime != 0 ? lifeTime : 30000;
		_remainingTime = _lifeTime;
		if (_skill != null)
		{
			_trapTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TrapTask(this), TICK, TICK);
		}
	}
	
	public L2TrapInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int lifeTime)
	{
		this(objectId, template, owner.getInstanceId(), lifeTime);
		_owner = owner;
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if ((player != null) && (_isTriggered || canBeSeen(player)))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		for (L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if ((player != null) && isInsideRadius(player, radiusInKnownlist, false, false) && (_isTriggered || canBeSeen(player)))
			{
				player.sendPacket(mov);
			}
		}
	}
	
	/**
	 * Verify if the character can see the trap.
	 * @param cha the character to verify
	 * @return {@code true} if the character can see the trap, {@code false} otherwise
	 */
	public boolean canBeSeen(L2Character cha)
	{
		if ((cha != null) && _playersWhoDetectedMe.contains(cha.getObjectId()))
		{
			return true;
		}
		
		if ((_owner == null) || (cha == null))
		{
			return false;
		}
		if (cha == _owner)
		{
			return true;
		}
		
		if (cha instanceof L2PcInstance)
		{
			// observers can't see trap
			if (((L2PcInstance) cha).inObserverMode())
			{
				return false;
			}
			
			// olympiad competitors can't see trap
			if (_owner.isInOlympiadMode() && ((L2PcInstance) cha).isInOlympiadMode() && (((L2PcInstance) cha).getOlympiadSide() != _owner.getOlympiadSide()))
			{
				return false;
			}
		}
		
		if (_isInArena)
		{
			return true;
		}
		
		if (_owner.isInParty() && cha.isInParty() && (_owner.getParty().getLeaderObjectId() == cha.getParty().getLeaderObjectId()))
		{
			return true;
		}
		return false;
	}
	
	public boolean checkTarget(L2Character target)
	{
		if (!L2Skill.checkForAreaOffensiveSkills(this, target, _skill, _isInArena))
		{
			return false;
		}
		
		if (!target.isInsideRadius(this, _skill.getEffectRange(), false, false) && !target.isInsideRadius(this, _skill.getAffectRange(), false, false))
		{
			return false;
		}
		
		// observers
		if ((target instanceof L2PcInstance) && ((L2PcInstance) target).inObserverMode())
		{
			return false;
		}
		
		// olympiad own team and their summons not attacked
		if ((_owner != null) && _owner.isInOlympiadMode())
		{
			final L2PcInstance player = target.getActingPlayer();
			if ((player != null) && player.isInOlympiadMode() && (player.getOlympiadSide() == _owner.getOlympiadSide()))
			{
				return false;
			}
		}
		
		if (_isInArena)
		{
			return true;
		}
		
		// trap owned by players not attack non-flagged players
		if (_owner != null)
		{
			if (target instanceof L2Attackable)
			{
				return true;
			}
			
			final L2PcInstance player = target.getActingPlayer();
			if ((player == null) || ((player.getPvpFlag() == 0) && (player.getKarma() == 0)))
			{
				return false;
			}
			
			getOwner().updatePvPStatus();
		}
		return true;
	}
	
	@Override
	public void deleteMe()
	{
		super.deleteMe();
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	public int getKarma()
	{
		return _owner != null ? _owner.getKarma() : 0;
	}
	
	@Override
	public TrapKnownList getKnownList()
	{
		return (TrapKnownList) super.getKnownList();
	}
	
	/**
	 * Get the owner of this trap.
	 * @return the owner
	 */
	public L2PcInstance getOwner()
	{
		return _owner;
	}
	
	public byte getPvpFlag()
	{
		return _owner != null ? _owner.getPvpFlag() : 0;
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new TrapKnownList(this));
	}
	
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canBeSeen(attacker);
	}
	
	@Override
	public boolean isTrap()
	{
		return true;
	}
	
	/**
	 * Checks is triggered
	 * @return True if trap is triggered.
	 */
	public boolean isTriggered()
	{
		return _isTriggered;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInArena = isInsideZone(ZoneIdType.PVP) && !isInsideZone(ZoneIdType.SIEGE);
		_playersWhoDetectedMe.clear();
	}
	
	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || (_owner == null))
		{
			return;
		}
		
		if (_owner.isInOlympiadMode() && (target instanceof L2PcInstance) && ((L2PcInstance) target).isInOlympiadMode() && (((L2PcInstance) target).getOlympiadGameId() == _owner.getOlympiadGameId()))
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage(getOwner(), damage);
		}
		
		if (target.isInvul() && !(target instanceof L2NpcInstance))
		{
			_owner.sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
		}
		else
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
			sm.addCharName(this);
			sm.addCharName(target);
			sm.addInt(damage);
			_owner.sendPacket(sm);
		}
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (_isTriggered || canBeSeen(activeChar))
		{
			activeChar.sendPacket(new TrapInfo(this, activeChar));
		}
	}
	
	public void setDetected(L2Character detector)
	{
		if (_isInArena)
		{
			if (detector.isPlayable())
			{
				sendInfo(detector.getActingPlayer());
			}
			return;
		}
		
		if ((_owner != null) && (_owner.getPvpFlag() == 0) && (_owner.getKarma() == 0))
		{
			return;
		}
		
		_playersWhoDetectedMe.add(detector.getObjectId());
		
		if (getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, detector, TrapAction.TRAP_DETECTED);
			}
		}
		if (detector.isPlayable())
		{
			sendInfo(detector.getActingPlayer());
		}
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	/**
	 * Trigger the trap.
	 * @param target the target
	 */
	public void triggerTrap(L2Character target)
	{
		if (_trapTask != null)
		{
			_trapTask.cancel(true);
			_trapTask = null;
		}
		
		_isTriggered = true;
		broadcastPacket(new TrapInfo(this, null));
		setTarget(target);
		
		if (getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION) != null)
		{
			for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, target, TrapAction.TRAP_TRIGGERED);
			}
		}
		
		ThreadPoolManager.getInstance().scheduleGeneral(new TrapTriggerTask(this), 300);
	}
	
	public void unSummon()
	{
		if (_trapTask != null)
		{
			_trapTask.cancel(true);
			_trapTask = null;
		}
		
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
			{
				getWorldRegion().removeFromZones(this);
			}
			
			deleteMe();
		}
	}
	
	@Override
	public void updateAbnormalEffect()
	{
		
	}
	
	public boolean hasLifeTime()
	{
		return _hasLifeTime;
	}
	
	public void setHasLifeTime(boolean val)
	{
		_hasLifeTime = val;
	}
	
	public int getRemainingTime()
	{
		return _remainingTime;
	}
	
	public void setRemainingTime(int time)
	{
		_remainingTime = time;
	}
	
	public int getLifeTime()
	{
		return _lifeTime;
	}
}
