/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.model.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.L2AttackableAI;
import l2r.gameserver.ai.L2CharacterAI;
import l2r.gameserver.ai.L2FortSiegeGuardAI;
import l2r.gameserver.ai.L2SiegeGuardAI;
import l2r.gameserver.ai.model.MageAI;
import l2r.gameserver.data.EventDroplist;
import l2r.gameserver.data.EventDroplist.DateDrop;
import l2r.gameserver.data.xml.impl.HerbDropData;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CursedWeaponsManager;
import l2r.gameserver.instancemanager.PcCafePointsManager;
import l2r.gameserver.instancemanager.WalkingManager;
import l2r.gameserver.model.AbsorberInfo;
import l2r.gameserver.model.AggroInfo;
import l2r.gameserver.model.DamageDoneInfo;
import l2r.gameserver.model.L2CommandChannel;
import l2r.gameserver.model.L2DropCategory;
import l2r.gameserver.model.L2DropData;
import l2r.gameserver.model.L2Object;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2Seed;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2RaidBossInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;
import l2r.gameserver.model.actor.instance.L2TrapInstance;
import l2r.gameserver.model.actor.knownlist.AttackableKnownList;
import l2r.gameserver.model.actor.stat.Rates;
import l2r.gameserver.model.actor.status.AttackableStatus;
import l2r.gameserver.model.actor.tasks.attackable.CommandChannelTimer;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.events.EventDispatcher;
import l2r.gameserver.model.events.impl.character.npc.attackable.OnAttackableAggroRangeEnter;
import l2r.gameserver.model.events.impl.character.npc.attackable.OnAttackableAttack;
import l2r.gameserver.model.events.impl.character.npc.attackable.OnAttackableKill;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.DecayTaskManager;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;

import gr.sr.interf.SunriseEvents;

public class L2Attackable extends L2Npc
{
	// Raid
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;
	//
	private boolean _champion = false;
	private final Map<L2Character, AggroInfo> _aggroList = new ConcurrentHashMap<>();
	private boolean _isReturningToSpawnPoint = false;
	private boolean _canReturnToSpawnPoint = true;
	private boolean _seeThroughSilentMove = false;
	// Manor
	private boolean _seeded = false;
	private L2Seed _seed = null;
	private int _seederObjId = 0;
	private ItemHolder _harvestItem;
	// Spoil
	private int _spoilerObjectId;
	private ItemHolder[] _sweepItems;
	// Over-hit
	private boolean _overhit;
	private double _overhitDamage;
	private L2Character _overhitAttacker;
	// Command channel
	private volatile L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;
	// Soul crystal
	private boolean _absorbed;
	private final Map<Integer, AbsorberInfo> _absorbersList = new ConcurrentHashMap<>();
	// Misc
	private volatile boolean _mustGiveExpSp;
	private volatile boolean _doItemDrop;
	public int RANDOM_WALK_RATE = -1;
	
	protected int _onKillDelay = 0;
	protected long _findTargetDelay = 0;
	
	/**
	 * Creates an attackable NPC.
	 * @param template the attackable NPC template
	 */
	public L2Attackable(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2Attackable);
		setIsInvul(false);
		_mustGiveExpSp = true;
		_doItemDrop = true;
	}
	
	@Override
	public AttackableKnownList getKnownList()
	{
		return (AttackableKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new AttackableKnownList(this));
	}
	
	@Override
	public AttackableStatus getStatus()
	{
		return (AttackableStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new AttackableStatus(this));
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		switch (getAiType())
		{
			case MAGE:
				return new MageAI(this);
			default:
				return new L2AttackableAI(this);
		}
	}
	
	public final Map<L2Character, AggroInfo> getAggroList()
	{
		return _aggroList;
	}
	
	public final boolean isReturningToSpawnPoint()
	{
		return _isReturningToSpawnPoint;
	}
	
	public final void setisReturningToSpawnPoint(boolean value)
	{
		_isReturningToSpawnPoint = value;
	}
	
	public final boolean canReturnToSpawnPoint()
	{
		return _canReturnToSpawnPoint;
	}
	
	public final void setCanReturnToSpawnPoint(boolean value)
	{
		_canReturnToSpawnPoint = value;
	}
	
	public boolean canSeeThroughSilentMove()
	{
		return _seeThroughSilentMove;
	}
	
	public void setSeeThroughSilentMove(boolean val)
	{
		_seeThroughSilentMove = val;
	}
	
	/**
	 * Use the skill if minimum checks are pass.
	 * @param skill the skill
	 */
	public void useMagic(L2Skill skill)
	{
		if ((skill == null) || isAlikeDead() || skill.isPassive() || isCastingNow() || isSkillDisabled(skill))
		{
			return;
		}
		
		if ((getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))) || (getCurrentHp() <= skill.getHpConsume()))
		{
			return;
		}
		
		if (!skill.isStatic())
		{
			if (skill.isMagic())
			{
				if (isMuted())
				{
					return;
				}
			}
			else
			{
				if (isPhysicalMuted())
				{
					return;
				}
			}
		}
		
		final L2Object target = skill.getFirstOfTargetList(this);
		if (target != null)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		}
	}
	
	/**
	 * Reduce the current HP of the L2Attackable.
	 * @param damage The HP decrease value
	 * @param attacker The L2Character who attacks
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(damage, attacker, true, false, skill);
	}
	
	/**
	 * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
	 * @param damage The HP decrease value
	 * @param attacker The L2Character who attacks
	 * @param awake The awake state (If True : stop sleeping)
	 * @param isDOT
	 * @param skill
	 */
	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (isRaid() && !isMinion() && (attacker != null) && (attacker.getParty() != null) && attacker.getParty().isInCommandChannel() && attacker.getParty().getCommandChannel().meetRaidWarCondition(this))
		{
			if (_firstCommandChannelAttacked == null) // looting right isn't set
			{
				synchronized (this)
				{
					if (_firstCommandChannelAttacked == null)
					{
						_firstCommandChannelAttacked = attacker.getParty().getCommandChannel();
						if (_firstCommandChannelAttacked != null)
						{
							_commandChannelTimer = new CommandChannelTimer(this);
							_commandChannelLastAttack = System.currentTimeMillis();
							ThreadPoolManager.getInstance().scheduleGeneral(_commandChannelTimer, 10000); // check for last attack
							_firstCommandChannelAttacked.broadcastPacket(new CreatureSay(0, Say2.PARTYROOM_ALL, "", "You have looting rights!")); // TODO: retail msg
						}
					}
				}
			}
			else if (attacker.getParty().getCommandChannel().equals(_firstCommandChannelAttacked)) // is in same channel
			{
				_commandChannelLastAttack = System.currentTimeMillis(); // update last attack time
			}
		}
		
		// If this L2Attackable is a L2MonsterInstance and it has spawned minions, call its minions to battle
		if (isMonster())
		{
			L2MonsterInstance master = (L2MonsterInstance) this;
			
			if (master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
			
			master = master.getLeader();
			if ((master != null) && master.hasMinions())
			{
				master.getMinionList().onAssist(this, attacker);
			}
		}
		
		addDamageHate(attacker, (int) damage, (long) (damage * 100) / (getLevel() + 7));
		
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
		
		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if (attacker != null)
		{
			addDamage(attacker, (int) damage, skill);
		}
	}
	
	public void setMustRewardExpSp(boolean value)
	{
		_mustGiveExpSp = value;
	}
	
	public boolean getMustRewardExpSP()
	{
		return _mustGiveExpSp;
	}
	
	public void enableItemDrop(boolean value)
	{
		_doItemDrop = value;
	}
	
	public boolean isItemDropEnabled()
	{
		return _doItemDrop;
	}
	
	/**
	 * Kill the L2Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.<br>
	 * Actions:<br>
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members<br>
	 * Notify the Quest Engine of the L2Attackable death if necessary.<br>
	 * Kill the L2NpcInstance (the corpse disappeared after 7 seconds)<br>
	 * Caution: This method DOESN'T GIVE rewards to L2PetInstance.
	 * @param killer The L2Character that has killed the L2Attackable
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2NpcInstance (the corpse disappeared after 7 seconds)
		if (!super.doDie(killer))
		{
			return false;
		}
		
		if ((killer != null) && killer.isPlayable())
		{
			// Delayed notification
			EventDispatcher.getInstance().notifyEventAsyncDelayed(new OnAttackableKill(killer.getActingPlayer(), this, killer.isSummon()), this, _onKillDelay);
		}
		
		if ((killer != null) && killer.isPlayer())
		{
			if (SunriseEvents.isInEvent(((L2PcInstance) killer)))
			{
				SunriseEvents.onKill(((L2PcInstance) killer), this);
			}
		}
		
		// Notify to minions if there are.
		if (isMonster())
		{
			final L2MonsterInstance mob = (L2MonsterInstance) this;
			if ((mob.getLeader() != null) && mob.getLeader().hasMinions())
			{
				final int respawnTime = Config.MINIONS_RESPAWN_TIME.containsKey(getId()) ? Config.MINIONS_RESPAWN_TIME.get(getId()) * 1000 : -1;
				mob.getLeader().getMinionList().onMinionDie(mob, respawnTime);
			}
		}
		return true;
	}
	
	/**
	 * Distribute Exp and SP rewards to L2PcInstance (including Summon owner) that hit the L2Attackable and to their Party members.<br>
	 * Actions:<br>
	 * Get the L2PcInstance owner of the L2ServitorInstance (if necessary) and L2Party in progress.<br>
	 * Calculate the Experience and SP rewards in function of the level difference.<br>
	 * Add Exp and SP rewards to L2PcInstance (including Summon penalty) and to Party members in the known area of the last attacker.<br>
	 * Caution : This method DOESN'T GIVE rewards to L2PetInstance.
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	@Override
	protected void calculateRewards(L2Character lastAttacker)
	{
		try
		{
			if (_aggroList.isEmpty())
			{
				return;
			}
			
			if (Config.DROP_LASTATTACKERISMAXDAMAGER)
			{
				int maxDamage = 0;
				L2PcInstance attacker = null;
				for (Map.Entry<L2Character, AggroInfo> entry : _aggroList.entrySet())
				{
					if ((entry.getKey() != null) && (entry.getKey().getActingPlayer() != null) && (entry.getValue().getDamage() > maxDamage))
					{
						maxDamage = entry.getValue().getDamage();
						attacker = entry.getKey().getActingPlayer();
					}
				}
				
				if (attacker != null)
				{
					lastAttacker = attacker;
				}
			}
			
			// NOTE: Concurrent-safe map is used because while iterating to verify all conditions sometimes an entry must be removed.
			final Map<L2PcInstance, DamageDoneInfo> rewards = new ConcurrentHashMap<>();
			L2PcInstance maxDealer = null;
			int maxDamage = 0;
			long totalDamage = 0;
			// While Iterating over This Map Removing Object is Not Allowed
			// Go through the _aggroList of the L2Attackable
			for (AggroInfo info : _aggroList.values())
			{
				if (info == null)
				{
					continue;
				}
				
				// Get the L2Character corresponding to this attacker
				final L2PcInstance attacker = info.getAttacker().getActingPlayer();
				if (attacker != null)
				{
					// Get damages done by this attacker
					final int damage = info.getDamage();
					
					// Prevent unwanted behavior
					if (damage > 1)
					{
						// Check if damage dealer isn't too far from this (killed monster)
						if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, attacker, true))
						{
							continue;
						}
						
						totalDamage += damage;
						
						// Calculate real damages (Summoners should get own damage plus summon's damage)
						final DamageDoneInfo reward = rewards.computeIfAbsent(attacker, DamageDoneInfo::new);
						reward.addDamage(damage);
						
						if (reward.getDamage() > maxDamage)
						{
							maxDealer = attacker;
							maxDamage = reward.getDamage();
						}
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the L2Attackable
			doItemDrop(Config.DROP_LASTATTACKERISMAXDAMAGER ? lastAttacker : (maxDealer != null) && maxDealer.isOnline() ? maxDealer : lastAttacker);
			
			// Manage drop of Special Events created by GM for a defined period
			doEventDrop(lastAttacker);
			
			if (!getMustRewardExpSP())
			{
				return;
			}
			
			if (!rewards.isEmpty())
			{
				for (DamageDoneInfo reward : rewards.values())
				{
					if (reward == null)
					{
						continue;
					}
					
					// Attacker to be rewarded
					final L2PcInstance attacker = reward.getAttacker();
					
					// Total amount of damage done
					final int damage = reward.getDamage();
					
					// Get party
					final L2Party attackerParty = attacker.getParty();
					
					// Penalty applied to the attacker's XP
					// If this attacker have servitor, get Exp Penalty applied for the servitor.
					final float penalty = attacker.hasServitor() ? ((L2ServitorInstance) attacker.getSummon()).getExpPenalty() : 0;
					
					// If there's NO party in progress
					if (attackerParty == null)
					{
						// Calculate Exp and SP rewards
						if (attacker.getKnownList().knowsObject(this))
						{
							// Calculate the difference of level between this attacker (player or servitor owner) and the L2Attackable
							// mob = 24, atk = 10, diff = -14 (full xp)
							// mob = 24, atk = 28, diff = 4 (some xp)
							// mob = 24, atk = 50, diff = 26 (no xp)
							final int levelDiff = attacker.getLevel() - getLevel();
							
							final double[] expSp = calculateExpAndSp(levelDiff, damage, totalDamage, attacker.isPremium());
							double exp = expSp[0];
							double sp = expSp[1];
							
							if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
							{
								exp *= Config.L2JMOD_CHAMPION_REWARDS_EXP;
								sp *= Config.L2JMOD_CHAMPION_REWARDS_SP;
							}
							
							exp *= 1 - penalty;
							
							// Check for an over-hit enabled strike
							L2Character overhitAttacker = getOverhitAttacker();
							if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
							{
								attacker.sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp((long) exp);
							}
							
							// Distribute the Exp and SP between the L2PcInstance and its L2Summon
							if (!attacker.isDead())
							{
								final long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
								final int addsp = (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
								
								attacker.addExpAndSp(addexp, addsp, useVitalityRate());
								if (addexp > 0)
								{
									// vGodFather for nevit system
									if (!attacker.isInsideZone(ZoneIdType.PEACE) && ((attacker.getLevel() - getLevel()) <= 9))
									{
										attacker.getNevitSystem().startAdventTask();
										attacker.getNevitSystem().checkIfMustGivePoints(addexp, this);
										
										attacker.updateVitalityPoints(getVitalityPoints(damage), true, false);
										PcCafePointsManager.givePcCafePoint(attacker, addexp);
									}
								}
							}
						}
					}
					else
					{
						// share with party members
						int partyDmg = 0;
						float partyMul = 1;
						int partyLvl = 0;
						
						// Get all L2Character that can be rewarded in the party
						final List<L2PcInstance> rewardedMembers = new ArrayList<>();
						// Go through all L2PcInstance in the party
						final List<L2PcInstance> groupMembers = attackerParty.isInCommandChannel() ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers();
						for (L2PcInstance partyPlayer : groupMembers)
						{
							if ((partyPlayer == null) || partyPlayer.isDead())
							{
								continue;
							}
							
							// Get the DamageDoneInfo of this L2PcInstance from L2Attackable rewards
							final DamageDoneInfo reward2 = rewards.get(partyPlayer);
							
							// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
							if (reward2 != null)
							{
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, partyPlayer, true))
								{
									partyDmg += reward2.getDamage(); // Add L2PcInstance damages to party damages
									rewardedMembers.add(partyPlayer);
									
									if (partyPlayer.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = partyPlayer.getLevel();
										}
									}
								}
								rewards.remove(partyPlayer); // Remove the L2PcInstance from the L2Attackable rewards
							}
							else
							{
								// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded
								// and in range of the monster.
								if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, this, partyPlayer, true))
								{
									rewardedMembers.add(partyPlayer);
									if (partyPlayer.getLevel() > partyLvl)
									{
										if (attackerParty.isInCommandChannel())
										{
											partyLvl = attackerParty.getCommandChannel().getLevel();
										}
										else
										{
											partyLvl = partyPlayer.getLevel();
										}
									}
								}
							}
						}
						
						// If the party didn't killed this L2Attackable alone
						if (partyDmg < totalDamage)
						{
							partyMul = ((float) partyDmg / totalDamage);
						}
						
						// Calculate the level difference between Party and L2Attackable
						final int levelDiff = partyLvl - getLevel();
						
						final double[] expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, false);
						final double[] premiumCheckExpSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, true);
						double exp_premium = premiumCheckExpSp[0];
						double sp_premium = premiumCheckExpSp[1];
						double exp = expSp[0];
						double sp = expSp[1];
						
						if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
						{
							exp *= Config.L2JMOD_CHAMPION_REWARDS_EXP;
							sp *= Config.L2JMOD_CHAMPION_REWARDS_SP;
							
							exp_premium *= Config.L2JMOD_CHAMPION_REWARDS_EXP;
							sp_premium *= Config.L2JMOD_CHAMPION_REWARDS_SP;
						}
						
						exp *= partyMul;
						sp *= partyMul;
						
						exp_premium *= partyMul;
						sp_premium *= partyMul;
						
						// Check for an over-hit enabled strike
						// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
						L2Character overhitAttacker = getOverhitAttacker();
						if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
						{
							attacker.sendPacket(SystemMessageId.OVER_HIT);
							exp += calculateOverhitExp((long) exp);
						}
						
						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if (partyDmg > 0)
						{
							attackerParty.distributeXpAndSp((long) exp_premium, (int) sp_premium, (long) exp, (int) sp, rewardedMembers, partyLvl, partyDmg, this);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(String.valueOf(e));
		}
	}
	
	@Override
	public void addAttackerToAttackByList(L2Character player)
	{
		if ((player == null) || (player == this) || getAttackByList().contains(player))
		{
			return;
		}
		getAttackByList().add(player);
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 * @param skill
	 */
	public void addDamage(L2Character attacker, int damage, L2Skill skill)
	{
		if (attacker == null)
		{
			return;
		}
		
		// Notify the L2Attackable AI with EVT_ATTACKED
		if (!isDead())
		{
			// If monster is on walk - stop it
			if (isWalker() && !isCoreAIDisabled() && WalkingManager.getInstance().isOnWalk(this))
			{
				WalkingManager.getInstance().stopMoving(this, false, true);
			}
			
			getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
			
			if (attacker.isPlayable())
			{
				final L2PcInstance player = attacker.getActingPlayer();
				if (player != null)
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnAttackableAttack(player, this, damage, skill, attacker.isSummon()), this);
				}
			}
		}
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 * @param aggro The hate (=damage) given by the attacker L2Character
	 */
	public void addDamageHate(L2Character attacker, int damage, long aggro)
	{
		if ((attacker == null))
		{
			return;
		}
		
		// Get the attacker, if is trap get trap owner
		L2PcInstance targetPlayer = attacker.isTrap() ? ((L2TrapInstance) attacker).getOwner() : attacker.getActingPlayer();
		// Get the AggroInfo of the attacker L2Character from the _aggroList of the L2Attackable
		AggroInfo ai = attacker.isTrap() ? _aggroList.computeIfAbsent(targetPlayer != null ? targetPlayer : attacker, AggroInfo::new) : _aggroList.computeIfAbsent(attacker, AggroInfo::new);
		
		// vGodFather: this will fix over hate damage it usually happens with gm speed in most cases
		if (damage > getCurrentHp())
		{
			damage = (int) getCurrentHp();
		}
		
		ai.addDamage(damage);
		ai.addHate(aggro);
		
		if ((targetPlayer != null) && (aggro == 0))
		{
			addDamageHate(attacker.isTrap() ? targetPlayer : attacker, 0, 1);
			
			// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
			if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
			
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnAttackableAggroRangeEnter(this, targetPlayer, attacker.isSummon()), this);
		}
		else if ((targetPlayer == null) && (aggro == 0))
		{
			aggro = 1;
			ai.addHate(1);
		}
		
		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if ((aggro > 0) && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}
	
	public void reduceHate(L2Character target, long amount)
	{
		if ((getAI() instanceof L2SiegeGuardAI) || (getAI() instanceof L2FortSiegeGuardAI))
		{
			// TODO: this just prevents error until siege guards are handled properly
			stopHating(target);
			setTarget(null);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			return;
		}
		
		if (target == null) // whole aggrolist
		{
			L2Character mostHated = getMostHated();
			
			if (mostHated == null) // makes target passive for a moment more
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				return;
			}
			
			for (AggroInfo ai : _aggroList.values())
			{
				ai.addHate(-amount);
			}
			
			amount = getHating(mostHated);
			if (amount <= 0)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
			return;
		}
		
		AggroInfo ai = _aggroList.get(target);
		if (ai == null)
		{
			_log.info("Target {} not present in aggro list of {}.", target, this);
			return;
		}
		
		ai.addHate(-amount);
		if (ai.getHate() <= 0)
		{
			if (getMostHated() == null)
			{
				((L2AttackableAI) getAI()).setGlobalAggro(-25);
				clearAggroList();
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				setWalking();
			}
		}
	}
	
	/**
	 * Clears _aggroList hate of the L2Character without removing from the list.
	 * @param target
	 */
	public void stopHating(L2Character target)
	{
		if (target == null)
		{
			return;
		}
		AggroInfo ai = _aggroList.get(target);
		if (ai != null)
		{
			ai.stopHate();
		}
	}
	
	/**
	 * @return the most hated L2Character of the L2Attackable _aggroList.
	 */
	public L2Character getMostHated()
	{
		if (_aggroList.isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		L2Character mostHated = null;
		long maxHate = 0;
		
		// While Interacting over This Map Removing Object is Not Allowed
		// Go through the aggroList of the L2Attackable
		for (AggroInfo ai : _aggroList.values())
		{
			if (ai == null)
			{
				continue;
			}
			
			if (ai.checkHate(this) > maxHate)
			{
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}
		
		return mostHated;
	}
	
	/**
	 * @return the 2 most hated L2Character of the L2Attackable _aggroList.
	 */
	public List<L2Character> get2MostHated()
	{
		if (_aggroList.isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		L2Character mostHated = null;
		L2Character secondMostHated = null;
		long maxHate = 0;
		List<L2Character> result = new ArrayList<>();
		
		// While iterating over this map removing objects is not allowed
		// Go through the aggroList of the L2Attackable
		for (AggroInfo ai : _aggroList.values())
		{
			if (ai == null)
			{
				continue;
			}
			
			if (ai.checkHate(this) > maxHate)
			{
				secondMostHated = mostHated;
				mostHated = ai.getAttacker();
				maxHate = ai.getHate();
			}
		}
		
		result.add(mostHated);
		
		if (getAttackByList().contains(secondMostHated))
		{
			result.add(secondMostHated);
		}
		else
		{
			result.add(null);
		}
		return result;
	}
	
	public List<L2Character> getHateList()
	{
		if (_aggroList.isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		List<L2Character> result = new ArrayList<>();
		for (AggroInfo ai : _aggroList.values())
		{
			if (ai == null)
			{
				continue;
			}
			ai.checkHate(this);
			
			result.add(ai.getAttacker());
		}
		return result;
	}
	
	/**
	 * @param target The L2Character whose hate level must be returned
	 * @return the hate level of the L2Attackable against this L2Character contained in _aggroList.
	 */
	public long getHating(final L2Character target)
	{
		if (_aggroList.isEmpty() || (target == null))
		{
			return 0;
		}
		
		final AggroInfo ai = _aggroList.get(target);
		if (ai == null)
		{
			return 0;
		}
		
		if (ai.getAttacker() instanceof L2PcInstance)
		{
			L2PcInstance act = (L2PcInstance) ai.getAttacker();
			if (act.isInvisible() || ai.getAttacker().isInvul() || act.isSpawnProtected())
			{
				// Remove Object Should Use This Method and Can be Blocked While Interacting
				_aggroList.remove(target);
				return 0;
			}
		}
		
		if (!ai.getAttacker().isVisible() || ai.getAttacker().isInvisible())
		{
			_aggroList.remove(target);
			return 0;
		}
		
		if (ai.getAttacker().isAlikeDead())
		{
			ai.stopHate();
			return 0;
		}
		return ai.getHate();
	}
	
	/**
	 * Calculates quantity of items for specific drop according to current situation
	 * @param drop The L2DropData count is being calculated for
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @param isSweep
	 * @return
	 */
	private ItemHolder calculateRewardItem(L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		double dropChance = drop.getChance();
		
		int deepBlueDrop = 1;
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			if (levelModifier > 0)
			{
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if (drop.getId() == Inventory.ADENA_ID)
				{
					deepBlueDrop *= (int) lastAttacker.getRate(Rates.DROP_ITEM, Inventory.ADENA_ID, (isRaid() && !isRaidMinion()));
				}
			}
		}
		
		// Avoid dividing by 0
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Applies Drop rates
		dropChance *= lastAttacker.getRate(isSweep ? Rates.SPOIL : Rates.DROP_ITEM, drop.getId(), (isRaid() && !isRaidMinion()));
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
		{
			dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}
		
		// Set our limits for chance of drop
		if (dropChance < 1)
		{
			dropChance = 1;
		}
		
		// Get min and max Item quantity that can be dropped in one time
		int minCount = drop.getMinDrop();
		int maxCount = drop.getMaxDrop();
		int itemCount = 0;
		
		// Count and chance adjustment for high rate servers
		if ((dropChance > L2DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
		{
			int multiplier = (int) dropChance / L2DropData.MAX_CHANCE;
			
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount * multiplier, maxCount * multiplier);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount * multiplier;
			}
			else
			{
				itemCount += multiplier;
			}
			
			dropChance = dropChance % L2DropData.MAX_CHANCE;
		}
		// Check if the Item must be dropped
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if (minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && ((drop.getId() == Inventory.ADENA_ID) || Util.contains(SevenSigns.SEAL_STONE_IDS, drop.getId())))
		{
			itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
		}
		
		if (itemCount > 0)
		{
			return new ItemHolder(drop.getId(), itemCount);
		}
		return null;
	}
	
	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation.<br>
	 * Only a max of ONE item from a category is allowed to be dropped.
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @param categoryDrops
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @return
	 */
	private ItemHolder calculateCategorizedRewardItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
		{
			return null;
		}
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		
		int deepBlueDrop = 1;
		
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
			// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
			if (levelModifier > 0)
			{
				deepBlueDrop = 3;
			}
		}
		
		// Avoid dividing by 0
		if (deepBlueDrop == 0)
		{
			deepBlueDrop = 1;
		}
		
		// Check if we should apply our maths so deep blue mobs will not drop that easy
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			categoryDropChance = ((categoryDropChance - ((categoryDropChance * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Applies Drop rates
		categoryDropChance *= lastAttacker.getRate(Rates.DROP_ITEM, -1, (isRaid() && !isRaidMinion()));
		
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
		{
			categoryDropChance *= Config.L2JMOD_CHAMPION_REWARDS;
		}
		
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(isRaid() && !isRaidMinion());
			
			if (drop == null)
			{
				return null;
			}
			
			// Now decide the quantity to drop based on the rates and penalties. To get this value
			// simply divide the modified categoryDropChance by the base category chance. This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again. If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure. So the chance will be adjusted to 100%
			// if smaller.
			double dropChance = drop.getChance() * lastAttacker.getRate(Rates.DROP_ITEM, drop.getId(), (isRaid() && !isRaidMinion()));
			
			if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
			{
				dropChance *= Config.L2JMOD_CHAMPION_REWARDS;
			}
			
			dropChance = Math.round(dropChance);
			
			if (dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			// Get min and max Item quantity that can be dropped in one time
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Count and chance adjustment for high rate servers
			if ((dropChance > L2DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);
				
				if (min < max)
				{
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				}
				else if (min == max)
				{
					itemCount += min * multiplier;
				}
				else
				{
					itemCount += multiplier;
				}
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			
			// Check if the Item must be dropped
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if (min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && ((drop.getId() == Inventory.ADENA_ID) || Util.contains(SevenSigns.SEAL_STONE_IDS, drop.getId())))
			{
				itemCount *= Config.L2JMOD_CHAMPION_ADENAS_REWARDS;
			}
			
			if (!Config.MULTIPLE_ITEM_DROP && !ItemData.getInstance().getTemplate(drop.getId()).isStackable() && (itemCount > 1))
			{
				itemCount = 1;
			}
			
			if (itemCount > 0)
			{
				return new ItemHolder(drop.getId(), itemCount);
			}
		}
		return null;
	}
	
	/**
	 * Calculates the level modifier for drop.
	 * @param lastAttacker The L2PcInstance that has killed the L2Attackable
	 * @return
	 */
	private int calculateLevelModifierForDrop(L2PcInstance lastAttacker)
	{
		if ((!isRaid() && Config.DEEPBLUE_DROP_RULES) || (isRaid() && Config.DEEPBLUE_DROP_RULES_RAID))
		{
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			if (!getAttackByList().isEmpty())
			{
				for (L2Character atkChar : getAttackByList())
				{
					if ((atkChar != null) && (atkChar.getLevel() > highestLevel))
					{
						highestLevel = atkChar.getLevel();
					}
				}
			}
			
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if ((highestLevel - 9) >= getLevel())
			{
				return ((highestLevel - (getLevel() + 8)) * 9);
			}
		}
		return 0;
	}
	
	private ItemHolder calculateCategorizedHerbItem(L2PcInstance lastAttacker, L2DropCategory categoryDrops)
	{
		if (categoryDrops == null)
		{
			return null;
		}
		
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		
		// Applies Drop rates
		switch (categoryDrops.getCategoryType())
		{
			case 0:
				if (Config.ENABLE_DROP_VITALITY_HERBS)
				{
					categoryDropChance *= Config.RATE_DROP_VITALITY_HERBS;
				}
				else
				{
					return null;
				}
				break;
			case 1:
				categoryDropChance *= Config.RATE_DROP_HP_HERBS;
				break;
			case 2:
				categoryDropChance *= Config.RATE_DROP_MP_HERBS;
				break;
			case 3:
				categoryDropChance *= Config.RATE_DROP_SPECIAL_HERBS;
				break;
			default:
				categoryDropChance *= Config.RATE_DROP_COMMON_HERBS;
		}
		
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(false);
			
			if (drop == null)
			{
				return null;
			}
			
			// Now decide the quantity to drop based on the rates and penalties. To get this value
			// simply divide the modified categoryDropChance by the base category chance. This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again. If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure. So the chance will be adjusted to 100%
			// if smaller.
			
			double dropChance = drop.getChance();
			
			switch (categoryDrops.getCategoryType())
			{
				case 0:
					dropChance *= Config.RATE_DROP_VITALITY_HERBS;
					break;
				case 1:
					dropChance *= Config.RATE_DROP_HP_HERBS;
					break;
				case 2:
					dropChance *= Config.RATE_DROP_MP_HERBS;
					break;
				case 3:
					dropChance *= Config.RATE_DROP_SPECIAL_HERBS;
					break;
				default:
					dropChance *= Config.RATE_DROP_COMMON_HERBS;
			}
			
			if (dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			// Get min and max Item quantity that can be dropped in one time
			int min = drop.getMinDrop();
			int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Count and chance adjustment for high rate servers
			if ((dropChance > L2DropData.MAX_CHANCE) && !Config.PRECISE_DROP_CALCULATION)
			{
				long multiplier = Math.round(dropChance / L2DropData.MAX_CHANCE);
				
				if (min < max)
				{
					itemCount += Rnd.get(min * multiplier, max * multiplier);
				}
				else if (min == max)
				{
					itemCount += min * multiplier;
				}
				else
				{
					itemCount += multiplier;
				}
				
				dropChance = dropChance % L2DropData.MAX_CHANCE;
			}
			
			// Check if the Item must be dropped
			int random = Rnd.get(L2DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
				{
					itemCount += Rnd.get(min, max);
				}
				else if (min == max)
				{
					itemCount += min;
				}
				else
				{
					itemCount++;
				}
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= L2DropData.MAX_CHANCE;
			}
			
			if (itemCount > 0)
			{
				return new ItemHolder(drop.getId(), itemCount);
			}
		}
		return null;
	}
	
	public void doItemDrop(L2Character mainDamageDealer)
	{
		doItemDrop(getTemplate(), mainDamageDealer);
	}
	
	/**
	 * Manage Base, Quests and Special Events drops of L2Attackable (called by calculateRewards).<br>
	 * Concept:<br>
	 * During a Special Event all L2Attackable can drop extra Items.<br>
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.<br>
	 * Each Special Event has a start and end date to stop to drop extra Items automatically.<br>
	 * Actions:<br>
	 * Manage drop of Special Events created by GM for a defined period.<br>
	 * Get all possible drops of this L2Attackable from L2NpcTemplate and add it Quest drops.<br>
	 * For each possible drops (base + quests), calculate which one must be dropped (random).<br>
	 * Get each Item quantity dropped (random).<br>
	 * Create this or these L2ItemInstance corresponding to each Item Identifier dropped.<br>
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable.<br>
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last.
	 * @param npcTemplate
	 * @param mainDamageDealer
	 */
	public void doItemDrop(L2NpcTemplate npcTemplate, L2Character mainDamageDealer)
	{
		if ((mainDamageDealer == null) || !isItemDropEnabled())
		{
			return;
		}
		
		L2PcInstance player = mainDamageDealer.getActingPlayer();
		
		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if (player == null)
		{
			return;
		}
		
		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(player);
		
		CursedWeaponsManager.getInstance().checkDrop(this, player);
		
		// now throw all categorized drops and handle spoil.
		for (L2DropCategory cat : npcTemplate.getDropData())
		{
			if (isRaid() && (this instanceof L2RaidBossInstance) && ((L2RaidBossInstance) this).isEventRaid())
			{
				break;
			}
			
			ItemHolder item = null;
			if (cat.isSweep())
			{
				if (isSpoiled())
				{
					List<ItemHolder> sweepList = new ArrayList<>();
					for (L2DropData drop : cat.getAllDrops())
					{
						item = calculateRewardItem(player, drop, levelModifier, true);
						if (item == null)
						{
							continue;
						}
						sweepList.add(item);
					}
					// Set the table _sweepItems of this L2Attackable
					if (!sweepList.isEmpty())
					{
						_sweepItems = sweepList.toArray(new ItemHolder[sweepList.size()]);
					}
				}
			}
			else
			{
				if (isSeeded())
				{
					L2DropData drop = cat.dropSeedAllowedDropsOnly();
					
					if (drop == null)
					{
						continue;
					}
					
					item = calculateRewardItem(player, drop, levelModifier, false);
				}
				else
				{
					item = calculateCategorizedRewardItem(player, cat, levelModifier);
				}
				
				if (item != null)
				{
					// vGodFather we use this for premium engine
					item.setFromMob(true);
					
					// Check if the autoLoot mode is active
					L2Item itemCheck = ItemData.getInstance().getTemplate(item.getId()); // used to check herbs
					if (isFlying() || (!itemCheck.hasExImmediateEffect() && ((!isRaid() && Config.AUTO_LOOT) || (isRaid() && Config.AUTO_LOOT_RAIDS))) || (itemCheck.hasExImmediateEffect() && Config.AUTO_LOOT_HERBS))
					{
						// vGodFather fix
						if ((itemCheck.isStackable() && (player.getInventory().getItemByItemId(itemCheck.getId()) != null)) || (player.getInventory().getSize(false) < player.getInventoryLimit()))
						{
							player.doAutoLoot(this, item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
						}
						else
						{
							dropItem(player, item); // drop the item on the ground
						}
					}
					else
					{
						dropItem(player, item); // drop the item on the ground
					}
					
					// Broadcast message if RaidBoss was defeated
					if (isRaid() && !isRaidMinion())
					{
						final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DIED_DROPPED_S3_S2);
						sm.addCharName(this);
						sm.addItemName(item.getId());
						sm.addLong(item.getCount());
						broadcastPacket(sm);
					}
				}
			}
		}
		// Apply Special Item drop with random(rnd) quantity(qty) for champions.
		if (Config.L2JMOD_CHAMPION_ENABLE && isChampion() && ((Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE > 0) || (Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE > 0)))
		{
			int champqty = Rnd.get(Config.L2JMOD_CHAMPION_REWARD_QTY);
			ItemHolder item = new ItemHolder(Config.L2JMOD_CHAMPION_REWARD_ID, ++champqty, true);
			L2Item itemCheck = ItemData.getInstance().getTemplate(item.getId());
			
			if ((player.getLevel() <= getLevel()) && (Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE))
			{
				if (Config.AUTO_LOOT || isFlying())
				{
					// vGodFather fix
					if ((itemCheck.isStackable() && (player.getInventory().getItemByItemId(itemCheck.getId()) != null)) || (player.getInventory().getSize(false) < player.getInventoryLimit()))
					{
						player.addItem("ChampionLoot", item.getId(), item.getCount(), this, true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
					}
					else
					{
						dropItem(player, item); // drop the item on the ground
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
			else if ((player.getLevel() > getLevel()) && (Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_HIGHER_LVL_ITEM_CHANCE))
			{
				if (Config.AUTO_LOOT || isFlying())
				{
					// vGodFather fix
					if ((itemCheck.isStackable() && (player.getInventory().getItemByItemId(itemCheck.getId()) != null)) || (player.getInventory().getSize(false) < player.getInventoryLimit()))
					{
						player.addItem("ChampionLoot", item.getId(), item.getCount(), this, true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
					}
					else
					{
						dropItem(player, item); // drop the item on the ground
					}
				}
				else
				{
					dropItem(player, item);
				}
			}
		}
		
		// Instant Item Drop :>
		if (getTemplate().getDropHerbGroup() > 0)
		{
			for (L2DropCategory cat : HerbDropData.getInstance().getHerbDroplist(getTemplate().getDropHerbGroup()))
			{
				ItemHolder item = calculateCategorizedHerbItem(player, cat);
				if (item != null)
				{
					// more than one herb can't be auto looted!
					long count = item.getCount();
					if (count > 1)
					{
						final ItemHolder herb = new ItemHolder(item.getId(), 1);
						for (int i = 0; i < count; i++)
						{
							dropItem(player, herb);
						}
					}
					else if (isFlying() || Config.AUTO_LOOT_HERBS)
					{
						player.addItem("Loot", item.getId(), count, this, true);
					}
					else
					{
						dropItem(player, item);
					}
				}
			}
		}
	}
	
	/**
	 * Manage Special Events drops created by GM for a defined period.<br>
	 * Concept:<br>
	 * During a Special Event all L2Attackable can drop extra Items.<br>
	 * Those extra Items are defined in the table allNpcDateDrops of the EventDroplist.<br>
	 * Each Special Event has a start and end date to stop to drop extra Items automatically.<br>
	 * Actions: <I>If an extra drop must be generated</I><br>
	 * Get an Item Identifier (random) from the DateDrop Item table of this Event.<br>
	 * Get the Item quantity dropped (random).<br>
	 * Create this or these L2ItemInstance corresponding to this Item Identifier.<br>
	 * If the autoLoot mode is actif and if the L2Character that has killed the L2Attackable is a L2PcInstance, Give the item(s) to the L2PcInstance that has killed the L2Attackable<br>
	 * If the autoLoot mode isn't actif or if the L2Character that has killed the L2Attackable is not a L2PcInstance, add this or these item(s) in the world as a visible object at the position where mob was last
	 * @param lastAttacker The L2Character that has killed the L2Attackable
	 */
	public void doEventDrop(L2Character lastAttacker)
	{
		if (lastAttacker == null)
		{
			return;
		}
		
		L2PcInstance player = lastAttacker.getActingPlayer();
		
		// Don't drop anything if the last attacker or owner isn't L2PcInstance
		if (player == null)
		{
			return;
		}
		
		if ((player.getLevel() - getLevel()) > 9)
		{
			return;
		}
		
		// Go through DateDrop of EventDroplist allNpcDateDrops within the date range
		for (DateDrop drop : EventDroplist.getInstance().getAllDrops())
		{
			if (Rnd.get(L2DropData.MAX_CHANCE) < drop.getEventDrop().getDropChance())
			{
				final int itemId = drop.getEventDrop().getItemIdList()[Rnd.get(drop.getEventDrop().getItemIdList().length)];
				final long itemCount = Rnd.get(drop.getEventDrop().getMinCount(), drop.getEventDrop().getMaxCount());
				
				if (Config.AUTO_LOOT || isFlying())
				{
					player.doAutoLoot(this, itemId, itemCount); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
				}
				else
				{
					dropItem(player, itemId, itemCount); // drop the item on the ground
				}
			}
		}
	}
	
	/**
	 * @return the active weapon of this L2Attackable (= null).
	 */
	public L2ItemInstance getActiveWeapon()
	{
		return null;
	}
	
	/**
	 * @return True if the _aggroList of this L2Attackable is Empty.
	 */
	public boolean noTarget()
	{
		return _aggroList.isEmpty();
	}
	
	/**
	 * Verifies if the creature is in the aggro list.
	 * @param creature the creature
	 * @return {@code true} if the creature is in the aggro list, {@code false} otherwise
	 */
	public boolean isInAggroList(L2Character creature)
	{
		return _aggroList.containsKey(creature);
	}
	
	/**
	 * Clear the _aggroList of the L2Attackable.
	 */
	public void clearAggroList()
	{
		_aggroList.clear();
		
		// clear overhit values
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}
	
	/**
	 * @return True if a Dwarf use Sweep on the L2Attackable and if item can be spoiled.
	 */
	@Override
	public boolean isSweepActive()
	{
		return _sweepItems != null;
	}
	
	/**
	 * @return a copy of dummy items for the spoil loot.
	 */
	public List<L2Item> getSpoilLootItems()
	{
		final List<L2Item> lootItems = new ArrayList<>();
		if (isSweepActive())
		{
			for (ItemHolder item : _sweepItems)
			{
				lootItems.add(ItemData.getInstance().getTemplate(item.getId()));
			}
		}
		return lootItems;
	}
	
	/**
	 * @return table containing all L2ItemInstance that can be spoiled.
	 */
	public synchronized ItemHolder[] takeSweep()
	{
		ItemHolder[] sweep = _sweepItems;
		_sweepItems = null;
		return sweep;
	}
	
	/**
	 * @return table containing all L2ItemInstance that can be harvested.
	 */
	public synchronized ItemHolder takeHarvest()
	{
		ItemHolder harvest = _harvestItem;
		_harvestItem = null;
		return harvest;
	}
	
	/**
	 * Checks if the corpse is too old.
	 * @param attacker the player to validate
	 * @param remainingTime the time to check
	 * @param sendMessage if {@code true} will send a message of corpse too old
	 * @return {@code true} if the corpse is too old
	 */
	public boolean isOldCorpse(L2PcInstance attacker, int remainingTime, boolean sendMessage)
	{
		if (isDead() && (DecayTaskManager.getInstance().getRemainingTime(this) < remainingTime))
		{
			if (sendMessage && (attacker != null))
			{
				attacker.sendPacket(SystemMessageId.CORPSE_TOO_OLD_SKILL_NOT_USED);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @param sweeper the player to validate.
	 * @param sendMessage sendMessage if {@code true} will send a message of sweep not allowed.
	 * @return {@code true} if is the spoiler or is in the spoiler party.
	 */
	public boolean checkSpoilOwner(L2PcInstance sweeper, boolean sendMessage)
	{
		if ((sweeper.getObjectId() != getSpoilerObjectId()) && !sweeper.isInLooterParty(getSpoilerObjectId()))
		{
			if (sendMessage)
			{
				sweeper.sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED);
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Set the over-hit flag on the L2Attackable.
	 * @param status The status of the over-hit flag
	 */
	public void overhitEnabled(boolean status)
	{
		_overhit = status;
	}
	
	/**
	 * Set the over-hit values like the attacker who did the strike and the amount of damage done by the skill.
	 * @param attacker The L2Character who hit on the L2Attackable using the over-hit enabled skill
	 * @param damage The amount of damage done by the over-hit enabled skill on the L2Attackable
	 */
	public void setOverhitValues(L2Character attacker, double damage)
	{
		// Calculate the over-hit damage
		// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
		double overhitDmg = -(getCurrentHp() - damage);
		if (overhitDmg < 0)
		{
			// we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
			// let's just clear all the over-hit related values
			overhitEnabled(false);
			_overhitDamage = 0;
			_overhitAttacker = null;
			return;
		}
		overhitEnabled(true);
		_overhitDamage = overhitDmg;
		_overhitAttacker = attacker;
	}
	
	/**
	 * Return the L2Character who hit on the L2Attackable using an over-hit enabled skill.
	 * @return L2Character attacker
	 */
	public L2Character getOverhitAttacker()
	{
		return _overhitAttacker;
	}
	
	/**
	 * Return the amount of damage done on the L2Attackable using an over-hit enabled skill.
	 * @return double damage
	 */
	public double getOverhitDamage()
	{
		return _overhitDamage;
	}
	
	/**
	 * @return True if the L2Attackable was hit by an over-hit enabled skill.
	 */
	public boolean isOverhit()
	{
		return _overhit;
	}
	
	/**
	 * Activate the absorbed soul condition on the L2Attackable.
	 */
	public void absorbSoul()
	{
		_absorbed = true;
	}
	
	/**
	 * @return True if the L2Attackable had his soul absorbed.
	 */
	public boolean isAbsorbed()
	{
		return _absorbed;
	}
	
	/**
	 * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.
	 * @param attacker
	 */
	public void addAbsorber(L2PcInstance attacker)
	{
		// If we have no _absorbersList initiated, do it
		final AbsorberInfo ai = _absorbersList.get(attacker.getObjectId());
		
		// If the L2Character attacker isn't already in the _absorbersList of this L2Attackable, add it
		if (ai == null)
		{
			_absorbersList.put(attacker.getObjectId(), new AbsorberInfo(attacker.getObjectId(), getCurrentHp()));
		}
		else
		{
			ai.setAbsorbedHp(getCurrentHp());
		}
		
		// Set this L2Attackable as absorbed
		absorbSoul();
	}
	
	public void resetAbsorbList()
	{
		_absorbed = false;
		_absorbersList.clear();
	}
	
	public Map<Integer, AbsorberInfo> getAbsorbersList()
	{
		return _absorbersList;
	}
	
	/**
	 * Calculate the Experience and SP to distribute to attacker (L2PcInstance, L2ServitorInstance or L2Party) of the L2Attackable.
	 * @param diff The difference of level between attacker (L2PcInstance, L2ServitorInstance or L2Party) and the L2Attackable
	 * @param damage The damages given by the attacker (L2PcInstance, L2ServitorInstance or L2Party)
	 * @param totalDamage
	 * @param isPremium
	 * @return
	 */
	private double[] calculateExpAndSp(int diff, int damage, long totalDamage, boolean isPremium)
	{
		double xp;
		double sp;
		
		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		
		xp = ((double) getExpReward(isPremium) * damage) / totalDamage;
		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		sp = ((double) getSpReward(isPremium) * damage) / totalDamage;
		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if ((Config.ALT_GAME_EXPONENT_XP == 0) && (Config.ALT_GAME_EXPONENT_SP == 0))
		{
			if (diff > 5) // formula revised May 07
			{
				double pow = Math.pow((double) 5 / 6, diff - 5);
				xp = xp * pow;
				sp = sp * pow;
			}
		}
		
		xp = Math.max(0., xp);
		sp = Math.max(0., sp);
		
		return new double[]
		{
			xp,
			sp
		};
	}
	
	public long calculateOverhitExp(long normalExp)
	{
		// Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the L2Attackable
		double overhitPercentage = ((getOverhitDamage() * 100) / getMaxHp());
		
		// Over-hit damage percentages are limited to 25% max
		if (overhitPercentage > 25)
		{
			overhitPercentage = 25;
		}
		
		// Get the overhit exp bonus according to the above over-hit damage percentage
		// (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
		double overhitExp = ((overhitPercentage / 100) * normalExp);
		
		// Return the rounded ammount of exp points to be added to the player's normal exp reward
		long bonusOverhit = Math.round(overhitExp);
		return bonusOverhit;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean canBeAttacked()
	{
		return true;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		// Clear mob spoil, seed
		setSpoilerObjectId(0);
		// Clear all aggro char from list
		clearAggroList();
		// Clear Harvester reward
		_harvestItem = null;
		// Clear mod Seeded stat
		_seeded = false;
		_seed = null;
		_seederObjId = 0;
		// Clear overhit value
		overhitEnabled(false);
		
		_sweepItems = null;
		resetAbsorbList();
		
		setWalking();
		
		// check the region where this mob is, do not activate the AI if region is inactive.
		if (!isInActiveRegion())
		{
			if (hasAI())
			{
				getAI().stopAITask();
			}
		}
	}
	
	/**
	 * Checks if its spoiled.
	 * @return {@code true} if its spoiled, {@code false} otherwise
	 */
	public final boolean isSpoiled()
	{
		return _spoilerObjectId != 0;
	}
	
	/**
	 * Gets the spoiler object id.
	 * @return the spoiler object id if its spoiled, 0 otherwise
	 */
	public final int getSpoilerObjectId()
	{
		return _spoilerObjectId;
	}
	
	/**
	 * Sets the spoiler object id.
	 * @param spoilerObjectId the spoiler object id
	 */
	public final void setSpoilerObjectId(int spoilerObjectId)
	{
		_spoilerObjectId = spoilerObjectId;
	}
	
	/**
	 * Sets state of the mob to seeded. Parameters needed to be set before.
	 * @param seeder
	 */
	public final void setSeeded(L2PcInstance seeder)
	{
		if ((_seed != null) && (_seederObjId == seeder.getObjectId()))
		{
			_seeded = true;
			
			int count = 1;
			for (int skillId : getTemplate().getSkills().keySet())
			{
				switch (skillId)
				{
					case 4303: // Strong type x2
						count *= 2;
						break;
					case 4304: // Strong type x3
						count *= 3;
						break;
					case 4305: // Strong type x4
						count *= 4;
						break;
					case 4306: // Strong type x5
						count *= 5;
						break;
					case 4307: // Strong type x6
						count *= 6;
						break;
					case 4308: // Strong type x7
						count *= 7;
						break;
					case 4309: // Strong type x8
						count *= 8;
						break;
					case 4310: // Strong type x9
						count *= 9;
						break;
				}
			}
			// hi-lvl mobs bonus
			final int diff = getLevel() - _seed.getLevel() - 5;
			if (diff > 0)
			{
				count += diff;
			}
			_harvestItem = new ItemHolder(_seed.getCropId(), count * Config.RATE_DROP_MANOR);
		}
	}
	
	/**
	 * Sets the seed parameters, but not the seed state
	 * @param seed - instance {@link L2Seed} of used seed
	 * @param seeder - player who sows the seed
	 */
	public final void setSeeded(L2Seed seed, L2PcInstance seeder)
	{
		if (!_seeded)
		{
			_seed = seed;
			_seederObjId = seeder.getObjectId();
		}
	}
	
	public final int getSeederId()
	{
		return _seederObjId;
	}
	
	public final L2Seed getSeed()
	{
		return _seed;
	}
	
	public final boolean isSeeded()
	{
		return _seeded;
	}
	
	/**
	 * Set delay for onKill() call, in ms Default: 0 ms
	 * @param delay
	 */
	public final void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
	}
	
	public final int getOnKillDelay()
	{
		return _onKillDelay;
	}
	
	public final void setFindTargetDelay(int delay)
	{
		_findTargetDelay = (System.currentTimeMillis() + delay);
	}
	
	public final long getFindTargetDelay()
	{
		return _findTargetDelay;
	}
	
	/**
	 * Check if the server allows Random Animation.
	 */
	// This is located here because L2Monster and L2FriendlyMob both extend this class. The other non-pc instances extend either L2NpcInstance or L2MonsterInstance.
	@Override
	public boolean hasRandomAnimation()
	{
		return ((Config.MAX_MONSTER_ANIMATION > 0) && isRandomAnimationEnabled() && !(this instanceof L2GrandBossInstance));
	}
	
	@Override
	public boolean isMob()
	{
		return true; // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
	}
	
	public void setCommandChannelTimer(CommandChannelTimer commandChannelTimer)
	{
		_commandChannelTimer = commandChannelTimer;
	}
	
	public CommandChannelTimer getCommandChannelTimer()
	{
		return _commandChannelTimer;
	}
	
	public L2CommandChannel getFirstCommandChannelAttacked()
	{
		return _firstCommandChannelAttacked;
	}
	
	public void setFirstCommandChannelAttacked(L2CommandChannel firstCommandChannelAttacked)
	{
		_firstCommandChannelAttacked = firstCommandChannelAttacked;
	}
	
	/**
	 * @return the _commandChannelLastAttack
	 */
	public long getCommandChannelLastAttack()
	{
		return _commandChannelLastAttack;
	}
	
	/**
	 * @param channelLastAttack the _commandChannelLastAttack to set
	 */
	public void setCommandChannelLastAttack(long channelLastAttack)
	{
		_commandChannelLastAttack = channelLastAttack;
	}
	
	public void returnHome()
	{
		returnHome(false);
	}
	
	public void returnHome(boolean teleport)
	{
		if (isDead() || isDecayed())
		{
			return;
		}
		
		clearAggroList();
		setisReturningToSpawnPoint(false);
		
		if (hasAI() && (getSpawn() != null))
		{
			if (!teleport)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
			}
			else
			{
				stopMove(null);
				getAttackByList().clear();
				
				setTarget(null);
				
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				
				broadcastPacket(new MagicSkillUse(this, this, 2036, 1, 500, 0), 2500);
				teleToLocation(new Location(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
			}
		}
	}
	
	/*
	 * Return vitality points decrease (if positive) or increase (if negative) based on damage. Maximum for damage = maxHp.
	 */
	public float getVitalityPoints(int damage)
	{
		// sanity check
		if (damage <= 0)
		{
			return 0;
		}
		
		final float divider = getTemplate().getBaseVitalityDivider();
		if (divider == 0)
		{
			return 0;
		}
		
		// negative value - vitality will be consumed
		return -Math.min(damage, getMaxHp()) / divider;
	}
	
	/*
	 * True if vitality rate for exp and sp should be applied
	 */
	public boolean useVitalityRate()
	{
		if (isChampion() && !Config.L2JMOD_CHAMPION_ENABLE_VITALITY)
		{
			return false;
		}
		
		return true;
	}
	
	/** Return True if the L2Character is RaidBoss or his minion. */
	@Override
	public boolean isRaid()
	{
		return _isRaid;
	}
	
	/**
	 * Set this Npc as a Raid instance.
	 * @param isRaid
	 */
	public void setIsRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	/**
	 * Set this Npc as a Minion instance.
	 * @param val
	 */
	public void setIsRaidMinion(boolean val)
	{
		_isRaid = val;
		_isRaidMinion = val;
	}
	
	@Override
	public boolean isRaidMinion()
	{
		return _isRaidMinion;
	}
	
	@Override
	public boolean isMinion()
	{
		return getLeader() != null;
	}
	
	/**
	 * @return leader of this minion or null.
	 */
	public L2Attackable getLeader()
	{
		return null;
	}
	
	public void setChampion(boolean champ)
	{
		_champion = champ;
	}
	
	@Override
	public boolean isChampion()
	{
		return _champion;
	}
	
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	@Override
	public L2Attackable getAttackable()
	{
		return this;
	}
	
	public boolean canShowLevelInTitle()
	{
		return !(getName().equals("Chest"));
	}
}
