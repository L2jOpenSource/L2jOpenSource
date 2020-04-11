package net.sf.l2j.gameserver.model.actor.cast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.GaugeColor;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillCanceled;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * This class groups all cast data related to a {@link Creature}.
 */
public class CreatureCast
{
	public static final CLogger LOGGER = new CLogger(CreatureCast.class.getName());
	
	protected final Creature _creature;
	
	protected Future<?> _castTask;
	protected Future<?> _castTask2;
	
	private long _castInterruptTime;
	
	private volatile boolean _isCastingNow;
	private volatile boolean _isCastingSimultaneouslyNow;
	
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;
	
	public CreatureCast(Creature creature)
	{
		_creature = creature;
	}
	
	public final void setSkillCast(Future<?> newSkillCast)
	{
		_castTask = newSkillCast;
	}
	
	/**
	 * @return True if the Creature is casting.
	 */
	public final boolean isCastingNow()
	{
		return _isCastingNow;
	}
	
	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}
	
	public final boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}
	
	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}
	
	/**
	 * @return True if the cast of the Creature can be aborted.
	 */
	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}
	
	public final L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}
	
	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}
	
	public final L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}
	
	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}
	
	public void doCast(int id, int level)
	{
		final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		if (skill != null)
			doCast(skill);
	}
	
	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted...</li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send MagicSkillUse (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message</li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li>
	 * </ul>
	 * @param skill The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		if (skill.getHitTime() > 50 && !skill.isSimultaneousCast())
			_creature.getMove().stop();
		
		beginCast(skill, false);
	}
	
	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		if (!checkDoCastConditions(skill))
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			return;
		}
		
		// Override casting type
		if (skill.isSimultaneousCast() && !simultaneously)
			simultaneously = true;
		
		// Set the target of the skill in function of Skill Type and Target Type
		Creature target = null;
		WorldObject[] targets = null;
		// Get all possible targets of the skill in a table in function of the skill target type
		// Allow direct usage of doCast
		if (_creature.getAI().getCurrentIntention().getType() == IntentionType.CAST)
		{
			final SkillUseHolder holder = (SkillUseHolder) _creature.getAI().getCurrentIntention().getFirstParameter();
			final WorldObject objTarget = holder.getTarget();
			
			if (_creature instanceof Playable)
			{
				final ItemInstance itemInstance = (ItemInstance) _creature.getAI().getCurrentIntention().getSecondParameter();
				if (itemInstance != null)
					((Playable) _creature).addItemSkillTimeStamp(holder.getSkill(), itemInstance);
			}
			
			final Creature craeTarget = (objTarget instanceof Creature ? (Creature) objTarget : null);
			targets = skill.getTargetList(_creature, false, craeTarget);
		}
		else
			targets = skill.getTargetList(_creature);
		
		boolean doit = false;
		
		// AURA skills should always be using caster as target
		switch (skill.getTargetType())
		{
			case AREA_SUMMON: // We need it to correct facing
				target = _creature.getSummon();
				break;
			
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case AURA_UNDEAD:
			case GROUND:
				target = _creature;
				break;
			
			case SELF:
			case CORPSE_ALLY:
			case PET:
			case SUMMON:
			case OWNER_PET:
			case PARTY:
			case CLAN:
			case ALLY:
				doit = true;
			default:
				if (targets.length == 0)
				{
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					
					if (_creature instanceof Player)
						_creature.sendPacket(ActionFailed.STATIC_PACKET);
					
					_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
					
					return;
				}
				
				switch (skill.getSkillType())
				{
					case BUFF:
					case HEAL:
					case COMBATPOINTHEAL:
					case MANAHEAL:
					case SEED:
					case REFLECT:
						doit = true;
						break;
				}
				
				target = (doit) ? (Creature) targets[0] : (_creature.getAI().getCurrentIntention().getFirstParameter() == null ? (Creature) _creature.getTarget() : (Creature) ((SkillUseHolder) (_creature.getAI().getCurrentIntention().getFirstParameter())).getTarget());
		}
		beginCast(skill, simultaneously, target, targets);
	}
	
	private void beginCast(L2Skill skill, boolean simultaneously, Creature target, WorldObject[] targets)
	{
		if (target == null)
		{
			if (simultaneously)
				setIsCastingSimultaneouslyNow(false);
			else
				setIsCastingNow(false);
			
			if (_creature instanceof Player)
				_creature.sendPacket(ActionFailed.STATIC_PACKET);
			
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			return;
		}
		
		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();
		
		final boolean effectWhileCasting = skill.getSkillType() == SkillType.FUSION || skill.getSkillType() == SkillType.SIGNET_CASTTIME;
		
		// Calculate the casting time of the skill (base + modifier of MAtkSpd). Don't modify the skill time for FUSION and static skills.
		if (!skill.isStaticHitTime())
		{
			if (!effectWhileCasting)
			{
				hitTime = Formulas.calcAtkSpd(_creature, skill, hitTime);
				if (coolTime > 0)
					coolTime = Formulas.calcAtkSpd(_creature, skill, coolTime);
				
				// Calculate altered Cast Speed due to BSpS/SpS
				if (skill.isMagic())
				{
					// Only takes 70% of the time to cast a BSpS/SpS cast
					if (_creature.isChargedShot(ShotType.SPIRITSHOT) || _creature.isChargedShot(ShotType.BLESSED_SPIRITSHOT))
					{
						hitTime = (int) (0.70 * hitTime);
						coolTime = (int) (0.70 * coolTime);
					}
				}
			}
			
			// if basic hitTime is higher than 500 than the min hitTime is 500
			if (skill.getHitTime() >= 500 && hitTime < 500)
				hitTime = 500;
		}
		
		// Set the _castInterruptTime and casting status.
		if (simultaneously)
		{
			setIsCastingSimultaneouslyNow(true);
			setLastSimultaneousSkillCast(skill);
		}
		else
		{
			setIsCastingNow(true);
			_castInterruptTime = System.currentTimeMillis() + hitTime - 200;
			setLastSkillCast(skill);
		}
		
		// Init the reuse time of the skill
		int reuseDelay = skill.getReuseDelay();
		
		if (!skill.isStaticReuse())
		{
			reuseDelay *= _creature.calcStat(skill.isMagic() ? Stats.MAGIC_REUSE_RATE : Stats.P_REUSE, 1, null, null);
			reuseDelay *= 333.0 / (skill.isMagic() ? _creature.getMAtkSpd() : _creature.getPAtkSpd());
		}
		
		final boolean skillMastery = Formulas.calcSkillMastery(_creature, skill);
		
		// Skill reuse check
		if (reuseDelay > 30000 && !skillMastery)
			_creature.addTimeStamp(skill, reuseDelay);
		
		// Check if this skill consume mp on start casting
		final int initmpcons = _creature.getStat().getMpInitialConsume(skill);
		if (initmpcons > 0)
		{
			_creature.getStatus().reduceMp(initmpcons);
			
			final StatusUpdate su = new StatusUpdate(_creature);
			su.addAttribute(StatusUpdate.CUR_MP, (int) _creature.getCurrentMp());
			_creature.sendPacket(su);
		}
		
		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if (reuseDelay > 10)
		{
			if (skillMastery)
			{
				reuseDelay = 100;
				
				if (_creature.getActingPlayer() != null)
					_creature.getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
			}
			
			_creature.disableSkill(skill, reuseDelay);
		}
		
		// Make sure that char is facing selected target
		if (target != _creature)
			_creature.getPosition().setHeadingTo(target);
		
		// For force buff skills, start the effect as long as the player is casting.
		if (effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the Creature
			if (skill.getItemConsumeId() > 0)
			{
				if (!_creature.destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					if (simultaneously)
						setIsCastingSimultaneouslyNow(false);
					else
						setIsCastingNow(false);
					
					_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
					
					return;
				}
			}
			
			if (skill.getSkillType() == SkillType.FUSION)
				_creature.startFusionSkill(target, skill);
			else
				callSkill(skill, targets);
		}
		
		// Get the Display Identifier for a skill that client can't display
		final int displayId = skill.getId();
		
		// Get the level of the skill
		int level = skill.getLevel();
		if (level < 1)
			level = 1;
		
		// Broadcast MagicSkillUse for non toggle skills.
		if (!skill.isToggle())
		{
			if (!skill.isPotion())
			{
				_creature.broadcastPacket(new MagicSkillUse(_creature, target, displayId, level, hitTime, reuseDelay, false));
				_creature.broadcastPacket(new MagicSkillLaunched(_creature, displayId, level, (targets == null || targets.length == 0) ? new WorldObject[]
				{
					target
				} : targets));
			}
			else
				_creature.broadcastPacket(new MagicSkillUse(_creature, target, displayId, level, 0, 0));
		}
		
		if (_creature instanceof Playable)
		{
			// Send a system message USE_S1 to the Creature
			if (_creature instanceof Player && skill.getId() != 1312)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.USE_S1);
				sm.addSkillName(skill);
				_creature.sendPacket(sm);
			}
			
			if (!effectWhileCasting && skill.getItemConsumeId() > 0)
			{
				if (!_creature.destroyItemByItemId("Consume", skill.getItemConsumeId(), skill.getItemConsume(), null, true))
				{
					_creature.getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					stop();
					return;
				}
			}
			
			// Before start AI Cast Broadcast Fly Effect is Need
			if (skill.getFlyType() != null)
				ThreadPool.schedule(() ->
				{
					_creature.broadcastPacket(new FlyToLocation(_creature, target, skill.getFlyType()));
					_creature.setXYZ(target);
				}, 50);
		}
		
		final MagicUseTask mut = new MagicUseTask(targets, skill, hitTime, coolTime, simultaneously);
		
		// launch the magic in hitTime milliseconds
		if (hitTime > 410)
		{
			// Send SetupGauge with the color of the gauge and the casting time
			if (_creature instanceof Player)
				_creature.sendPacket(new SetupGauge(GaugeColor.BLUE, hitTime));
			
			if (effectWhileCasting)
				mut.phase = 2;
			
			if (simultaneously)
			{
				final Future<?> future = _castTask2;
				if (future != null)
				{
					future.cancel(true);
					_castTask2 = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_castTask2 = ThreadPool.schedule(mut, hitTime - 400);
			}
			else
			{
				final Future<?> future = _castTask;
				if (future != null)
				{
					future.cancel(true);
					_castTask = null;
				}
				
				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_castTask = ThreadPool.schedule(mut, hitTime - 400);
			}
		}
		else
		{
			mut.hitTime = 0;
			onMagicLaunchedTimer(mut);
		}
	}
	
	/**
	 * Check if casting of skill is possible
	 * @param skill
	 * @return True if casting is possible
	 */
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if (skill == null || _creature.isSkillDisabled(skill))
		{
			// Send ActionFailed to the Player
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough MP
		if (_creature.getCurrentMp() < _creature.getStat().getMpConsume(skill) + _creature.getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
			
			// Send ActionFailed to the Player
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster has enough HP
		if (_creature.getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
			
			// Send ActionFailed to the Player
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Verify the different types of silence (magic and physic)
		if (!skill.isPotion() && ((skill.isMagic() && _creature.isMuted()) || (!skill.isMagic() && _creature.isPhysicalMuted())))
		{
			// Send ActionFailed to the Player
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster owns the weapon needed
		if (!skill.getWeaponDependancy(_creature))
		{
			// Send ActionFailed to the Player
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the spell consumes an Item
		if (skill.getItemConsumeId() > 0 && _creature.getInventory() != null)
		{
			// Get the ItemInstance consumed by the spell
			final ItemInstance requiredItems = _creature.getInventory().getItemByItemId(skill.getItemConsumeId());
			
			// Check if the caster owns enough consumed Item to cast
			if (requiredItems == null || requiredItems.getCount() < skill.getItemConsume())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if (skill.getSkillType() == SkillType.SUMMON)
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1);
					sm.addItemName(skill.getItemConsumeId());
					sm.addNumber(skill.getItemConsume());
					_creature.sendPacket(sm);
					return false;
				}
				
				_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NUMBER_INCORRECT));
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.<BR>
	 * <BR>
	 * @return
	 */
	public final boolean stop()
	{
		if (isCastingNow() || isCastingSimultaneouslyNow())
		{
			if (_castTask != null)
			{
				_castTask.cancel(true);
				_castTask = null;
			}
			
			if (_castTask2 != null)
			{
				_castTask2.cancel(true);
				_castTask2 = null;
			}
			
			if (_creature.getFusionSkill() != null)
				_creature.getFusionSkill().onCastAbort();
			
			final AbstractEffect effect = _creature.getFirstEffect(EffectType.SIGNET_GROUND);
			if (effect != null)
				effect.exit();
			
			if (_creature.isAllSkillsDisabled())
				_creature.enableAllSkills();
			
			setIsCastingNow(false);
			setIsCastingSimultaneouslyNow(false);
			
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			
			_creature.getAI().notifyEvent(AiEventType.FINISHED_CASTING, false, null);
			
			_creature.broadcastPacket(new MagicSkillCanceled(_creature.getObjectId()));
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		return false;
	}
	
	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature.
	 */
	public void interrupt()
	{
		// damage can only cancel magical skills
		if (canAbortCast() && getLastSkillCast() != null && getLastSkillCast().isMagic())
		{
			// Abort the cast of the Creature and send Server->Client MagicSkillCanceld/ActionFailed packet.
			if (stop())
				_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTING_INTERRUPTED));
		}
	}
	
	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Broadcast MagicSkillLaunched packet (to display magic skill animation)</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send StatusUpdate with MP modification to the Player</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with ATTACK</li>
	 * <li>Notify the AI of the Creature with EVT_FINISHED_CASTING</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT>
	 * @param mut
	 */
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		final WorldObject[] targets = mut.targets;
		
		if (skill == null || targets == null)
		{
			stop();
			return;
		}
		
		if (targets.length == 0)
		{
			switch (skill.getTargetType())
			{
				// only AURA-type skills can be cast without target
				case AURA:
				case FRONT_AURA:
				case BEHIND_AURA:
				case AURA_UNDEAD:
					break;
				
				default:
					stop();
					return;
			}
		}
		
		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if (skill.getEffectRange() > escapeRange)
			escapeRange = skill.getEffectRange();
		else if (skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
			escapeRange = skill.getSkillRadius();
		
		if (targets.length > 0 && escapeRange > 0)
		{
			int _skiprange = 0;
			int _skipgeo = 0;
			int _skippeace = 0;
			final List<Creature> targetList = new ArrayList<>(targets.length);
			for (final WorldObject target : targets)
			{
				if (target instanceof Creature)
				{
					if (!MathUtil.checkIfInRange(escapeRange, _creature, target, true))
					{
						_skiprange++;
						continue;
					}
					
					if (skill.getSkillRadius() > 0 && skill.isOffensive() && !GeoEngine.getInstance().canSeeTarget(_creature, target))
					{
						_skipgeo++;
						continue;
					}
					
					if (skill.isOffensive() && _creature.isInsidePeaceZone(target))
					{
						_skippeace++;
						continue;
					}
					targetList.add((Creature) target);
				}
			}
			
			if (targetList.isEmpty())
			{
				if (_creature instanceof Player)
				{
					if (_skiprange > 0)
						_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED));
					else if (_skipgeo > 0)
						_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
					else if (_skippeace > 0)
						_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IN_PEACEZONE));
				}
				stop();
				return;
			}
			mut.targets = targetList.toArray(new Creature[targetList.size()]);
		}
		
		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Potions can be used while faking death.
		if ((mut.simultaneously && !isCastingSimultaneouslyNow()) || (!mut.simultaneously && !isCastingNow()) || (_creature.isAlikeDead() && !skill.isPotion()))
		{
			// now cancels both, simultaneous and normal
			_creature.getAI().notifyEvent(AiEventType.CANCEL, null, null);
			return;
		}
		
		mut.phase = 2;
		if (mut.hitTime == 0)
			onMagicHitTimer(mut);
		else
			_castTask = ThreadPool.schedule(mut, 400);
	}
	
	/*
	 * Runs in the end of skill casting
	 */
	public void onMagicHitTimer(MagicUseTask mut)
	{
		final L2Skill skill = mut.skill;
		final WorldObject[] targets = mut.targets;
		
		if (skill == null || targets == null)
		{
			stop();
			return;
		}
		
		if (_creature.getFusionSkill() != null)
		{
			if (mut.simultaneously)
			{
				_castTask2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_castTask = null;
				setIsCastingNow(false);
			}
			_creature.getFusionSkill().onCastAbort();
			_creature.notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}
		
		final AbstractEffect effect = _creature.getFirstEffect(EffectType.SIGNET_GROUND);
		if (effect != null)
		{
			if (mut.simultaneously)
			{
				_castTask2 = null;
				setIsCastingSimultaneouslyNow(false);
			}
			else
			{
				_castTask = null;
				setIsCastingNow(false);
			}
			effect.exit();
			
			_creature.notifyQuestEventSkillFinished(skill, targets[0]);
			_creature.getAI().notifyEvent(AiEventType.FINISHED_CASTING, true, null);
			return;
		}
		
		// Go through targets table
		for (final WorldObject tgt : targets)
		{
			if (tgt instanceof Playable)
			{
				if (skill.getSkillType() == SkillType.BUFF || skill.getSkillType() == SkillType.FUSION || skill.getSkillType() == SkillType.SEED)
					((Creature) tgt).sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
				
				if (_creature instanceof Player && tgt instanceof Summon)
					((Summon) tgt).updateAndBroadcastStatus(1);
			}
		}
		
		// Recharge any active auto soulshot tasks for current Creature.
		_creature.rechargeShots(skill.useSoulShot(), skill.useSpiritShot());
		
		final StatusUpdate su = new StatusUpdate(_creature);
		boolean isSendStatus = false;
		
		// Consume MP of the Creature and Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		final double mpConsume = _creature.getStat().getMpConsume(skill);
		if (mpConsume > 0)
		{
			if (mpConsume > _creature.getCurrentMp())
			{
				_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
				stop();
				return;
			}
			
			_creature.getStatus().reduceMp(mpConsume);
			su.addAttribute(StatusUpdate.CUR_MP, (int) _creature.getCurrentMp());
			isSendStatus = true;
		}
		
		// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
		final double hpConsume = skill.getHpConsume();
		if (hpConsume > 0)
		{
			if (hpConsume > _creature.getCurrentHp())
			{
				_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_HP));
				stop();
				return;
			}
			
			_creature.getStatus().reduceHp(hpConsume, _creature, true);
			su.addAttribute(StatusUpdate.CUR_HP, (int) _creature.getCurrentHp());
			isSendStatus = true;
		}
		
		// Send StatusUpdate with MP modification to the Player
		if (isSendStatus)
			_creature.sendPacket(su);
		
		if (_creature instanceof Player)
		{
			// check for charges
			final int charges = ((Player) _creature).getCharges();
			if (skill.getMaxCharges() == 0 && charges < skill.getNumCharges())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(skill);
				_creature.sendPacket(sm);
				stop();
				return;
			}
			
			// generate charges if any
			if (skill.getNumCharges() > 0)
			{
				if (skill.getMaxCharges() > 0)
					((Player) _creature).increaseCharges(skill.getNumCharges(), skill.getMaxCharges());
				else
					((Player) _creature).decreaseCharges(skill.getNumCharges());
			}
		}
		
		// Launch the magic skill in order to calculate its effects
		callSkill(mut.skill, mut.targets);
		
		mut.phase = 3;
		if (mut.hitTime == 0 || mut.coolTime == 0)
			onMagicFinalizer(mut);
		else
		{
			if (mut.simultaneously)
				_castTask2 = ThreadPool.schedule(mut, mut.coolTime);
			else
				_castTask = ThreadPool.schedule(mut, mut.coolTime);
		}
	}
	
	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if (mut.simultaneously)
		{
			_castTask2 = null;
			setIsCastingSimultaneouslyNow(false);
			return;
		}
		
		_castTask = null;
		_castInterruptTime = 0;
		
		setIsCastingNow(false);
		setIsCastingSimultaneouslyNow(false);
		
		// Recharge any active auto soulshot tasks for current Creature.
		_creature.rechargeShots(mut.skill.useSoulShot(), mut.skill.useSpiritShot());
		
		final L2Skill skill = mut.skill;
		final WorldObject target = mut.targets.length > 0 ? mut.targets[0] : null;
		
		if (skill.isOffensive() && !(skill.getSkillType() == SkillType.UNLOCK) && !(skill.getSkillType() == SkillType.DELUXE_KEY_UNLOCK))
			_creature.getAI().startAttackStance();
		
		_creature.getAI().notifyEvent(AiEventType.FINISHED_CASTING, true, null);
		
		_creature.notifyQuestEventSkillFinished(skill, target);
		
		// Wipe current cast state.
		if (_creature instanceof Playable)
		{
			if (_creature instanceof Player)
				_creature.getActingPlayer().setCurrentSkill(null, null, false, false);
			else
				_creature.getActingPlayer().setCurrentPetSkill(null, null, false, false);
		}
	}
	
	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.
	 * @param skill The L2Skill to use
	 * @param targets The table of WorldObject targets
	 */
	public void callSkill(L2Skill skill, WorldObject[] targets)
	{
		try
		{
			// Check if the toggle skill effects are already in progress on the Creature
			if (skill.isToggle() && _creature.getFirstEffect(skill.getId()) != null)
				return;
			
			// Initial checks
			for (final WorldObject trg : targets)
			{
				if (!(trg instanceof Creature))
					continue;
				
				// Set some values inside target's instance for later use
				final Creature target = (Creature) trg;
				
				if (_creature instanceof Playable)
				{
					// Raidboss curse.
					if (!Config.RAID_DISABLE_CURSE)
					{
						// Target must be a raid type. // TODO move the whole into onSkillSee
						if (target.isRaidRelated() && _creature.getLevel() > target.getLevel() + 8)
						{
							final L2Skill curse = FrequentSkill.RAID_CURSE.getSkill();
							if (curse != null)
							{
								// Send visual and skill effects. Caster is the victim.
								_creature.broadcastPacket(new MagicSkillUse(_creature, _creature, curse.getId(), curse.getLevel(), 300, 0));
								curse.getEffects(_creature, _creature);
							}
							return;
						}
					}
					
					// Check if over-hit is possible.
					if (skill.isOverhit() && target instanceof Monster)
						((Monster) target).getOverhitState().set(true);
				}
				
				switch (skill.getSkillType())
				{
					case COMMON_CRAFT: // Crafting does not trigger any chance skills.
					case DWARVEN_CRAFT:
						break;
					
					default: // Launch weapon Special ability skill effect if available
						final Weapon activeWeaponItem = _creature.getActiveWeaponItem();
						if (activeWeaponItem != null && !target.isDead())
							activeWeaponItem.castSkillOnMagic(_creature, target, skill);
						
						// Maybe launch chance skills on us
						if (_creature.getChanceSkills() != null)
							_creature.getChanceSkills().onSkillHit(target, false, skill.isMagic(), skill.isOffensive());
						
						// Maybe launch chance skills on target
						if (target.getChanceSkills() != null)
							target.getChanceSkills().onSkillHit(_creature, true, skill.isMagic(), skill.isOffensive());
				}
			}
			
			// Launch the magic skill and calculate its effects
			final ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			if (handler != null)
				handler.useSkill(_creature, skill, targets);
			else
				skill.useSkill(_creature, targets);
			
			final Player player = _creature.getActingPlayer();
			if (player != null)
			{
				for (final WorldObject target : targets)
				{
					if (target instanceof Creature)
					{
						if (skill.isOffensive())
						{
							if (player.getSummon() != target)
								player.updatePvPStatus((Creature) target);
						}
						else
						{
							if (target instanceof Player)
							{
								// Casting non offensive skill on player with pvp flag set or with karma
								if (!(target.equals(_creature) || target.equals(player)) && (((Player) target).getPvpFlag() > 0 || ((Player) target).getKarma() > 0))
									player.updatePvPStatus();
							}
							else if (target instanceof Attackable && !((Attackable) target).isGuard())
							{
								switch (skill.getSkillType())
								{
									case SUMMON:
									case BEAST_FEED:
									case UNLOCK:
									case UNLOCK_SPECIAL:
									case DELUXE_KEY_UNLOCK:
										break;
									
									default:
										player.updatePvPStatus();
								}
							}
						}
						
						switch (skill.getTargetType())
						{
							case CORPSE_MOB:
							case AREA_CORPSE_MOB:
								if (((Creature) target).isDead())
									((Npc) target).endDecayTask();
								break;
						}
					}
				}
				
				// Mobs in range 1000 see spell
				for (final Npc npcMob : player.getKnownTypeInRadius(Npc.class, 1000))
				{
					final List<Quest> scripts = npcMob.getTemplate().getEventQuests(ScriptEventType.ON_SKILL_SEE);
					if (scripts != null)
						for (final Quest quest : scripts)
							quest.notifySkillSee(npcMob, player, skill, targets, _creature instanceof Summon);
				}
			}
			
			// Notify AI
			if (skill.isOffensive())
			{
				switch (skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREMOVE:
						break;
					
					default:
						for (final WorldObject target : targets)
						{
							// notify target AI about the attack
							if (target instanceof Creature && ((Creature) target).hasAI())
								((Creature) target).getAI().notifyEvent(AiEventType.ATTACKED, _creature, null);
						}
						break;
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("Couldn't call skill {}.", e, (skill == null) ? "not found" : skill.getId());
		}
	}
	
	/** Task lauching the magic skill phases */
	class MagicUseTask implements Runnable
	{
		WorldObject[] targets;
		L2Skill skill;
		int hitTime;
		int coolTime;
		int phase;
		boolean simultaneously;
		
		public MagicUseTask(WorldObject[] tgts, L2Skill s, int hit, int coolT, boolean simultaneous)
		{
			targets = tgts;
			skill = s;
			phase = 1;
			hitTime = hit;
			coolTime = coolT;
			simultaneously = simultaneous;
		}
		
		@Override
		public void run()
		{
			try
			{
				switch (phase)
				{
					case 1:
						onMagicLaunchedTimer(this);
						break;
					
					case 2:
						onMagicHitTimer(this);
						break;
					
					case 3:
						onMagicFinalizer(this);
						break;
					
					default:
						break;
				}
			}
			catch (final Exception e)
			{
				LOGGER.error("Failed executing MagicUseTask on phase {} for skill {}.", e, phase, (skill == null) ? "not found" : skill.getName());
				
				if (simultaneously)
					setIsCastingSimultaneouslyNow(false);
				else
					setIsCastingNow(false);
			}
		}
	}
}