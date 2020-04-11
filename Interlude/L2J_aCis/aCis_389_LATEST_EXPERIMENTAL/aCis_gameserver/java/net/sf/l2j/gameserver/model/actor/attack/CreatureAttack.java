package net.sf.l2j.gameserver.model.actor.attack;

import java.util.List;
import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.GaugeColor;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.container.creature.ChanceSkillList;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.Attack;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * This class groups all attack data related to a {@link Creature}.
 */
public class CreatureAttack
{
	protected final Creature _creature;
	
	protected Future<?> _hitTask;
	protected Future<?> _bowReuseTask;
	
	public CreatureAttack(Creature creature)
	{
		_creature = creature;
	}
	
	/**
	 * Manage hit process (called by Hit Task).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send ActionFailed (if attacker is a Player)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li>
	 * </ul>
	 * @param target The Creature targeted
	 * @param hitTaskHolder1 The HitTaskHolder for the first attack (if it's a dual attack)
	 * @param hitTaskHolder2 The HitTaskHolder for the second attack (if it's a dual attack)
	 * @param soulshot Soulshot usage
	 * @param afterAttackDelay The Delay after which to trigger the second attack (if it's a dual attack) and the delay until the Creature has FINISHED ATTACKING
	 */
	protected void onHitTimer(Creature target, HitTaskHolder hitTaskHolder1, HitTaskHolder hitTaskHolder2, boolean soulshot, int afterAttackDelay)
	{
		final boolean isBow = (_creature.getAttackType() == WeaponType.BOW);
		
		if (_creature.getCast().isCastingNow() || _creature.cantAttack())
		{
			if (_hitTask != null)
			{
				_hitTask.cancel(false);
				_hitTask = null;
			}
			
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			return;
		}
		
		_creature.rechargeShots(true, false);
		
		if (target == null || _creature.isAlikeDead())
		{
			if (_hitTask != null)
			{
				_hitTask.cancel(false);
				_hitTask = null;
			}
			
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			return;
		}
		
		if ((_creature instanceof Npc && target.isAlikeDead()) || target.isDead() || (!_creature.knows(target) && !(_creature instanceof Door)))
		{
			if (_hitTask != null)
			{
				_hitTask.cancel(false);
				_hitTask = null;
			}
			
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (hitTaskHolder1._miss)
		{
			// Notify target AI
			if (target.hasAI())
				target.getAI().notifyEvent(AiEventType.EVADED, _creature, null);
			
			// ON_EVADED_HIT
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onEvadedHit(_creature);
			
			if (target instanceof Player)
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(_creature));
		}
		
		// Send message about damage/crit or miss
		_creature.sendDamageMessage(target, hitTaskHolder1._damage, false, hitTaskHolder1._crit, hitTaskHolder1._miss);
		
		// Character will be petrified if attacking a raid related object that's more than 8 levels lower
		if (!Config.RAID_DISABLE_CURSE && target.isRaidRelated() && _creature.getLevel() > target.getLevel() + 8)
		{
			final L2Skill skill = FrequentSkill.RAID_CURSE2.getSkill();
			if (skill != null)
			{
				// Send visual and skill effects. Caster is the victim.
				_creature.broadcastPacket(new MagicSkillUse(_creature, _creature, skill.getId(), skill.getLevel(), 300, 0));
				skill.getEffects(_creature, _creature);
			}
			
			hitTaskHolder1._damage = 0; // prevents messing up drop calculation
		}
		
		if (!hitTaskHolder1._miss && hitTaskHolder1._damage > 0)
		{
			_creature.getAI().startAttackStance();
			
			if (target.hasAI())
				target.getAI().notifyEvent(AiEventType.ATTACKED, _creature, null);
			
			int reflectedDamage = 0;
			
			// Reflect damage system - do not reflect if weapon is a bow or target is invulnerable
			if (!isBow && !target.isInvul())
			{
				// quick fix for no drop from raid if boss attack high-level char with damage reflection
				if (!target.isRaidRelated() || _creature.getActingPlayer() == null || _creature.getActingPlayer().getLevel() <= target.getLevel() + 8)
				{
					// Calculate reflection damage to reduce HP of attacker if necessary
					final double reflectPercent = target.getStat().calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0, null, null);
					if (reflectPercent > 0)
					{
						reflectedDamage = (int) (reflectPercent / 100. * hitTaskHolder1._damage);
						
						if (reflectedDamage > target.getMaxHp())
							reflectedDamage = target.getMaxHp();
					}
				}
			}
			
			// Reduce target HPs
			target.reduceCurrentHp(hitTaskHolder1._damage, _creature, null);
			
			// Reduce attacker HPs in case of a reflect.
			if (reflectedDamage > 0)
				_creature.reduceCurrentHp(reflectedDamage, target, true, false, null);
			
			if (!isBow) // Do not absorb if weapon is of type bow
			{
				// Absorb HP from the damage inflicted
				final double absorbPercent = _creature.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);
				
				if (absorbPercent > 0)
				{
					final int maxCanAbsorb = (int) (_creature.getMaxHp() - _creature.getCurrentHp());
					int absorbDamage = (int) (absorbPercent / 100. * hitTaskHolder1._damage);
					
					if (absorbDamage > maxCanAbsorb)
						absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						
					if (absorbDamage > 0)
						_creature.setCurrentHp(_creature.getCurrentHp() + absorbDamage);
				}
			}
			
			// Manage cast break of the target (calculating rate, sending message...)
			Formulas.calcCastBreak(target, hitTaskHolder1._damage);
			
			// Maybe launch chance skills on us
			final ChanceSkillList chanceSkills = _creature.getChanceSkills();
			if (chanceSkills != null)
			{
				chanceSkills.onHit(target, false, hitTaskHolder1._crit);
				
				// Reflect triggers onHit
				if (reflectedDamage > 0)
					chanceSkills.onHit(target, true, false);
			}
			
			// Maybe launch chance skills on target
			if (target.getChanceSkills() != null)
				target.getChanceSkills().onHit(_creature, true, hitTaskHolder1._crit);
			
			// Launch weapon Special ability effect if available
			if (hitTaskHolder1._crit)
			{
				final Weapon activeWeapon = _creature.getActiveWeaponItem();
				if (activeWeapon != null)
					activeWeapon.castSkillOnCrit(_creature, target);
			}
		}
		
		if (hitTaskHolder2 != null)
			_hitTask = ThreadPool.schedule(new HitTask(target, hitTaskHolder2, null, soulshot, afterAttackDelay), afterAttackDelay);
		else
		{
			if (isBow)
			{
				if (_hitTask != null)
				{
					_hitTask.cancel(false);
					_hitTask = null;
				}
				
				_creature.getAI().notifyEvent(AiEventType.FINISHED_ATTACK_BOW, null, null);
				
				_bowReuseTask = ThreadPool.schedule(() ->
				{
					if (_bowReuseTask != null)
					{
						_bowReuseTask.cancel(false);
						_bowReuseTask = null;
					}
					
					_creature.getAI().notifyEvent(AiEventType.BOW_ATTACK_REUSED, null, null);
				}, afterAttackDelay);
			}
			else
			{
				_hitTask = ThreadPool.schedule(() ->
				{
					if (_hitTask != null)
					{
						_hitTask.cancel(false);
						_hitTask = null;
					}
					
					_creature.getAI().notifyEvent(AiEventType.FINISHED_ATTACK, null, null);
				}, afterAttackDelay);
			}
		}
	}
	
	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Get the active weapon (always equipped in the right hand)</li>
	 * </ul>
	 * <ul>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the Player with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use</li>
	 * </ul>
	 * <ul>
	 * <li>Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)</li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the Creature AND to all Player in the _KnownPlayers of the Creature</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li>
	 * </ul>
	 * @param target The Creature targeted
	 */
	public void doAttack(Creature target)
	{
		if (target == null || _creature.isAttackingDisabled())
		{
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!_creature.isAlikeDead())
		{
			if (_creature instanceof Npc && (target.isAlikeDead() || !_creature.knows(target)))
			{
				_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
				_creature.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (_creature instanceof Playable && target.isDead())
			{
				_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
				_creature.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		
		final Player player = _creature.getActingPlayer();
		
		if (player != null && player.isInObserverMode())
		{
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE));
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Checking if target has moved to peace zone
		if (_creature.isInsidePeaceZone(target))
		{
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Get the active weapon item corresponding to the active weapon instance (always equipped in the right hand)
		final Weapon weaponItem = _creature.getActiveWeaponItem();
		final WeaponType weaponItemType = _creature.getAttackType();
		
		// You can't make an attack with a fishing pole.
		if (weaponItemType == WeaponType.FISHINGROD)
		{
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE));
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// GeoData Los Check here (or dz > 1000)
		if (!GeoEngine.getInstance().canSeeTarget(_creature, target))
		{
			_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Bow checks.
		if (weaponItemType == WeaponType.BOW)
		{
			if (_creature instanceof Player)
			{
				// Equip needed arrows in left hand ; if it's not possible, cancel the action.
				if (!_creature.checkAndEquipArrows())
				{
					_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
					_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_ARROWS));
					_creature.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the bow can be used. Cancel the action if the bow can't be re-use at this moment.
				if (!isBowAttackReused())
				{
					_creature.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				
				// Verify if the Player owns enough MP. If not, stop the attack.
				final int mpConsume = weaponItem.getMpConsume();
				if (mpConsume > 0)
				{
					if (_creature.getCurrentMp() < mpConsume)
					{
						_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
						_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_MP));
						_creature.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
					_creature.getStatus().reduceMp(mpConsume);
				}
			}
			else if (_creature instanceof Npc)
			{
				if (!isBowAttackReused())
				{
					_creature.getAI().tryTo(IntentionType.ACTIVE, null, null);
					return;
				}
			}
		}
		
		// Get the Attack Speed of the Creature (delay (in milliseconds) before next attack)
		final int timeAtk = Formulas.calculateTimeBetweenAttacks(_creature);
		
		// Create Attack
		final Attack attack = new Attack(_creature, _creature.isChargedShot(ShotType.SOULSHOT), (weaponItem != null) ? weaponItem.getCrystalType().getId() : 0);
		
		// Make sure that char is facing selected target
		_creature.getPosition().setHeadingTo(target);
		
		boolean hitted;
		
		// Select the type of attack to start
		switch (weaponItemType)
		{
			case BOW:
				hitted = doAttackHitByBow(attack, target, timeAtk, weaponItem);
				break;
			
			case POLE:
				hitted = doAttackHitByPole(attack, target, timeAtk / 2);
				break;
			
			case DUAL:
			case DUALFIST:
				hitted = doAttackHitByDual(attack, target, timeAtk / 2);
				break;
			
			case FIST:
				hitted = (_creature.getSecondaryWeaponItem() instanceof Armor) ? doAttackHitSimple(attack, target, timeAtk / 2) : doAttackHitByDual(attack, target, timeAtk / 2);
				break;
			
			default:
				hitted = doAttackHitSimple(attack, target, timeAtk / 2);
				break;
		}
		
		// Refresh PvP status of the attacker.
		if (player != null && player.getSummon() != target)
			player.updatePvPStatus(target);
		
		// Check if hit isn't missed
		if (hitted)
		{
			// IA implementation for ON_ATTACK_ACT (mob which attacks a player).
			if (_creature instanceof Attackable)
			{
				// Bypass behavior if the victim isn't a player
				final Player victim = target.getActingPlayer();
				if (victim != null)
				{
					final Npc mob = ((Npc) _creature);
					
					final List<Quest> scripts = mob.getTemplate().getEventQuests(ScriptEventType.ON_ATTACK_ACT);
					if (scripts != null)
						for (final Quest quest : scripts)
							quest.notifyAttackAct(mob, victim);
				}
			}
			
			// If we didn't miss the hit, discharge the shoulshots, if any
			_creature.setChargedShot(ShotType.SOULSHOT, false);
			
			if (player != null)
			{
				if (player.isCursedWeaponEquipped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if (!target.isInvul())
						target.setCurrentCp(0);
				}
				else if (player.isHero())
				{
					if (target instanceof Player && ((Player) target).isCursedWeaponEquipped())
						// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
						target.setCurrentCp(0);
				}
			}
		}
		
		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the Creature AND to all Player in the _KnownPlayers of the Creature
		if (attack.hasHits())
			_creature.broadcastPacket(attack);
	}
	
	/**
	 * Launch a Bow attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>Consumme arrows</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>If the Creature is a Player, Send SetupGauge</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @param weapon The weapon, which is attacker using
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, Creature target, int sAtk, Weapon weapon)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.calcHitMiss(_creature, target);
		
		// Consume arrows
		_creature.reduceArrowCount();
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(_creature, target, null);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(_creature.getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(_creature, target, null, shld1, crit1, attack.soulshot);
		}
		
		// Get the Attack Reuse Delay of the Weapon
		int reuse = weapon.getReuseDelay();
		if (reuse != 0)
			reuse = (reuse * 345) / _creature.getStat().getPAtkSpd();
		
		// Check if the Creature is a Player
		if (_creature instanceof Player)
		{
			// Send a system message
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GETTING_READY_TO_SHOOT_AN_ARROW));
			
			// Send SetupGauge
			_creature.sendPacket(new SetupGauge(GaugeColor.RED, sAtk + reuse));
		}
		
		// The bow attack must have AfterAttackDelay = 0, to allow movement when the red gauge bar reaches 50%.
		_hitTask = ThreadPool.schedule(new HitTask(target, new HitTaskHolder(damage1, crit1, miss1, shld1), null, attack.soulshot, reuse), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * Launch a Dual attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Calculate if hits are missed or not</li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient</li>
	 * <li>If hits aren't missed, calculate if hit is critical</li>
	 * <li>If hits aren't missed, calculate physical damages</li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		
		// Calculate if hits are missed or not
		final boolean miss1 = Formulas.calcHitMiss(_creature, target);
		final boolean miss2 = Formulas.calcHitMiss(_creature, target);
		
		// Check if hit 1 isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Formulas.calcShldUse(_creature, target, null);
			
			// Calculate if hit 1 is critical
			crit1 = Formulas.calcCrit(_creature.getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 1
			damage1 = (int) Formulas.calcPhysDam(_creature, target, null, shld1, crit1, attack.soulshot);
			damage1 /= 2;
		}
		
		// Check if hit 2 isn't missed
		if (!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Formulas.calcShldUse(_creature, target, null);
			
			// Calculate if hit 2 is critical
			crit2 = Formulas.calcCrit(_creature.getStat().getCriticalHit(target, null));
			
			// Calculate physical damages of hit 2
			damage2 = (int) Formulas.calcPhysDam(_creature, target, null, shld2, crit2, attack.soulshot);
			damage2 /= 2;
		}
		
		_hitTask = ThreadPool.schedule(new HitTask(target, new HitTaskHolder(damage1, crit1, miss1, shld1), new HitTaskHolder(damage2, crit2, miss2, shld2), attack.soulshot, sAtk / 2), sAtk / 2);
		
		// Add those hits to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2));
		
		// Return true if hit 1 or hit 2 isn't missed
		return (!miss1 || !miss2);
	}
	
	/**
	 * Launch a Pole attack.<BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Get all visible objects in a spherical area near the Creature to obtain possible targets</li>
	 * <li>If possible target is the Creature targeted, launch a simple attack against it</li>
	 * <li>If possible target isn't the Creature targeted but is attackable, launch a simple attack against it</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, Creature target, int sAtk)
	{
		final int maxRadius = _creature.getPhysicalAttackRange();
		final int maxAngleDiff = (int) _creature.getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);
		
		// Get the number of targets (-1 because the main target is already used)
		final int attackRandomCountMax = (int) _creature.getStat().calcStat(Stats.ATTACK_COUNT_MAX, 0, null, null) - 1;
		int attackcount = 0;
		
		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		
		final Creature attackTarget = (Creature) _creature.getAI().getCurrentIntention().getFirstParameter();
		
		for (final Creature obj : _creature.getKnownType(Creature.class))
		{
			if (obj == target || obj.isAlikeDead())
				continue;
			
			if (_creature instanceof Player)
			{
				if (obj instanceof Pet && ((Pet) obj).getOwner() == ((Player) _creature))
					continue;
			}
			else if (_creature instanceof Attackable)
			{
				if (obj instanceof Player && _creature.getTarget() instanceof Attackable)
					continue;
				
				if (obj instanceof Attackable && !_creature.isConfused())
					continue;
			}
			
			if (!MathUtil.checkIfInRange(maxRadius, _creature, obj, false))
				continue;
			
			// otherwise hit too high/low. 650 because mob z coord sometimes wrong on hills
			if (Math.abs(obj.getZ() - _creature.getZ()) > 650)
				continue;
			
			if (!_creature.isFacing(obj, maxAngleDiff))
				continue;
			
			// Launch an attack on each character, until attackRandomCountMax is reached.
			if (obj == attackTarget || obj.isAutoAttackable(_creature))
			{
				attackcount++;
				if (attackcount > attackRandomCountMax)
					break;
				
				hitted |= doAttackHitSimple(attack, obj, attackpercent, sAtk);
				attackpercent /= 1.15;
			}
		}
		// Return true if one hit isn't missed
		return hitted;
	}
	
	/**
	 * Launch a simple attack.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Calculate if hit is missed or not</li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient</li>
	 * <li>If hit isn't missed, calculate if hit is critical</li>
	 * <li>If hit isn't missed, calculate physical damages</li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack</li>
	 * </ul>
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The Creature targeted
	 * @param sAtk The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, Creature target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}
	
	private boolean doAttackHitSimple(Attack attack, Creature target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;
		
		// Calculate if hit is missed or not
		final boolean miss1 = Formulas.calcHitMiss(_creature, target);
		
		// Check if hit isn't missed
		if (!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Formulas.calcShldUse(_creature, target, null);
			
			// Calculate if hit is critical
			crit1 = Formulas.calcCrit(_creature.getStat().getCriticalHit(target, null));
			
			// Calculate physical damages
			damage1 = (int) Formulas.calcPhysDam(_creature, target, null, shld1, crit1, attack.soulshot);
			
			if (attackpercent != 100)
				damage1 = (int) (damage1 * attackpercent / 100);
		}
		
		// Create a new hit task with Medium priority
		_hitTask = ThreadPool.schedule(new HitTask(target, new HitTaskHolder(damage1, crit1, miss1, shld1), null, attack.soulshot, sAtk), sAtk);
		
		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));
		
		// Return true if hit isn't missed
		return !miss1;
	}
	
	/**
	 * @return True if the {@link Creature} is attacking.
	 */
	public boolean isAttackingNow()
	{
		return _hitTask != null;
	}
	
	public boolean isBowAttackReused()
	{
		return _bowReuseTask == null;
	}
	
	/**
	 * Abort the current attack of the {@link Creature} and send {@link ActionFailed} packet.
	 * @return True if an attack was existing and has been aborted, false otherwise.
	 */
	public final boolean stop()
	{
		if (_hitTask != null)
		{
			_hitTask.cancel(true);
			_hitTask = null;
			
			_creature.sendPacket(ActionFailed.STATIC_PACKET);
			return true;
		}
		return false;
	}
	
	/**
	 * Abort the current attack and send {@link SystemMessageId#ATTACK_FAILED} to the {@link Creature}.
	 */
	public void interrupt()
	{
		if (stop())
			_creature.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
	}
	
	class HitTaskHolder
	{
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		
		public HitTaskHolder(int damage, boolean crit, boolean miss, byte shld)
		{
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
		}
	}
	
	/**
	 * Task lauching the function onHitTimer().<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send ActionFailed (if attacker is a Player)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are Player</li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary</li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...)</li>
	 * </ul>
	 */
	class HitTask implements Runnable
	{
		Creature _target;
		HitTaskHolder _hitTaskHolder1;
		HitTaskHolder _hitTaskHolder2;
		boolean _soulshot;
		int _afterAttackDelay;
		
		public HitTask(Creature target, HitTaskHolder hitTaskHolder1, HitTaskHolder hitTaskHolder2, boolean soulshot, int afterAttackDelay)
		{
			_target = target;
			_hitTaskHolder1 = hitTaskHolder1;
			_hitTaskHolder2 = hitTaskHolder2;
			_soulshot = soulshot;
			_afterAttackDelay = afterAttackDelay;
		}
		
		@Override
		public void run()
		{
			onHitTimer(_target, _hitTaskHolder1, _hitTaskHolder2, _soulshot, _afterAttackDelay);
		}
	}
}