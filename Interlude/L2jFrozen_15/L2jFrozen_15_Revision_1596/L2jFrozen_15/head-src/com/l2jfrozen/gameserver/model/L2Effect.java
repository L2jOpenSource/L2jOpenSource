package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import com.l2jfrozen.gameserver.network.serverpackets.MagicEffectIcons;
import com.l2jfrozen.gameserver.network.serverpackets.PartySpelled;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.effects.EffectTemplate;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.skills.funcs.FuncTemplate;
import com.l2jfrozen.gameserver.skills.funcs.Lambda;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.12 $ $Date: 2005/04/11 10:06:07 $
 * @author  l2jfrozen dev
 */
public abstract class L2Effect
{
	static final Logger LOGGER = Logger.getLogger(L2Effect.class);
	
	public static enum EffectState
	{
		CREATED,
		ACTING,
		FINISHING
	}
	
	public static enum EffectType
	{
		BUFF,
		DEBUFF,
		CHARGE,
		DMG_OVER_TIME,
		HEAL_OVER_TIME,
		COMBAT_POINT_HEAL_OVER_TIME,
		MANA_DMG_OVER_TIME,
		MANA_HEAL_OVER_TIME,
		MP_CONSUME_PER_LEVEL,
		RELAXING,
		STUN,
		ROOT,
		SLEEP,
		HATE,
		FAKE_DEATH,
		CONFUSION,
		CONFUSE_MOB_ONLY,
		MUTE,
		IMMOBILEUNTILATTACKED,
		FEAR,
		SALVATION,
		SILENT_MOVE,
		SIGNET_EFFECT,
		SIGNET_GROUND,
		SEED,
		PARALYZE,
		STUN_SELF,
		PSYCHICAL_MUTE,
		REMOVE_TARGET,
		TARGET_ME,
		SILENCE_MAGIC_PHYSICAL,
		BETRAY,
		NOBLESSE_BLESSING,
		PHOENIX_BLESSING,
		PETRIFICATION,
		BLUFF,
		BATTLE_FORCE,
		SPELL_FORCE,
		CHARM_OF_LUCK,
		INVINCIBLE,
		PROTECTION_BLESSING,
		INTERRUPT,
		MEDITATION,
		BLOW,
		FUSION,
		CANCEL,
		BLOCK_BUFF,
		BLOCK_DEBUFF,
		PREVENT_BUFF,
		CLAN_GATE,
		NEGATE
	}
	
	private static final Func[] emptyFunctionSet = new Func[0];
	
	// member effector is the instance of L2Character that cast/used the spell/skill that is
	// causing this effect. Do not confuse with the instance of L2Character that
	// is being affected by this effect.
	private final L2Character effector;
	
	// member effected is the instance of L2Character that was affected
	// by this effect. Do not confuse with the instance of L2Character that
	// catsed/used this effect.
	protected final L2Character effected;
	
	// the skill that was used.
	public L2Skill skill;
	
	// or the items that was used.
	// private final L2Item item;
	
	// the value of an update
	private final Lambda lambda;
	
	// the current state
	private EffectState state;
	
	// period, seconds
	private final int period;
	private int periodStartTicks;
	private int periodfirsttime;
	
	// function templates
	private final FuncTemplate[] funcTemplates;
	
	// initial count
	protected int totalCount;
	// counter
	private int count;
	
	// abnormal effect mask
	private final int abnormalEffect;
	
	public boolean preventExitUpdate;
	
	private boolean cancelEffect = false;
	
	public final class EffectTask implements Runnable
	{
		protected final int delay;
		protected final int rate;
		
		EffectTask(final int pDelay, final int pRate)
		{
			delay = pDelay;
			rate = pRate;
		}
		
		@Override
		public void run()
		{
			try
			{
				if (getPeriodfirsttime() == 0)
				{
					setPeriodStartTicks(GameTimeController.getGameTicks());
				}
				else
				{
					setPeriodfirsttime(0);
				}
				scheduleEffect();
			}
			catch (final Throwable e)
			{
				LOGGER.error("", e);
			}
		}
	}
	
	private ScheduledFuture<?> currentFuture;
	private EffectTask currentTask;
	
	/** The Identifier of the stack group */
	private final String stackType;
	
	/** The position of the effect in the stack group */
	private final float stackOrder;
	
	private final EffectTemplate template;
	
	private boolean inUse = false;
	
	protected L2Effect(final Env env, final EffectTemplate template)
	{
		this.template = template;
		state = EffectState.CREATED;
		skill = env.skill;
		// item = env._item == null ? null : env._item.getItem();
		effected = env.target;
		effector = env.player;
		lambda = template.lambda;
		funcTemplates = template.funcTemplates;
		count = template.counter;
		totalCount = count;
		int temp = template.period;
		if (env.skillMastery)
		{
			temp *= 2;
		}
		period = temp;
		abnormalEffect = template.abnormalEffect;
		stackType = template.stackType;
		stackOrder = template.stackOrder;
		periodStartTicks = GameTimeController.getGameTicks();
		periodfirsttime = 0;
		scheduleEffect();
	}
	
	public int getCount()
	{
		return count;
	}
	
	public int getTotalCount()
	{
		return totalCount;
	}
	
	public void setCount(final int newcount)
	{
		count = newcount;
	}
	
	public void setFirstTime(final int newfirsttime)
	{
		if (currentFuture != null)
		{
			periodStartTicks = GameTimeController.getGameTicks() - newfirsttime * GameTimeController.TICKS_PER_SECOND;
			currentFuture.cancel(false);
			currentFuture = null;
			currentTask = null;
			periodfirsttime = newfirsttime;
			final int duration = period - periodfirsttime;
			// LOGGER.warn("Period: "+_period+"-"+_periodfirsttime+"="+duration);
			currentTask = new EffectTask(duration * 1000, -1);
			currentFuture = ThreadPoolManager.getInstance().scheduleEffect(currentTask, duration * 1000);
		}
	}
	
	public int getPeriod()
	{
		return period;
	}
	
	public int getTime()
	{
		return (GameTimeController.getGameTicks() - periodStartTicks) / GameTimeController.TICKS_PER_SECOND;
	}
	
	/**
	 * Returns the elapsed time of the task.
	 * @return Time in seconds.
	 */
	public int getTaskTime()
	{
		if (count == totalCount)
		{
			return 0;
		}
		
		return Math.abs(count - totalCount + 1) * period + getTime() + 1;
	}
	
	public boolean getInUse()
	{
		return inUse;
	}
	
	public void setInUse(final boolean inUse)
	{
		this.inUse = inUse;
	}
	
	public String getStackType()
	{
		return stackType;
	}
	
	public float getStackOrder()
	{
		return stackOrder;
	}
	
	public final L2Skill getSkill()
	{
		return skill;
	}
	
	public final L2Character getEffector()
	{
		return effector;
	}
	
	public final L2Character getEffected()
	{
		return effected;
	}
	
	public boolean isSelfEffect()
	{
		return skill.effectTemplatesSelf != null;
	}
	
	public boolean isHerbEffect()
	{
		if (getSkill().getName().contains("Herb"))
		{
			return true;
		}
		
		return false;
	}
	
	public final double calc()
	{
		final Env env = new Env();
		env.player = effector;
		env.target = effected;
		env.skill = skill;
		return lambda.calc(env);
	}
	
	private synchronized void startEffectTask(final int duration)
	{
		stopEffectTask();
		currentTask = new EffectTask(duration, -1);
		currentFuture = ThreadPoolManager.getInstance().scheduleEffect(currentTask, duration);
		
		if (state == EffectState.ACTING)
		{
			// To avoid possible NPE caused by player crash
			if (effected != null)
			{
				effected.addEffect(this);
			}
			else
			{
				LOGGER.warn("Effected is null for skill " + skill.getId() + " on effect " + getEffectType());
			}
		}
	}
	
	private synchronized void startEffectTaskAtFixedRate(final int delay, final int rate)
	{
		stopEffectTask();
		currentTask = new EffectTask(delay, rate);
		currentFuture = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(currentTask, delay, rate);
		
		if (state == EffectState.ACTING)
		{
			effected.addEffect(this);
		}
	}
	
	/**
	 * Stop the L2Effect task and send Server->Client update packet.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Cancel the effect in the the abnormal effect map of the L2Character</li>
	 * <li>Stop the task of the L2Effect, remove it and update client magic icone</li><BR>
	 * <BR>
	 */
	public final void exit()
	{
		this.exit(false, false);
	}
	
	public final void exit(final boolean cancelEffect)
	{
		this.exit(false, cancelEffect);
	}
	
	public final void exit(final boolean preventUpdate, final boolean cancelEffect)
	{
		preventExitUpdate = preventUpdate;
		state = EffectState.FINISHING;
		this.cancelEffect = cancelEffect;
		scheduleEffect();
	}
	
	/**
	 * Stop the task of the L2Effect, remove it and update client magic icone.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Cancel the task</li>
	 * <li>Stop and remove L2Effect from L2Character and update client magic icone</li><BR>
	 * <BR>
	 */
	public synchronized void stopEffectTask()
	{
		// Cancel the task
		if (currentFuture != null)
		{
			if (!currentFuture.isCancelled())
			{
				currentFuture.cancel(false);
			}
			
			currentFuture = null;
			currentTask = null;
			
			// To avoid possible NPE caused by player crash
			if (effected != null)
			{
				effected.removeEffect(this);
			}
			else
			{
				LOGGER.warn("Effected is null for skill " + skill.getId() + " on effect " + getEffectType());
			}
		}
	}
	
	/**
	 * @return effect type
	 */
	public abstract EffectType getEffectType();
	
	/** Notify started */
	public void onStart()
	{
		if (abnormalEffect != 0)
		{
			getEffected().startAbnormalEffect(abnormalEffect);
		}
	}
	
	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR>
	 * <BR>
	 */
	public void onExit()
	{
		if (abnormalEffect != 0)
		{
			getEffected().stopAbnormalEffect(abnormalEffect);
		}
	}
	
	/**
	 * Return true for continuation of this effect
	 * @return
	 */
	public abstract boolean onActionTime();
	
	public final void rescheduleEffect()
	{
		if (state != EffectState.ACTING)
		{
			scheduleEffect();
		}
		else
		{
			if (count > 1)
			{
				startEffectTaskAtFixedRate(5, period * 1000);
				return;
			}
			if (period > 0)
			{
				startEffectTask(period * 1000);
				return;
			}
		}
	}
	
	public final void scheduleEffect()
	{
		if (state == EffectState.CREATED)
		{
			state = EffectState.ACTING;
			
			onStart();
			
			if (skill.isPvpSkill() && getEffected() != null && getEffected() instanceof L2PcInstance && getShowIcon())
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				smsg.addString(skill.getName());
				getEffected().sendPacket(smsg);
				smsg = null;
			}
			
			if (count > 1)
			{
				startEffectTaskAtFixedRate(5, period * 1000);
				return;
			}
			if (period > 0)
			{
				startEffectTask(period * 1000);
				return;
			}
		}
		
		if (state == EffectState.ACTING)
		{
			if (count-- > 0)
			{
				if (getInUse())
				{ // effect has to be in use
					if (onActionTime())
					{
						return; // false causes effect to finish right away
					}
				}
				else if (count > 0)
				{
					return;
				}
			}
			state = EffectState.FINISHING;
		}
		
		if (state == EffectState.FINISHING)
		{
			// Cancel the effect in the the abnormal effect map of the L2Character
			onExit();
			
			// If the time left is equal to zero, send the message
			if (getEffected() != null && getEffected() instanceof L2PcInstance && getShowIcon() && !getEffected().isDead())
			{
				
				// Like L2OFF message S1_HAS_BEEN_ABORTED for toogle skills
				if (getSkill().isToggle())
				{
					final SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
					smsg3.addString(getSkill().getName());
					getEffected().sendPacket(smsg3);
				}
				else if (cancelEffect)
				{
					SystemMessage smsg3 = new SystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
					smsg3.addString(getSkill().getName());
					getEffected().sendPacket(smsg3);
					smsg3 = null;
				}
				else if (count == 0)
				{
					SystemMessage smsg3 = new SystemMessage(SystemMessageId.S1_HAS_WORN_OFF);
					smsg3.addString(skill.getName());
					getEffected().sendPacket(smsg3);
					smsg3 = null;
				}
				
			}
			
			// Stop the task of the L2Effect, remove it and update client magic icone
			stopEffectTask();
			
		}
	}
	
	public Func[] getStatFuncs()
	{
		if (funcTemplates == null)
		{
			return emptyFunctionSet;
		}
		final List<Func> funcs = new ArrayList<>();
		for (final FuncTemplate t : funcTemplates)
		{
			final Env env = new Env();
			env.player = getEffector();
			env.target = getEffected();
			env.skill = getSkill();
			final Func f = t.getFunc(env, this); // effect is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.size() == 0)
		{
			return emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public final void addIcon(final MagicEffectIcons mi)
	{
		EffectTask task = currentTask;
		ScheduledFuture<?> future = currentFuture;
		
		if (task == null || future == null)
		{
			return;
		}
		
		if (state == EffectState.FINISHING || state == EffectState.CREATED)
		{
			return;
		}
		
		if (!getShowIcon())
		{
			return;
		}
		
		final L2Skill sk = getSkill();
		
		if (task.rate > 0)
		{
			if (sk.isPotion())
			{
				mi.addEffect(sk.getId(), getLevel(), sk.getBuffDuration() - getTaskTime() * 1000, false);
			}
			else if (!sk.isToggle())
			{
				if (sk.is_Debuff())
				{
					mi.addEffect(sk.getId(), getLevel(), (count * period) * 1000, true);
				}
				else
				{
					mi.addEffect(sk.getId(), getLevel(), (count * period) * 1000, false);
				}
			}
			else
			{
				mi.addEffect(sk.getId(), getLevel(), -1, true);
			}
		}
		else
		{
			if (sk.getSkillType() == SkillType.DEBUFF)
			{
				mi.addEffect(sk.getId(), getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS) + 1000, true);
			}
			else
			{
				mi.addEffect(sk.getId(), getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS) + 1000, false);
			}
		}
		
		task = null;
		future = null;
	}
	
	public final void addPartySpelledIcon(final PartySpelled ps)
	{
		EffectTask task = currentTask;
		ScheduledFuture<?> future = currentFuture;
		
		if (task == null || future == null)
		{
			return;
		}
		
		if (state == EffectState.FINISHING || state == EffectState.CREATED)
		{
			return;
		}
		
		L2Skill sk = getSkill();
		ps.addPartySpelledEffect(sk.getId(), getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		
		task = null;
		future = null;
		sk = null;
	}
	
	public final void addOlympiadSpelledIcon(final ExOlympiadSpelledInfo os)
	{
		EffectTask task = currentTask;
		ScheduledFuture<?> future = currentFuture;
		
		if (task == null || future == null)
		{
			return;
		}
		
		if (state == EffectState.FINISHING || state == EffectState.CREATED)
		{
			return;
		}
		
		L2Skill sk = getSkill();
		os.addEffect(sk.getId(), getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
		
		sk = null;
		task = null;
		future = null;
	}
	
	public int getLevel()
	{
		return getSkill().getLevel();
	}
	
	public int getPeriodfirsttime()
	{
		return periodfirsttime;
	}
	
	public void setPeriodfirsttime(final int periodfirsttime)
	{
		this.periodfirsttime = periodfirsttime;
	}
	
	public int getPeriodStartTicks()
	{
		return periodStartTicks;
	}
	
	public void setPeriodStartTicks(final int periodStartTicks)
	{
		this.periodStartTicks = periodStartTicks;
	}
	
	public final boolean getShowIcon()
	{
		return template.showIcon;
	}
	
	public EffectState get_state()
	{
		return state;
	}
	
}
