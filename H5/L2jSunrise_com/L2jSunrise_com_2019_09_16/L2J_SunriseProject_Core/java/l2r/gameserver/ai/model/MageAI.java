package l2r.gameserver.ai.model;

import static l2r.gameserver.enums.CtrlIntention.AI_INTENTION_ACTIVE;

import java.util.List;

import l2r.Config;
import l2r.gameserver.GameTimeController;
import l2r.gameserver.GeoData;
import l2r.gameserver.ai.L2AttackableAI;
import l2r.gameserver.enums.AIType;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.L2SkillType;
import l2r.gameserver.model.skills.targets.L2TargetType;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

/**
 * @author vGodFather
 */
public class MageAI extends L2AttackableAI
{
	public MageAI(L2Attackable creature)
	{
		super(creature);
	}
	
	@Override
	protected void thinkAttack()
	{
		final L2Attackable npc = getActiveChar();
		
		// vGodFather: this will fix broken attack animations from mobs
		if (npc.isCastingNow() || npc.isAttackingNow())
		{
			return;
		}
		
		if (npc.isCoreAIDisabled())
		{
			return;
		}
		
		final L2Character mostHate = npc.getMostHated();
		if (mostHate == null)
		{
			setIntention(AI_INTENTION_ACTIVE);
			return;
		}
		
		setAttackTarget(mostHate);
		npc.setTarget(mostHate);
		
		// Immobilize condition
		if (npc.isMovementDisabled())
		{
			movementDisable();
			return;
		}
		
		// Check if target is dead or if timeout is expired to stop this attack
		final L2Character originalAttackTarget = getAttackTarget();
		if ((originalAttackTarget == null) || originalAttackTarget.isAlikeDead() || (_attackTimeout < GameTimeController.getInstance().getGameTicks()))
		{
			// Stop hating this target after the attack timeout or if target is dead
			if (originalAttackTarget != null)
			{
				npc.stopHating(originalAttackTarget);
			}
			
			// Set the AI Intention to AI_INTENTION_ACTIVE
			// Retail you must give time to mobs before start go back
			npc.RANDOM_WALK_RATE = 15;
			setIntention(AI_INTENTION_ACTIVE);
			
			// tryToGoHome(npc);
			
			npc.setWalking();
			return;
		}
		
		// Initialize data
		final int collision = npc.getTemplate().getCollisionRadius();
		final int combinedCollision = collision + mostHate.getTemplate().getCollisionRadius();
		
		if (!npc.getTemplate().getSuicideSkills().isEmpty() && ((int) ((npc.getCurrentHp() / npc.getMaxHp()) * 100) < 30))
		{
			final L2Skill skill = npc.getTemplate().getSuicideSkills().get(Rnd.get(npc.getTemplate().getSuicideSkills().size()));
			if (Util.checkIfInRange(skill.getAffectRange(), getActiveChar(), mostHate, false) && npc.hasSkillChance())
			{
				if (cast(skill))
				{
					_log.debug("{} used suicide skill {}", this, skill);
					return;
				}
			}
		}
		
		if (!npc.isMovementDisabled())
		{
			// apply move around
			if (Rnd.nextInt(100) <= 3)
			{
				for (L2Object nearby : npc.getKnownList().getKnownObjects().values())
				{
					if ((nearby instanceof L2Attackable) && npc.isInsideRadius(nearby, collision, false, false) && (nearby != mostHate))
					{
						int newX = (Rnd.nextBoolean() ? mostHate.getX() + combinedCollision + Rnd.get(40) : (mostHate.getX() - combinedCollision) + Rnd.get(40));
						int newY = (Rnd.nextBoolean() ? mostHate.getY() + combinedCollision + Rnd.get(40) : (mostHate.getY() - combinedCollision) + Rnd.get(40));
						
						if (!npc.isInsideRadius(newX, newY, 0, collision, false, false))
						{
							int newZ = npc.getZ() + 30;
							Location loc = GeoData.getInstance().moveCheck(npc, new Location(newX, newY, newZ));
							moveTo(loc);
							return;
						}
					}
				}
			}
			
			// apply dodge
			if (npc.getCanDodge() > 0)
			{
				if (Rnd.get(100) <= npc.getCanDodge())
				{
					double distance2 = npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY());
					if (Math.sqrt(distance2) <= (60 + combinedCollision))
					{
						int newX = (Rnd.nextBoolean() ? mostHate.getX() + combinedCollision + Rnd.get(150) : (mostHate.getX() - combinedCollision) + Rnd.get(150));
						int newY = (Rnd.nextBoolean() ? mostHate.getY() + combinedCollision + Rnd.get(150) : (mostHate.getY() - combinedCollision) + Rnd.get(150));
						int posZ = npc.getZ() + 30;
						
						Location loc = GeoData.getInstance().moveCheck(npc, new Location(newX, newY, posZ));
						moveTo(loc);
						return;
					}
				}
			}
		}
		
		// BOSS/Raid Minion Target Reconsider
		if (npc.isRaid() || npc.isRaidMinion())
		{
			_chaosTime++;
			if (npc instanceof L2RaidBossInstance)
			{
				if (!((L2MonsterInstance) npc).hasMinions())
				{
					if (_chaosTime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 100) / npc.getMaxHp())))
						{
							aggroReconsider();
							_chaosTime = 0;
							return;
						}
					}
				}
				else
				{
					if (_chaosTime > Config.RAID_CHAOS_TIME)
					{
						if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
						{
							aggroReconsider();
							_chaosTime = 0;
							return;
						}
					}
				}
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				if (_chaosTime > Config.GRAND_CHAOS_TIME)
				{
					double chaosRate = 100 - ((npc.getCurrentHp() * 300) / npc.getMaxHp());
					if (((chaosRate <= 10) && (Rnd.get(100) <= 10)) || ((chaosRate > 10) && (Rnd.get(100) <= chaosRate)))
					{
						aggroReconsider();
						_chaosTime = 0;
						return;
					}
				}
			}
			else
			{
				if (_chaosTime > Config.MINION_CHAOS_TIME)
				{
					if (Rnd.get(100) <= (100 - ((npc.getCurrentHp() * 200) / npc.getMaxHp())))
					{
						aggroReconsider();
						_chaosTime = 0;
						return;
					}
				}
			}
		}
		
		final List<L2Skill> generalSkills = npc.getTemplate().getGeneralskills();
		if (!generalSkills.isEmpty())
		{
			// Heal Condition
			final List<L2Skill> aiHealSkills = npc.getTemplate().getHealSkills();
			generalSkills.removeAll(aiHealSkills);
			if (!aiHealSkills.isEmpty())
			{
				double percentage = (npc.getCurrentHp() / npc.getMaxHp()) * 100;
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && !leader.isDead() && (Rnd.get(100) > ((leader.getCurrentHp() / leader.getMaxHp()) * 100)))
					{
						for (L2Skill healSkill : aiHealSkills)
						{
							if (healSkill.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							
							if (!checkSkillCastConditions(npc, healSkill))
							{
								continue;
							}
							
							if (!Util.checkIfInRange((healSkill.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(healSkill) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, healSkill.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								final L2Object target = npc.getTarget();
								npc.setTarget(leader);
								npc.doCast(healSkill);
								npc.setTarget(target);
								_log.debug(this + " used heal skill " + healSkill + " on leader {}", leader);
								return;
							}
						}
					}
				}
				
				if (Rnd.get(100) < ((100 - percentage) / 3))
				{
					for (L2Skill sk : aiHealSkills)
					{
						if (!checkSkillCastConditions(npc, sk))
						{
							continue;
						}
						
						clientStopMoving(null);
						final L2Object target = npc.getTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						_log.debug("{} used heal skill {} on itself", this, sk);
						return;
					}
				}
				
				for (L2Skill sk : aiHealSkills)
				{
					if (!checkSkillCastConditions(npc, sk))
					{
						continue;
					}
					
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if (!(obj instanceof L2Attackable) || obj.isDead())
							{
								continue;
							}
							
							final L2Attackable targets = (L2Attackable) obj;
							if (!((L2Attackable) obj).isInMyClan(npc))
							{
								continue;
							}
							
							percentage = (targets.getCurrentHp() / targets.getMaxHp()) * 100;
							if (Rnd.get(100) < ((100 - percentage) / 10))
							{
								if (GeoData.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									final L2Object target = npc.getTarget();
									npc.setTarget(obj);
									npc.doCast(sk);
									npc.setTarget(target);
									_log.debug(this + " used heal skill " + sk + " on {}", obj);
									return;
								}
							}
						}
					}
					
					if (isParty(sk))
					{
						clientStopMoving(null);
						npc.doCast(sk);
						return;
					}
				}
			}
			
			// Res Skill Condition
			final List<L2Skill> aiResSkills = npc.getTemplate().getResSkills();
			generalSkills.removeAll(aiResSkills);
			if (!aiResSkills.isEmpty())
			{
				if (npc.isMinion())
				{
					L2Character leader = npc.getLeader();
					if ((leader != null) && leader.isDead())
					{
						for (L2Skill sk : aiResSkills)
						{
							if (sk.getTargetType() == L2TargetType.SELF)
							{
								continue;
							}
							
							if (!checkSkillCastConditions(npc, sk))
							{
								continue;
							}
							
							if (!Util.checkIfInRange((sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius()), npc, leader, false) && !isParty(sk) && !npc.isMovementDisabled())
							{
								moveToPawn(leader, sk.getCastRange() + collision + leader.getTemplate().getCollisionRadius());
								return;
							}
							
							if (GeoData.getInstance().canSeeTarget(npc, leader))
							{
								clientStopMoving(null);
								final L2Object target = npc.getTarget();
								npc.setTarget(leader);
								npc.doCast(sk);
								npc.setTarget(target);
								_log.debug(this + " used resurrection skill " + sk + " on leader {}", leader);
								return;
							}
						}
					}
				}
				
				for (L2Skill sk : aiResSkills)
				{
					if (!checkSkillCastConditions(npc, sk))
					{
						continue;
					}
					
					if (sk.getTargetType() == L2TargetType.ONE)
					{
						for (L2Character obj : npc.getKnownList().getKnownCharactersInRadius(sk.getCastRange() + collision))
						{
							if (!(obj instanceof L2Attackable) || !obj.isDead())
							{
								continue;
							}
							
							final L2Attackable targets = (L2Attackable) obj;
							if (!npc.isInMyClan(targets))
							{
								continue;
							}
							
							if (Rnd.get(100) < 10)
							{
								if (GeoData.getInstance().canSeeTarget(npc, targets))
								{
									clientStopMoving(null);
									final L2Object target = npc.getTarget();
									npc.setTarget(obj);
									npc.doCast(sk);
									npc.setTarget(target);
									_log.debug(this + " used heal skill " + sk + " on clan member {}", obj);
									return;
								}
							}
						}
					}
					if (isParty(sk))
					{
						clientStopMoving(null);
						final L2Object target = npc.getTarget();
						npc.setTarget(npc);
						npc.doCast(sk);
						npc.setTarget(target);
						_log.debug("{} used heal skill {} on party", this, sk);
						return;
					}
				}
			}
			
			if (npc.getAiType() == AIType.MAGE)
			{
				for (L2Skill skill : generalSkills)
				{
					if ((skill.getTargetType() == L2TargetType.ONE) && !skill.isPassive() && skill.isOffensive() && !(skill.getSkillType() == L2SkillType.BUFF))
					{
						if (!checkSkillCastConditions(npc, skill))
						{
							continue;
						}
						
						if (maybeMoveToPawn(npc.getTarget(), _actor.getMagicalAttackRange(skill)))
						{
							return;
						}
						
						clientStopMoving(null);
						npc.doCast(skill);
						return;
					}
				}
			}
		}
		
		double dist = Math.sqrt(npc.getPlanDistanceSq(mostHate.getX(), mostHate.getY()));
		int dist2 = (int) dist - collision;
		int range = npc.getPhysicalAttackRange() + combinedCollision;
		if (mostHate.isMoving())
		{
			range = range + 50;
			if (npc.isMoving())
			{
				range = range + 50;
			}
		}
		
		// Long/Short Range skill usage.
		if (!npc.getShortRangeSkills().isEmpty() && npc.hasSkillChance() && (dist2 <= 150))
		{
			final L2Skill shortRangeSkill = npc.getShortRangeSkills().get(Rnd.get(npc.getShortRangeSkills().size()));
			if (checkSkillCastConditions(npc, npc.getTarget(), shortRangeSkill))
			{
				clientStopMoving(null);
				npc.doCast(shortRangeSkill);
				_log.debug(this + " used short range skill " + shortRangeSkill + " on {}", npc.getTarget());
				return;
			}
		}
		
		if (!npc.getLongRangeSkills().isEmpty() && npc.hasSkillChance() && (dist2 > 150))
		{
			final L2Skill longRangeSkill = npc.getLongRangeSkills().get(Rnd.get(npc.getLongRangeSkills().size()));
			if (checkSkillCastConditions(npc, npc.getTarget(), longRangeSkill))
			{
				clientStopMoving(null);
				npc.doCast(longRangeSkill);
				_log.debug(this + " used long range skill " + longRangeSkill + " on {}", npc.getTarget());
				return;
			}
		}
		
		// Starts melee attack
		if ((dist2 > range) || !GeoData.getInstance().canSeeTarget(npc, mostHate))
		{
			if (npc.isMovementDisabled())
			{
				targetReconsider();
			}
			else
			{
				final L2Character target = getAttackTarget();
				if (target != null)
				{
					if (target.isMoving())
					{
						range -= 100;
					}
					
					moveToPawn(target, Math.max(range, 5));
				}
			}
			return;
		}
		
		// Attacks target
		_actor.doAttack(getAttackTarget());
	}
}
