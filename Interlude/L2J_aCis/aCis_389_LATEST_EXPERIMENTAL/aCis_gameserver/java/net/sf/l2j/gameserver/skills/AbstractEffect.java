package net.sf.l2j.gameserver.skills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.EffectFlag;
import net.sf.l2j.gameserver.enums.skills.EffectState;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbnormalStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.skills.basefuncs.FuncTemplate;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;

public abstract class AbstractEffect
{
	private EffectState _state;
	
	private final EffectTemplate _template;
	private final L2Skill _skill;
	private final Creature _effected;
	private final Creature _effector;
	private final boolean _isHerbEffect;
	
	private int _count;
	
	private final int _period;
	private long _periodStartTime;
	private int _periodFirstTime;
	
	private boolean _isSelfEffect;
	private boolean _cantUpdateAnymore;
	
	private ScheduledFuture<?> _currentFuture;
	
	private boolean _inUse = false;
	private boolean _startConditionsCorrect = true;
	
	protected AbstractEffect(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		_state = EffectState.CREATED;
		
		_template = template;
		_skill = skill;
		_effected = effected;
		_effector = effector;
		_isHerbEffect = _skill.getName().contains("Herb");
		
		_count = template.getCounter();
		
		// Support for retail herbs duration when _effected has a Summon.
		int temp = template.getPeriod();
		
		if (_skill.getId() > 2277 && _skill.getId() < 2286)
		{
			if (_effected instanceof Servitor || (_effected instanceof Player && ((Player) _effected).getSummon() != null))
				temp /= 2;
		}
		
		_period = temp;
		_periodStartTime = System.currentTimeMillis();
		_periodFirstTime = 0;
	}
	
	/**
	 * @return The {@link EffectType} associated to this {@link AbstractEffect}.
	 */
	public abstract EffectType getEffectType();
	
	/**
	 * Fire an event, happening every "tick".
	 * @return True if the continuation of this {@link AbstractEffect} occurs.
	 */
	public abstract boolean onActionTime();
	
	@Override
	public String toString()
	{
		return "AbstractEffect [_skill=" + _skill.getName() + ", _state=" + _state + ", _period=" + _period + "]";
	}
	
	public EffectTemplate getTemplate()
	{
		return _template;
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final Creature getEffected()
	{
		return _effected;
	}
	
	public final Creature getEffector()
	{
		return _effector;
	}
	
	public boolean isHerbEffect()
	{
		return _isHerbEffect;
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public void setCount(int newCount)
	{
		_count = Math.min(newCount, _template.getCounter());
	}
	
	public int getPeriod()
	{
		return _period;
	}
	
	public void setFirstTime(int newFirstTime)
	{
		_periodFirstTime = Math.min(newFirstTime, _period);
		_periodStartTime = System.currentTimeMillis() - _periodFirstTime * 1000;
	}
	
	public int getTime()
	{
		return (int) ((System.currentTimeMillis() - _periodStartTime) / 1000);
	}
	
	/**
	 * @return The elapsed time of the task in seconds.
	 */
	public int getTaskTime()
	{
		if (_count == _template.getCounter())
			return 0;
		
		return (Math.abs(_count - _template.getCounter() + 1) * _period) + getTime() + 1;
	}
	
	public boolean getInUse()
	{
		return _inUse;
	}
	
	public boolean setInUse(boolean inUse)
	{
		_inUse = inUse;
		
		if (_inUse)
			_startConditionsCorrect = onStart();
		else
			onExit();
		
		return _startConditionsCorrect;
	}
	
	public boolean isSelfEffect()
	{
		return _isSelfEffect;
	}
	
	public void setSelfEffect()
	{
		_isSelfEffect = true;
	}
	
	public boolean cantUpdateAnymore()
	{
		return _cantUpdateAnymore;
	}
	
	private final synchronized void startEffectTask()
	{
		if (_period > 0)
		{
			stopEffectTask();
			
			final int initialDelay = Math.max((_period - _periodFirstTime) * 1000, 5);
			if (_count > 1)
				_currentFuture = ThreadPool.scheduleAtFixedRate(this::startEffect, initialDelay, _period * 1000);
			else
				_currentFuture = ThreadPool.schedule(this::startEffect, initialDelay);
		}
		
		if (_state == EffectState.ACTING)
		{
			if (isSelfEffectType())
				_effector.addEffect(this);
			else
				_effected.addEffect(this);
		}
	}
	
	private void startEffect()
	{
		_periodFirstTime = 0;
		_periodStartTime = System.currentTimeMillis();
		
		scheduleEffect();
	}
	
	public final void exit()
	{
		exit(false);
	}
	
	public final void exit(boolean cantUpdateAnymore)
	{
		_cantUpdateAnymore = cantUpdateAnymore;
		
		_state = EffectState.FINISHING;
		
		scheduleEffect();
	}
	
	public final synchronized void stopEffectTask()
	{
		if (_currentFuture != null)
		{
			_currentFuture.cancel(false);
			_currentFuture = null;
			
			if (isSelfEffectType() && getEffector() != null)
				getEffector().removeEffect(this);
			else if (getEffected() != null)
				getEffected().removeEffect(this);
		}
	}
	
	/**
	 * Fire an event, happening on start.
	 * @return Always true, but overidden in each effect.
	 */
	public boolean onStart()
	{
		if (_template.getAbnormalEffect() != AbnormalEffect.NULL)
			getEffected().startAbnormalEffect(_template.getAbnormalEffect());
		
		return true;
	}
	
	/**
	 * Cancel the effect in the abnormal effect map of the effected {@link Creature}.
	 */
	public void onExit()
	{
		if (_template.getAbnormalEffect() != AbnormalEffect.NULL)
			getEffected().stopAbnormalEffect(_template.getAbnormalEffect());
	}
	
	public final void rescheduleEffect()
	{
		if (_state != EffectState.ACTING)
			scheduleEffect();
		else
		{
			if (_period != 0)
			{
				startEffectTask();
				return;
			}
		}
	}
	
	public final void scheduleEffect()
	{
		switch (_state)
		{
			case CREATED:
			{
				_state = EffectState.ACTING;
				
				if (_skill.isPvpSkill() && _template.showIcon() && getEffected() instanceof Player)
					getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(_skill));
				
				if (_period != 0)
				{
					startEffectTask();
					return;
				}
				
				// Effects not having count or period should start.
				_startConditionsCorrect = onStart();
			}
			case ACTING:
			{
				if (_count > 0)
				{
					_count--;
					
					// Effect has to be in use.
					if (getInUse())
					{
						// False causes effect to finish right away.
						if (onActionTime() && _startConditionsCorrect && _count > 0)
							return;
					}
					// Do not finish it yet, in case reactivated.
					else if (_count > 0)
						return;
				}
				_state = EffectState.FINISHING;
			}
			case FINISHING:
			{
				// If the time left is equal to zero, send the message.
				if (_count == 0 && _template.showIcon() && getEffected() instanceof Player)
					getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF).addSkillName(_skill));
				
				// if task is null - stopEffectTask does not remove effect.
				if (_currentFuture == null && getEffected() != null)
					getEffected().removeEffect(this);
				
				// Stop the task, remove it and update icon.
				stopEffectTask();
				
				// Cancel the effect in the the abnormal effect map of the Creature.
				if (getInUse() || !(_count > 1 || _period > 0))
					if (_startConditionsCorrect)
						onExit();
			}
		}
	}
	
	public List<Func> getStatFuncs()
	{
		if (_template.getFuncTemplates() == null)
			return Collections.emptyList();
		
		final List<Func> funcs = new ArrayList<>(_template.getFuncTemplates().size());
		
		for (FuncTemplate template : _template.getFuncTemplates())
		{
			final Func func = template.getFunc(getEffector(), getEffected(), _skill, this);
			if (func != null)
				funcs.add(func);
		}
		return funcs;
	}
	
	public final void addIcon(AbnormalStatusUpdate asu)
	{
		if (_state != EffectState.ACTING)
			return;
		
		final ScheduledFuture<?> future = _currentFuture;
		if (_template.getCounter() > 1)
		{
			if (_skill.isPotion())
				asu.addEffect(_skill, _skill.getBuffDuration() - (getTaskTime() * 1000));
			else
				asu.addEffect(_skill, (_count * _period - getTaskTime()) * 1000);
		}
		else if (future != null)
			asu.addEffect(_skill, (int) future.getDelay(TimeUnit.MILLISECONDS));
		else if (_period == -1)
			asu.addEffect(_skill, _period);
	}
	
	public final void addPartySpelledIcon(PartySpelled ps)
	{
		if (_state != EffectState.ACTING)
			return;
		
		final ScheduledFuture<?> future = _currentFuture;
		if (future != null)
			ps.addEffect(_skill, (int) future.getDelay(TimeUnit.MILLISECONDS));
		else if (_period == -1)
			ps.addEffect(_skill, _period);
	}
	
	public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo eosi)
	{
		if (_state != EffectState.ACTING)
			return;
		
		final ScheduledFuture<?> future = _currentFuture;
		if (future != null)
			eosi.addEffect(_skill, (int) future.getDelay(TimeUnit.MILLISECONDS));
		else if (_period == -1)
			eosi.addEffect(_skill, _period);
	}
	
	/**
	 * @return The EffectFlag mask for this {@link AbstractEffect}.
	 */
	public int getEffectFlags()
	{
		return EffectFlag.NONE.getMask();
	}
	
	public boolean isSelfEffectType()
	{
		return false;
	}
	
	public boolean onSameEffect(AbstractEffect effect)
	{
		return true;
	}
	
	/**
	 * @param effect : The {@link AbstractEffect} to test.
	 * @return True if both this and tested {@link AbstractEffect}s share {@link L2Skill} id, {@link EffectType}, aswell as stack type and order.
	 */
	public boolean isIdentical(AbstractEffect effect)
	{
		return _skill.getId() == effect.getSkill().getId() && getEffectType() == effect.getEffectType() && _template.isSameStackTypeAndOrderThan(effect.getTemplate());
	}
}