package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlEvent;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.ISkillHandler;
import com.l2jfrozen.gameserver.handler.SkillHandler;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Formulas;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillDrain;
import com.l2jfrozen.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

public class L2CubicInstance
{
	protected static final Logger LOGGER = Logger.getLogger(L2CubicInstance.class);
	
	// Type of Cubics
	public static final int STORM_CUBIC = 1;
	public static final int VAMPIRIC_CUBIC = 2;
	public static final int LIFE_CUBIC = 3;
	public static final int VIPER_CUBIC = 4;
	public static final int POLTERGEIST_CUBIC = 5;
	public static final int BINDING_CUBIC = 6;
	public static final int AQUA_CUBIC = 7;
	public static final int SPARK_CUBIC = 8;
	public static final int ATTRACT_CUBIC = 9;
	public static final int SMART_CUBIC_EVATEMPLAR = 10;
	public static final int SMART_CUBIC_SHILLIENTEMPLAR = 11;
	public static final int SMART_CUBIC_ARCANALORD = 12;
	public static final int SMART_CUBIC_ELEMENTALMASTER = 13;
	public static final int SMART_CUBIC_SPECTRALMASTER = 14;
	
	// Max range of cubic skills
	/** The Constant MAX_MAGIC_RANGE. */
	public static final int MAX_MAGIC_RANGE = 900;
	
	// Cubic skills
	public static final int SKILL_CUBIC_HEAL = 4051;
	public static final int SKILL_CUBIC_CURE = 5579;
	
	protected L2PcInstance playerOwner;
	protected L2Character playerTarget;
	protected int cubicId;
	protected int matk;
	protected int activationTime;
	protected int activationChance;
	protected boolean active;
	private final boolean givenByOther;
	protected List<L2Skill> skills = new ArrayList<>();
	private Future<?> disappearTask;
	private Future<?> actionTask;
	
	/**
	 * Instantiates a new l2 cubic instance.
	 * @param owner            the owner
	 * @param id               the id
	 * @param level            the level
	 * @param mAtk             the m atk
	 * @param activationtime   the activationtime
	 * @param activationchance the activationchance
	 * @param totallifetime    the totallifetime
	 * @param givenByOther     the given by other
	 */
	public L2CubicInstance(final L2PcInstance owner, final int id, final int level, final int mAtk, final int activationtime, final int activationchance, final int totallifetime, final boolean givenByOther)
	{
		playerOwner = owner;
		cubicId = id;
		matk = mAtk;
		activationTime = activationtime * 1000;
		activationChance = activationchance;
		active = false;
		this.givenByOther = givenByOther;
		
		switch (cubicId)
		{
			case STORM_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4049, level));
				break;
			case VAMPIRIC_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4050, level));
				break;
			case LIFE_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4051, level));
				doAction();
				break;
			case VIPER_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4052, level));
				break;
			case POLTERGEIST_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4053, level));
				skills.add(SkillTable.getInstance().getInfo(4054, level));
				skills.add(SkillTable.getInstance().getInfo(4055, level));
				break;
			case BINDING_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4164, level));
				break;
			case AQUA_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4165, level));
				break;
			case SPARK_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(4166, level));
				break;
			case ATTRACT_CUBIC:
				skills.add(SkillTable.getInstance().getInfo(5115, level));
				skills.add(SkillTable.getInstance().getInfo(5116, level));
				break;
			case SMART_CUBIC_ARCANALORD:
				// skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				skills.add(SkillTable.getInstance().getInfo(4051, 7)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_ELEMENTALMASTER:
				skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(4166, 9)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SPECTRALMASTER:
				skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				skills.add(SkillTable.getInstance().getInfo(4052, 6)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_EVATEMPLAR:
				// skills.add(SkillTable.getInstance().getInfo(4049,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				skills.add(SkillTable.getInstance().getInfo(4053, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(4165, 9)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5115,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
			case SMART_CUBIC_SHILLIENTEMPLAR:
				skills.add(SkillTable.getInstance().getInfo(4049, 8)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(4050,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4051,7)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4052,6)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4053,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4054,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4055,8)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4164,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4165,9)); no animation
				// skills.add(SkillTable.getInstance().getInfo(4166,9)); no animation
				skills.add(SkillTable.getInstance().getInfo(5115, 4)); // have animation
				// skills.add(SkillTable.getInstance().getInfo(5116,4)); no animation
				// skills.add(SkillTable.getInstance().getInfo(5579,4)); no need to add to the
				// cubic skills list
				break;
		}
		disappearTask = ThreadPoolManager.getInstance().scheduleGeneral(new Disappear(), totallifetime); // disappear
	}
	
	/**
	 * Do action.
	 */
	public synchronized void doAction()
	{
		if (active)
		{
			return;
		}
		active = true;
		
		switch (cubicId)
		{
			case AQUA_CUBIC:
			case BINDING_CUBIC:
			case SPARK_CUBIC:
			case STORM_CUBIC:
			case POLTERGEIST_CUBIC:
			case VAMPIRIC_CUBIC:
			case VIPER_CUBIC:
			case ATTRACT_CUBIC:
			case SMART_CUBIC_ARCANALORD:
			case SMART_CUBIC_ELEMENTALMASTER:
			case SMART_CUBIC_SPECTRALMASTER:
			case SMART_CUBIC_EVATEMPLAR:
			case SMART_CUBIC_SHILLIENTEMPLAR:
				actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Action(activationChance), 0, activationTime);
				break;
			case LIFE_CUBIC:
				actionTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new Heal(), 0, activationTime);
				break;
		}
	}
	
	/**
	 * Gets the id.
	 * @return the id
	 */
	public int getId()
	{
		return cubicId;
	}
	
	/**
	 * Gets the owner.
	 * @return the owner
	 */
	public L2PcInstance getOwner()
	{
		return playerOwner;
	}
	
	/**
	 * Gets the m critical hit.
	 * @param  target the target
	 * @param  skill  the skill
	 * @return        the m critical hit
	 */
	public final int getMCriticalHit(final L2Character target, final L2Skill skill)
	{
		// TODO: Temporary now mcrit for cubics is the baseMCritRate of its owner
		return playerOwner.getTemplate().baseMCritRate;
	}
	
	/**
	 * Gets the m atk.
	 * @return the m atk
	 */
	public int getMAtk()
	{
		return matk;
	}
	
	/**
	 * Stop action.
	 */
	public void stopAction()
	{
		playerTarget = null;
		if (actionTask != null)
		{
			if (!actionTask.isCancelled())
			{
				actionTask.cancel(true);
			}
			actionTask = null;
		}
		active = false;
	}
	
	/**
	 * Cancel disappear.
	 */
	public void cancelDisappear()
	{
		if (disappearTask != null)
		{
			disappearTask.cancel(true);
			disappearTask = null;
		}
	}
	
	/**
	 * this sets the enemy target for a cubic.
	 */
	public void getCubicTarget()
	{
		try
		{
			playerTarget = null;
			final L2Object ownerTarget = playerOwner.getTarget();
			if (ownerTarget == null)
			{
				return;
			}
			
			// Duel targeting
			if (playerOwner.isInDuel())
			{
				final L2PcInstance PlayerA = DuelManager.getInstance().getDuel(playerOwner.getDuelId()).getPlayerA();
				final L2PcInstance PlayerB = DuelManager.getInstance().getDuel(playerOwner.getDuelId()).getPlayerB();
				
				if (DuelManager.getInstance().getDuel(playerOwner.getDuelId()).isPartyDuel())
				{
					final L2Party partyA = PlayerA.getParty();
					final L2Party partyB = PlayerB.getParty();
					L2Party partyEnemy = null;
					
					if (partyA != null)
					{
						if (partyA.getPartyMembers().contains(playerOwner))
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								playerTarget = PlayerB;
							}
						}
						else
						{
							partyEnemy = partyA;
						}
					}
					else
					{
						if (PlayerA == playerOwner)
						{
							if (partyB != null)
							{
								partyEnemy = partyB;
							}
							else
							{
								playerTarget = PlayerB;
							}
						}
						else
						{
							playerTarget = PlayerA;
						}
					}
					if (playerTarget == PlayerA || playerTarget == PlayerB)
					{
						if (playerTarget == ownerTarget)
						{
							return;
						}
					}
					if (partyEnemy != null)
					{
						if (partyEnemy.getPartyMembers().contains(ownerTarget))
						{
							playerTarget = (L2Character) ownerTarget;
						}
						return;
					}
				}
				if (PlayerA != playerOwner && ownerTarget == PlayerA)
				{
					playerTarget = PlayerA;
					return;
				}
				if (PlayerB != playerOwner && ownerTarget == PlayerB)
				{
					playerTarget = PlayerB;
					return;
				}
				playerTarget = null;
				return;
			}
			
			// Olympiad targeting
			if (playerOwner.isInOlympiadMode())
			{
				if (playerOwner.isInOlympiadFight())
				{
					final L2PcInstance[] players = Olympiad.getInstance().getPlayers(playerOwner.getOlympiadGameId());
					if (players != null)
					{
						if (playerOwner.getOlympiadSide() == 1)
						{
							if (ownerTarget == players[1])
							{
								playerTarget = players[1];
							}
							else if (players[1].getPet() != null && ownerTarget == players[1].getPet())
							{
								playerTarget = players[1].getPet();
							}
						}
						else
						{
							if (ownerTarget == players[0])
							{
								playerTarget = players[0];
							}
							else if (players[0].getPet() != null && ownerTarget == players[0].getPet())
							{
								playerTarget = players[0].getPet();
							}
						}
					}
				}
				return;
			}
			
			// test owners target if it is valid then use it
			if (ownerTarget instanceof L2Character && ownerTarget != playerOwner.getPet() && ownerTarget != playerOwner)
			{
				
				// target mob which has aggro on you or your summon
				if (ownerTarget instanceof L2Attackable)
				{
					
					if (((L2Attackable) ownerTarget).getAggroList().get(playerOwner) != null && !((L2Attackable) ownerTarget).isDead())
					{
						playerTarget = (L2Character) ownerTarget;
						return;
					}
					if (playerOwner.getPet() != null)
					{
						if (((L2Attackable) ownerTarget).getAggroList().get(playerOwner.getPet()) != null && !((L2Attackable) ownerTarget).isDead())
						{
							playerTarget = (L2Character) ownerTarget;
							return;
						}
					}
				}
				
				// get target in pvp or in siege
				L2PcInstance enemy = null;
				
				if ((playerOwner.getPvpFlag() > 0 && !playerOwner.isInsideZone(L2Character.ZONE_PEACE)) || playerOwner.isInsideZone(L2Character.ZONE_PVP))
				{
					if (!((L2Character) ownerTarget).isDead() && ownerTarget instanceof L2PcInstance)
					{
						enemy = (L2PcInstance) ownerTarget;
					}
					
					if (enemy != null)
					{
						boolean targetIt = true;
						
						if (playerOwner.getParty() != null)
						{
							if (playerOwner.getParty().getPartyMembers().contains(enemy))
							{
								targetIt = false;
							}
							else if (playerOwner.getParty().getCommandChannel() != null)
							{
								if (playerOwner.getParty().getCommandChannel().getMembers().contains(enemy))
								{
									targetIt = false;
								}
							}
						}
						if (playerOwner.getClan() != null && !playerOwner.isInsideZone(L2Character.ZONE_PVP))
						{
							if (playerOwner.getClan().isMember(enemy.getName()))
							{
								targetIt = false;
							}
							if (playerOwner.getAllyId() > 0 && enemy.getAllyId() > 0)
							{
								if (playerOwner.getAllyId() == enemy.getAllyId())
								{
									targetIt = false;
								}
							}
						}
						if (enemy.getPvpFlag() == 0 && !enemy.isInsideZone(L2Character.ZONE_PVP))
						{
							targetIt = false;
						}
						if (enemy.isInsideZone(L2Character.ZONE_PEACE))
						{
							targetIt = false;
						}
						if (playerOwner.getSiegeState() > 0 && playerOwner.getSiegeState() == enemy.getSiegeState())
						{
							targetIt = false;
						}
						if (!enemy.isVisible())
						{
							targetIt = false;
						}
						
						if (targetIt)
						{
							playerTarget = enemy;
							return;
						}
					}
				}
			}
		}
		catch (final Exception e)
		{
			LOGGER.error("", e);
		}
	}
	
	/**
	 * The Class Action.
	 */
	private class Action implements Runnable
	{
		private final int chance;
		
		/**
		 * Instantiates a new action.
		 * @param chance the chance
		 */
		Action(final int chance)
		{
			this.chance = chance;
			// run task
		}
		
		@Override
		public void run()
		{
			try
			{
				final L2PcInstance owner = playerOwner;
				if (owner == null)
				{
					stopAction();
					cancelDisappear();
					return;
				}
				
				if (owner.isDead() || !owner.isOnline())
				{
					stopAction();
					owner.delCubic(cubicId);
					owner.broadcastUserInfo();
					cancelDisappear();
					return;
				}
				
				if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(owner))
				{
					if (owner.getPet() != null)
					{
						if (!AttackStanceTaskManager.getInstance().getAttackStanceTask(owner.getPet()))
						{
							stopAction();
							return;
						}
					}
					else
					{
						stopAction();
						return;
					}
				}
				
				// Smart Cubic debuff cancel is 100%
				boolean UseCubicCure = false;
				L2Skill skill = null;
				
				if (cubicId >= SMART_CUBIC_EVATEMPLAR && cubicId <= SMART_CUBIC_SPECTRALMASTER)
				{
					final L2Effect[] effects = owner.getAllEffects();
					
					for (final L2Effect e : effects)
					{
						if (e != null && e.getSkill().isOffensive())
						{
							UseCubicCure = true;
							e.exit(true);
						}
					}
				}
				
				if (UseCubicCure)
				{
					// Smart Cubic debuff cancel is needed, no other skill is used in this
					// activation period
					final MagicSkillUser msu = new MagicSkillUser(owner, owner, SKILL_CUBIC_CURE, 1, 0, 0);
					owner.broadcastPacket(msu);
				}
				else if (Rnd.get(100) < chance)
				{
					skill = skills.get(Rnd.get(skills.size()));
					if (skill != null)
					{
						
						if (skill.getId() == SKILL_CUBIC_HEAL)
						{
							// friendly skill, so we look a target in owner's party
							cubicTargetForHeal();
						}
						else
						{
							// offensive skill, we look for an enemy target
							getCubicTarget();
							if (playerTarget == owner || !isInCubicRange(owner, playerTarget))
							{
								playerTarget = null;
							}
						}
						
						final L2Character target = playerTarget;
						
						if ((target != null) && (!target.isDead()))
						{
							if (Config.DEBUG)
							{
								LOGGER.info("L2CubicInstance: Action.run();");
								LOGGER.info("Cubic Id: " + cubicId + " Target: " + target.getName() + " distance: " + Math.sqrt(target.getDistanceSq(owner.getX(), owner.getY(), owner.getZ())));
							}
							
							owner.broadcastPacket(new MagicSkillUser(owner, target, skill.getId(), skill.getLevel(), 0, 0));
							
							final SkillType type = skill.getSkillType();
							final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
							final L2Character[] targets =
							{
								target
							};
							
							if ((type == SkillType.PARALYZE) || (type == SkillType.STUN) || (type == SkillType.ROOT) || (type == SkillType.AGGDAMAGE))
							{
								if (Config.DEBUG)
								{
									LOGGER.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicDisabler(type, L2CubicInstance.this, skill, targets);
							}
							else if (type == SkillType.MDAM)
							{
								if (Config.DEBUG)
								{
									LOGGER.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicMdam(L2CubicInstance.this, skill, targets);
							}
							else if ((type == SkillType.POISON) || (type == SkillType.DEBUFF) || (type == SkillType.DOT))
							{
								if (Config.DEBUG)
								{
									LOGGER.info("L2CubicInstance: Action.run() handler " + type);
								}
								useCubicContinuous(L2CubicInstance.this, skill, targets);
							}
							else if (type == SkillType.DRAIN)
							{
								if (Config.DEBUG)
								{
									LOGGER.info("L2CubicInstance: Action.run() skill " + type);
								}
								((L2SkillDrain) skill).useCubicSkill(L2CubicInstance.this, targets);
							}
							else
							{
								handler.useSkill(owner, skill, targets);
								if (Config.DEBUG)
								{
									LOGGER.info("L2CubicInstance: Action.run(); other handler");
								}
							}
						}
					}
				}
			}
			catch (final Exception e)
			{
				LOGGER.error("", e);
			}
		}
	}
	
	/**
	 * Use cubic continuous.
	 * @param activeCubic the active cubic
	 * @param skill       the skill
	 * @param targets     the targets
	 */
	public void useCubicContinuous(final L2CubicInstance activeCubic, final L2Skill skill, final L2Object[] targets)
	{
		for (final L2Character target : (L2Character[]) targets)
		{
			if (target == null || target.isDead())
			{
				continue;
			}
			
			if (skill.isOffensive())
			{
				final boolean acted = Formulas.calcCubicSkillSuccess(activeCubic, target, skill);
				if (!acted)
				{
					activeCubic.getOwner().sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					continue;
				}
				
			}
			
			// if this is a debuff let the duel manager know about it
			// so the debuff can be removed after the duel
			// (player & target must be in the same duel)
			if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && skill.getSkillType() == SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId())
			{
				final DuelManager dm = DuelManager.getInstance();
				for (final L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
				{
					if (debuff != null)
					{
						dm.onBuff(((L2PcInstance) target), debuff);
					}
				}
			}
			else
			{
				skill.getEffects(activeCubic.getOwner(), target);
			}
		}
	}
	
	/**
	 * Use cubic mdam.
	 * @param activeCubic the active cubic
	 * @param skill       the skill
	 * @param targets     the targets
	 */
	public void useCubicMdam(final L2CubicInstance activeCubic, final L2Skill skill, final L2Object[] targets)
	{
		for (final L2Character target : (L2Character[]) targets)
		{
			if (target == null)
			{
				continue;
			}
			
			if (target.isAlikeDead())
			{
				if (target instanceof L2PcInstance)
				{
					target.stopFakeDeath(null);
				}
				else
				{
					continue;
				}
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, skill));
			final int damage = (int) Formulas.calcMagicDam(activeCubic, target, skill, mcrit);
			
			if (Config.DEBUG)
			{
				LOGGER.info("L2SkillMdam: useCubicSkill() -> damage = " + damage);
			}
			
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeCubic.getOwner().sendDamageMessage(target, damage, mcrit, false, false);
				
				if (skill.hasEffects())
				{
					// activate attacked effects, if any
					target.stopSkillEffects(skill.getId());
					if (target.getFirstEffect(skill) != null)
					{
						target.removeEffect(target.getFirstEffect(skill));
					}
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						skill.getEffects(activeCubic.getOwner(), target);
					}
				}
				
				target.reduceCurrentHp(damage, activeCubic.getOwner());
			}
		}
	}
	
	/**
	 * Use cubic disabler.
	 * @param type        the type
	 * @param activeCubic the active cubic
	 * @param skill       the skill
	 * @param targets     the targets
	 */
	public void useCubicDisabler(final SkillType type, final L2CubicInstance activeCubic, final L2Skill skill, final L2Object[] targets)
	{
		if (Config.DEBUG)
		{
			LOGGER.info("Disablers: useCubicSkill()");
		}
		
		for (final L2Character target : (L2Character[]) targets)
		{
			if (target == null || target.isDead())
			{
				continue;
			}
			
			switch (type)
			{
				case STUN:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						// if this is a debuff let the duel manager know about it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && skill.getSkillType() == SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId())
						{
							final DuelManager dm = DuelManager.getInstance();
							for (final L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((L2PcInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic.getOwner(), target);
						}
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> failed");
						}
					}
					break;
				}
				case PARALYZE: // use same as root for now
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						// if this is a debuff let the duel manager know about it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && skill.getSkillType() == SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId())
						{
							final DuelManager dm = DuelManager.getInstance();
							for (final L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((L2PcInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic.getOwner(), target);
						}
						
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> failed");
						}
					}
					break;
				}
				case CANCEL:
				{
					final L2Effect[] effects = target.getAllEffects();
					
					if (effects == null || effects.length == 0)
					{
						break;
					}
					
					final int max_negated_effects = 3;
					int count = 0;
					for (final L2Effect e : effects)
					{
						if (e.getSkill().isOffensive() && count < max_negated_effects)
						{
							// Do not remove raid curse skills
							if (e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515 && e.getSkill().getId() != 4082)
							{
								e.exit(true);
								if (count > -1)
								{
									count++;
								}
							}
						}
					}
					
					break;
				}
				case ROOT:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						// if this is a debuff let the duel manager know about it
						// so the debuff can be removed after the duel
						// (player & target must be in the same duel)
						if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && skill.getSkillType() == SkillType.DEBUFF && activeCubic.getOwner().getDuelId() == ((L2PcInstance) target).getDuelId())
						{
							final DuelManager dm = DuelManager.getInstance();
							for (final L2Effect debuff : skill.getEffects(activeCubic.getOwner(), target))
							{
								if (debuff != null)
								{
									dm.onBuff(((L2PcInstance) target), debuff);
								}
							}
						}
						else
						{
							skill.getEffects(activeCubic.getOwner(), target);
						}
						
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> failed");
						}
					}
					break;
				}
				case AGGDAMAGE:
				{
					if (Formulas.calcCubicSkillSuccess(activeCubic, target, skill))
					{
						if (target instanceof L2Attackable)
						{
							target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeCubic.getOwner(), (int) ((150 * skill.getPower()) / (target.getLevel() + 7)));
						}
						skill.getEffects(activeCubic.getOwner(), target);
						
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> success");
						}
					}
					else
					{
						if (Config.DEBUG)
						{
							LOGGER.info("Disablers: useCubicSkill() -> failed");
						}
					}
					break;
				}
			}
		}
	}
	
	/**
	 * returns true if the target is inside of the owner's max Cubic range.
	 * @param  owner  the owner
	 * @param  target the target
	 * @return        true, if is in cubic range
	 */
	public boolean isInCubicRange(final L2Character owner, final L2Character target)
	{
		if (owner == null || target == null)
		{
			return false;
		}
		
		int x, y, z;
		// temporary range check until real behavior of cubics is known/coded
		final int range = MAX_MAGIC_RANGE;
		
		x = (owner.getX() - target.getX());
		y = (owner.getY() - target.getY());
		z = (owner.getZ() - target.getZ());
		
		return ((x * x) + (y * y) + (z * z) <= (range * range));
	}
	
	/**
	 * this sets the friendly target for a cubic.
	 */
	public void cubicTargetForHeal()
	{
		L2Character target = null;
		double percentleft = 100.0;
		L2Party party = playerOwner.getParty();
		
		// if owner is in a duel but not in a party duel, then it is the same as he does not have a
		// party
		if (playerOwner.isInDuel())
		{
			if (!DuelManager.getInstance().getDuel(playerOwner.getDuelId()).isPartyDuel())
			{
				party = null;
			}
		}
		
		if (party != null && !playerOwner.isInOlympiadMode())
		{
			// Get all visible objects in a spheric area near the L2Character
			// Get a list of Party Members
			final List<L2PcInstance> partyList = party.getPartyMembers();
			for (final L2Character partyMember : partyList)
			{
				if (!partyMember.isDead())
				{
					// if party member not dead, check if he is in castrange of heal cubic
					if (isInCubicRange(playerOwner, partyMember))
					{
						// member is in cubic casting range, check if he need heal and if he have
						// the lowest HP
						if (partyMember.getCurrentHp() < partyMember.getMaxHp())
						{
							if (percentleft > (partyMember.getCurrentHp() / partyMember.getMaxHp()))
							{
								percentleft = (partyMember.getCurrentHp() / partyMember.getMaxHp());
								target = partyMember;
							}
						}
					}
				}
				if (partyMember.getPet() != null)
				{
					if (partyMember.getPet().isDead())
					{
						continue;
					}
					
					// if party member's pet not dead, check if it is in castrange of heal cubic
					if (!isInCubicRange(playerOwner, partyMember.getPet()))
					{
						continue;
					}
					
					// member's pet is in cubic casting range, check if he need heal and if he have
					// the lowest HP
					if (partyMember.getPet().getCurrentHp() < partyMember.getPet().getMaxHp())
					{
						if (percentleft > (partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp()))
						{
							percentleft = (partyMember.getPet().getCurrentHp() / partyMember.getPet().getMaxHp());
							target = partyMember.getPet();
						}
					}
				}
			}
		}
		else
		{
			if (playerOwner.getCurrentHp() < playerOwner.getMaxHp())
			{
				percentleft = (playerOwner.getCurrentHp() / playerOwner.getMaxHp());
				target = playerOwner;
			}
			if (playerOwner.getPet() != null)
			{
				if (!playerOwner.getPet().isDead() && playerOwner.getPet().getCurrentHp() < playerOwner.getPet().getMaxHp() && percentleft > (playerOwner.getPet().getCurrentHp() / playerOwner.getPet().getMaxHp()) && isInCubicRange(playerOwner, playerOwner.getPet()))
				{
					target = playerOwner.getPet();
				}
			}
		}
		
		playerTarget = target;
	}
	
	/**
	 * Given by other.
	 * @return true, if successful
	 */
	public boolean givenByOther()
	{
		return givenByOther;
	}
	
	/**
	 * The Class Heal.
	 */
	private class Heal implements Runnable
	{
		
		/**
		 * Instantiates a new heal.
		 */
		Heal()
		{
			// run task
		}
		
		@Override
		public void run()
		{
			if (playerOwner.isDead() || !playerOwner.isOnline())
			{
				stopAction();
				playerOwner.delCubic(cubicId);
				playerOwner.broadcastUserInfo();
				cancelDisappear();
				return;
			}
			try
			{
				L2Skill skill = null;
				for (final L2Skill sk : skills)
				{
					if (sk.getId() == SKILL_CUBIC_HEAL)
					{
						skill = sk;
						break;
					}
				}
				
				if (skill != null)
				{
					cubicTargetForHeal();
					final L2Character target = playerTarget;
					if (target != null && !target.isDead())
					{
						if (target.getMaxHp() - target.getCurrentHp() > skill.getPower())
						{
							final L2Character[] targets =
							{
								target
							};
							final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(skill.getSkillType());
							if (handler != null)
							{
								handler.useSkill(playerOwner, skill, targets);
							}
							else
							{
								skill.useSkill(playerOwner, targets);
							}
							
							final MagicSkillUser msu = new MagicSkillUser(playerOwner, target, skill.getId(), skill.getLevel(), 0, 0);
							playerOwner.broadcastPacket(msu);
						}
					}
				}
			}
			catch (final Exception e)
			{
				LOGGER.error("", e);
			}
		}
	}
	
	private class Disappear implements Runnable
	{
		Disappear()
		{
			// run task
		}
		
		@Override
		public void run()
		{
			stopAction();
			playerOwner.delCubic(cubicId);
			playerOwner.broadcastUserInfo();
		}
	}
}
