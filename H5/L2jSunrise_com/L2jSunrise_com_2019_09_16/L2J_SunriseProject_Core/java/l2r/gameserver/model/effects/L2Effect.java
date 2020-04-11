/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.effects;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.ChanceCondition;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.interfaces.IChanceSkillTrigger;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Formulas;
import l2r.gameserver.model.stats.functions.AbstractFunction;
import l2r.gameserver.model.stats.functions.FuncTemplate;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.MagicSkillLaunched;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2Effect implements IChanceSkillTrigger
{
	protected static final Logger _log = LoggerFactory.getLogger(L2Effect.class);
	
	private static final AbstractFunction[] _emptyFunctionSet = new AbstractFunction[0];
	
	// member _effector is the instance of L2Character that cast/used the spell/skill that is
	// causing this effect. Do not confuse with the instance of L2Character that
	// is being affected by this effect.
	private final L2Character _effector;
	
	// member _effected is the instance of L2Character that was affected
	// by this effect. Do not confuse with the instance of L2Character that
	// casted/used this effect.
	private final L2Character _effected;
	
	// the skill that was used.
	private final L2Skill _skill;
	
	// the value of an update
	private final double _value;
	
	// the current state
	private EffectState _state;
	
	// period, seconds
	private int _period;
	protected int _periodStartTicks;
	protected int _periodFirstTime;
	
	private final EffectTemplate _template;
	
	// function templates
	private final FuncTemplate[] _funcTemplates;
	
	// counter
	private int _count;
	
	// abnormal effect mask
	private final AbnormalEffect _abnormalEffect;
	// special effect mask
	private final AbnormalEffect[] _specialEffect;
	// show icon
	private final boolean _icon;
	// is self effect?
	private boolean _isSelfEffect = false;
	// is passive effect?
	private boolean _isPassiveEffect = false;
	
	public boolean preventExitUpdate;
	
	protected final class EffectTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				_periodFirstTime = 0;
				_periodStartTicks = GameTimeController.getInstance().getGameTicks();
				scheduleEffect();
			}
			catch (Exception e)
			{
				_log.error("", e);
			}
		}
	}
	
	private volatile ScheduledFuture<?> _currentFuture;
	
	/** The Identifier of the stack group */
	private final String _abnormalType;
	
	/** The position of the effect in the stack group */
	private final byte _abnormalLvl;
	
	/** If {@code true} then this effect is in use (or has been stop because an Herb took place). */
	private boolean _isInUse = true;
	private boolean _startConditionsCorrect = true;
	
	/**
	 * For special behavior. See Formulas.calcEffectSuccess
	 */
	private double _effectPower;
	
	/**
	 * <font color="FF0000"><b>WARNING: scheduleEffect no longer inside constructor</b></font><br>
	 * So you must call it explicitly
	 * @param env
	 * @param template
	 */
	protected L2Effect(Env env, EffectTemplate template)
	{
		_state = EffectState.CREATED;
		_skill = env.getSkill();
		_template = template;
		_effected = env.getTarget();
		_effector = env.getCharacter();
		_value = template.value;
		_funcTemplates = template.funcTemplates;
		_count = template.counter;
		
		_period = Formulas.calcEffectAbnormalTime(_effector, _effected, this);
		_abnormalEffect = template.abnormalEffect;
		_specialEffect = template.specialEffect;
		_abnormalType = template.abnormalType;
		_abnormalLvl = template.abnormalLvl;
		_periodStartTicks = GameTimeController.getInstance().getGameTicks();
		_periodFirstTime = 0;
		_icon = template.icon;
		_effectPower = template.effectPower;
	}
	
	/**
	 * Special constructor to "steal" buffs. Must be implemented on every child class that can be stolen.<br>
	 * <font color="FF0000"><b>WARNING: scheduleEffect no longer inside constructor</b></font><br>
	 * So you must call it explicitly.
	 * @param env
	 * @param effect
	 */
	protected L2Effect(Env env, L2Effect effect)
	{
		_template = effect._template;
		_state = EffectState.CREATED;
		_skill = env.getSkill();
		_effected = env.getTarget();
		_effector = env.getCharacter();
		_value = _template.value;
		_funcTemplates = _template.funcTemplates;
		_count = _template.counter;
		_period = _template.abnormalTime;
		_abnormalEffect = _template.abnormalEffect;
		_specialEffect = _template.specialEffect;
		_abnormalType = _template.abnormalType;
		_abnormalLvl = _template.abnormalLvl;
		_periodStartTicks = effect.getPeriodStartTicks();
		_periodFirstTime = effect.getTime();
		_icon = _template.icon;
		
		// Commented out by DrHouse:
		// scheduleEffect can call onStart before effect is completly initialized on constructor (child classes constructor)
		// scheduleEffect();
	}
	
	public int getCount()
	{
		return _count;
	}
	
	public int getTotalCount()
	{
		return _template.counter;
	}
	
	public void setCount(int newcount)
	{
		_count = Math.min(newcount, getTotalCount()); // sanity check
	}
	
	public void setFirstTime(int newFirstTime)
	{
		_periodFirstTime = Math.min(newFirstTime, _period);
		_periodStartTicks -= _periodFirstTime * GameTimeController.TICKS_PER_SECOND;
	}
	
	public boolean getShowIcon()
	{
		return _icon;
	}
	
	public int getAbnormalTime()
	{
		return _period;
	}
	
	/**
	 * @return Time pass from effect start
	 */
	public int getTime()
	{
		return (GameTimeController.getInstance().getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * @return Time left for effect
	 */
	public int getRemainingTime()
	{
		if (getSkill().isToggle())
		{
			return -1;
		}
		return (_period * Math.max(_count, 1)) - ((GameTimeController.getInstance().getGameTicks() - _periodStartTicks) / GameTimeController.TICKS_PER_SECOND);
	}
	
	public void setPeriod(int period)
	{
		_period = period;
	}
	
	/**
	 * Verify if this buff info is in use.
	 * @return {@code true} if this buff info is in use, {@code false} otherwise
	 */
	public boolean getInUse()
	{
		return _isInUse;
	}
	
	/**
	 * Verify if this buff info is in use.
	 * @return {@code true} if this buff info is in use, {@code false} otherwise
	 */
	public boolean isInUse()
	{
		return _isInUse;
	}
	
	public boolean setInUse(boolean inUse)
	{
		_isInUse = inUse;
		if (_isInUse)
		{
			_startConditionsCorrect = onStart();
		}
		else
		{
			onExit();
		}
		
		return _startConditionsCorrect;
	}
	
	public String getAbnormalType()
	{
		return _abnormalType;
	}
	
	public byte getAbnormalLvl()
	{
		return _abnormalLvl;
	}
	
	public final L2Skill getSkill()
	{
		return _skill;
	}
	
	public final L2Character getEffector()
	{
		return _effector;
	}
	
	public final L2Character getEffected()
	{
		return _effected;
	}
	
	public boolean isSelfEffect()
	{
		return _isSelfEffect;
	}
	
	public void setSelfEffect()
	{
		_isSelfEffect = true;
	}
	
	public boolean isPassiveEffect()
	{
		return _isPassiveEffect;
	}
	
	public void setPassiveEffect()
	{
		_isPassiveEffect = true;
	}
	
	public EffectState getState()
	{
		return _state;
	}
	
	private final void startEffectTask()
	{
		stopEffectTask();
		
		if (_period > 0)
		{
			int initialDelay = Math.max((_period - _periodFirstTime) * 1000, 5);
			if (_count > 1)
			{
				_currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new EffectTask(), initialDelay, _period * 1000);
			}
			else
			{
				_currentFuture = ThreadPoolManager.getInstance().scheduleEffect(new EffectTask(), initialDelay);
			}
		}
		
		if (_state == EffectState.ACTING)
		{
			if (isSelfEffectType())
			{
				_effector.addEffect(this);
			}
			else
			{
				_effected.addEffect(this);
			}
		}
	}
	
	/**
	 * Stop the L2Effect task and send Server->Client update packet.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Cancel the effect in the the abnormal effect map of the L2Character</li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icon</li>
	 * </ul>
	 */
	public final void exit()
	{
		exit(false, false);
	}
	
	public final void exit(boolean preventUpdate)
	{
		exit(preventUpdate, false);
	}
	
	public final void exit(boolean preventUpdate, boolean force)
	{
		preventExitUpdate = preventUpdate;
		_state = EffectState.FINISHING;
		scheduleEffect(force);
	}
	
	public final void stopEffectTask()
	{
		stopEffectTask(false);
	}
	
	public final void stopEffectTask(boolean force)
	{
		try
		{
			if (_currentFuture != null)
			{
				// Cancel the task
				_currentFuture.cancel(false);
				// ThreadPoolManager.getInstance().removeEffect(_currentTask);
				_currentFuture = null;
				
				if (isSelfEffectType() && (getEffector() != null))
				{
					getEffector().removeEffect(this);
				}
				else if ((getEffected() != null) && !getSkill().isPassive())
				{
					getEffected().removeEffect(this);
				}
				else if (getSkill().isPassive() && force)
				{
					getEffector().removeEffect(this);
				}
			}
		}
		catch (Exception e)
		{
			// nothing to log
		}
	}
	
	/**
	 * Get this effect's type.<br>
	 * TODO: Remove.
	 * @return the effect type
	 */
	public L2EffectType getEffectType()
	{
		return L2EffectType.NONE;
	}
	
	/**
	 * Notify started.
	 * @return {@code true} if all the start conditions are meet, {@code false} otherwise
	 */
	public boolean onStart()
	{
		if (_abnormalEffect != AbnormalEffect.NULL)
		{
			getEffected().startAbnormalEffect(_abnormalEffect);
		}
		if (_specialEffect != null)
		{
			getEffected().startSpecialEffect(_specialEffect);
		}
		return true;
	}
	
	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.
	 */
	public void onExit()
	{
		if (_abnormalEffect != AbnormalEffect.NULL)
		{
			getEffected().stopAbnormalEffect(_abnormalEffect);
		}
		if (_specialEffect != null)
		{
			getEffected().stopSpecialEffect(_specialEffect);
		}
	}
	
	/**
	 * Called on each tick.<br>
	 * If the abnormal time is lesser than zero it will last forever.
	 * @return if {@code true} this effect will continue forever, if {@code false} it will stop after abnormal time has passed
	 */
	public boolean onActionTime()
	{
		return getAbnormalTime() < 0;
	}
	
	public final void scheduleEffect()
	{
		scheduleEffect(false);
	}
	
	public final void scheduleEffect(boolean force)
	{
		switch (_state)
		{
			case CREATED:
			{
				_state = EffectState.ACTING;
				
				if (_skill.isOffensive() && _icon && !isInstant() && getEffected().isPlayer())
				{
					SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					smsg.addSkillName(_skill);
					getEffected().sendPacket(smsg);
				}
				
				// vGodFather Update abnormal effects just in case
				getEffected().updateAbnormalEffect();
				
				if (_period != 0)
				{
					startEffectTask();
					return;
				}
				// effects not having count or period should start
				_startConditionsCorrect = onStart();
			}
			case ACTING:
			{
				if (_count-- > 0)
				{
					if (getInUse())
					{
						// effect has to be in use
						if (onActionTime() && _startConditionsCorrect && (_count > 0))
						{
							return; // false causes effect to finish right away
						}
					}
					else if (_count > 0)
					{
						return;
					}
				}
				_state = EffectState.FINISHING;
			}
			case FINISHING:
			{
				// if task is null - stopEffectTask does not remove effect
				if ((_currentFuture == null) && (getEffected() != null))
				{
					getEffected().removeEffect(this);
				}
				
				// Stop the task of the L2Effect, remove it and update client magic icon
				stopEffectTask(force);
				
				// vGodFather Update abnormal effects just in case
				getEffected().updateAbnormalEffect();
				
				// Cancel the effect in the abnormal effect map of the L2Character
				if (getInUse() || !((_count > 1) || (_period > 0)))
				{
					if (_startConditionsCorrect && !getSkill().isPassive())
					{
						onExit();
					}
				}
				
				if (_skill.getAfterEffectId() > 0)
				{
					L2Skill skill = SkillData.getInstance().getInfo(_skill.getAfterEffectId(), _skill.getAfterEffectLvl());
					if (skill != null)
					{
						getEffected().broadcastPacket(new MagicSkillUse(_effected, skill.getId(), skill.getLevel(), 0, 0));
						getEffected().broadcastPacket(new MagicSkillLaunched(_effected, skill.getId(), skill.getLevel()));
						skill.getEffects(getEffected(), getEffected());
					}
				}
			}
		}
	}
	
	public AbstractFunction[] getStatFuncs()
	{
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		
		final ArrayList<AbstractFunction> funcs = new ArrayList<>(_funcTemplates.length);
		
		Env env = new Env();
		env.setCharacter(_effector);
		env.setTarget(_effected);
		env.setSkill(_skill);
		
		AbstractFunction f;
		for (FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, this); // effect is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		
		if (funcs.isEmpty())
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new AbstractFunction[funcs.size()]);
	}
	
	public int getLevel()
	{
		return getSkill().getLevel();
	}
	
	public int getPeriodStartTicks()
	{
		return _periodStartTicks;
	}
	
	public EffectTemplate getEffectTemplate()
	{
		return _template;
	}
	
	public double getEffectPower()
	{
		return _effectPower;
	}
	
	public boolean canBeStolen()
	{
		// TODO: Unhardcode skillId
		return (!effectCanBeStolen() || (getEffectType() == L2EffectType.TRANSFORMATION) || getSkill().isPassive() || getSkill().isToggle() || getSkill().isDebuff() || getSkill().isHeroSkill() || getSkill().isGMSkill() || (getSkill().isStatic() && ((getSkill().getId() != 2274) && (getSkill().getId() != 2341))) || !getSkill().canBeDispeled()) ? false : true;
	}
	
	/**
	 * @return {@code true} if effect itself can be stolen, {@code false} otherwise
	 */
	protected boolean effectCanBeStolen()
	{
		return false;
	}
	
	/**
	 * Return bit flag for current effect
	 * @return int flag
	 */
	public int getEffectFlags()
	{
		return EffectFlag.NONE.getMask();
	}
	
	public double getValue()
	{
		return _value;
	}
	
	@Override
	public String toString()
	{
		return "L2Effect [_skill=" + _skill + ", _state=" + _state + ", _period=" + _period + "]";
	}
	
	public boolean isSelfEffectType()
	{
		return false;
	}
	
	public void decreaseForce()
	{
	}
	
	public void increaseEffect()
	{
	}
	
	public int getForceEffect()
	{
		return 0;
	}
	
	public boolean checkCondition(Object obj)
	{
		return true;
	}
	
	@Override
	public boolean triggersChanceSkill()
	{
		return false;
	}
	
	@Override
	public int getTriggeredChanceId()
	{
		return 0;
	}
	
	@Override
	public int getTriggeredChanceLevel()
	{
		return 0;
	}
	
	@Override
	public ChanceCondition getTriggeredChanceCondition()
	{
		return null;
	}
	
	// TODO Partially Done
	public boolean isInstant()
	{
		return false;
	}
}