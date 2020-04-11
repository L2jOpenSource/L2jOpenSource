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
package l2r.gameserver.model.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javolution.util.FastMap;
import l2r.Config;
import l2r.gameserver.SevenSigns;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.ai.L2AttackableAI;
import l2r.gameserver.ai.L2CharacterAI;
import l2r.gameserver.ai.L2FortSiegeGuardAI;
import l2r.gameserver.ai.L2SiegeGuardAI;
import l2r.gameserver.datatables.EventDroplist;
import l2r.gameserver.datatables.EventDroplist.DateDrop;
import l2r.gameserver.datatables.xml.HerbDropData;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.datatables.xml.ManorData;
import l2r.gameserver.enums.CtrlEvent;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.enums.QuestEventType;
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
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.events.AttackableEvents;
import l2r.gameserver.model.actor.instance.L2GrandBossInstance;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;
import l2r.gameserver.model.actor.instance.L2TrapInstance;
import l2r.gameserver.model.actor.knownlist.AttackableKnownList;
import l2r.gameserver.model.actor.stat.Rates;
import l2r.gameserver.model.actor.status.AttackableStatus;
import l2r.gameserver.model.actor.tasks.attackable.CommandChannelTimer;
import l2r.gameserver.model.actor.tasks.attackable.OnKillNotifyTask;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.holders.ItemHolder;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.L2Item;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.taskmanager.DecayTaskManager;
import l2r.gameserver.util.Util;
import l2r.util.Rnd;
import gr.reunion.interf.ReunionEvents;

public class L2Attackable extends L2Npc
{
	private boolean _isRaid = false;
	private boolean _isRaidMinion = false;
	private boolean _champion = false;
	private final Map<L2Character, AggroInfo> _aggroList = new FastMap<L2Character, AggroInfo>().shared();
	private boolean _isReturningToSpawnPoint = false;
	private boolean _canReturnToSpawnPoint = true;
	private boolean _seeThroughSilentMove = false;
	private ItemHolder[] _sweepItems;
	private ItemHolder[] _harvestItems;
	private boolean _seeded;
	private int _seedType = 0;
	private int _seederObjId = 0;
	
	private boolean _overhit;
	
	private double _overhitDamage;
	
	private L2Character _overhitAttacker;
	
	private volatile L2CommandChannel _firstCommandChannelAttacked = null;
	private CommandChannelTimer _commandChannelTimer = null;
	private long _commandChannelLastAttack = 0;
	
	private boolean _absorbed;
	
	private final Map<Integer, AbsorberInfo> _absorbersList = new ConcurrentHashMap<>();
	
	private volatile boolean _mustGiveExpSp;
	private volatile boolean _doItemDrop;
	
	/** True if a Dwarf has used Spoil on this L2NpcInstance */
	private boolean _isSpoil = false;
	
	private int _isSpoiledBy = 0;
	
	protected int _onKillDelay = 5000;
	
	/**
	 * Constructor of L2Attackable (use L2Character and L2NpcInstance constructor).<br>
	 * Actions:<br>
	 * Call the L2Character constructor to set the _template of the L2Attackable (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)<br>
	 * Set the name of the L2Attackable<br>
	 * Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it.
	 * @param objectId identifier of the object initialized.
	 * @param template the template to apply to the NPC.
	 */
	public L2Attackable(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
	public void initCharEvents()
	{
		setCharEvents(new AttackableEvents(this));
	}
	
	@Override
	public AttackableEvents getEvents()
	{
		return (AttackableEvents) super.getEvents();
	}
	
	@Override
	protected L2CharacterAI initAI()
	{
		return new L2AttackableAI(new AIAccessor());
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
		
		// Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
		if (attacker != null)
		{
			addDamage(attacker, (int) damage, skill);
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
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
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
		
		// Notify the Quest Engine of the L2Attackable death if necessary
		try
		{
			L2PcInstance player = null;
			
			if (killer != null)
			{
				player = killer.getActingPlayer();
			}
			
			if (player != null)
			{
				if (ReunionEvents.isInEvent(player))
				{
					ReunionEvents.onKill(player, this);
				}
				
				if (getTemplate().getEventQuests(QuestEventType.ON_KILL) != null)
				{
					for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_KILL))
					{
						ThreadPoolManager.getInstance().scheduleEffect(new OnKillNotifyTask(this, quest, player, (killer != null) && killer.isSummon()), _onKillDelay);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.error(String.valueOf(e));
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
			
			if (mob.hasMinions())
			{
				mob.getMinionList().onMasterDie(false);
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
			if (getAggroList().isEmpty())
			{
				return;
			}
			
			// NOTE: Concurrent-safe map is used because while iterating to verify all conditions sometimes an entry must be removed.
			final Map<L2PcInstance, DamageDoneInfo> rewards = new ConcurrentHashMap<>();
			L2PcInstance maxDealer = null;
			int maxDamage = 0;
			long totalDamage = 0;
			// While Iterating over This Map Removing Object is Not Allowed
			// Go through the _aggroList of the L2Attackable
			for (AggroInfo info : getAggroList().values())
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
						DamageDoneInfo reward = rewards.get(attacker);
						if (reward == null)
						{
							reward = new DamageDoneInfo(attacker, damage);
							rewards.put(attacker, reward);
						}
						else
						{
							reward.addDamage(damage);
						}
						
						if (reward.getDamage() > maxDamage)
						{
							maxDealer = attacker;
							maxDamage = reward.getDamage();
						}
					}
				}
			}
			
			// Manage Base, Quests and Sweep drops of the L2Attackable
			doItemDrop((maxDealer != null) && maxDealer.isOnline() ? maxDealer : lastAttacker);
			
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
							
							final int[] expSp = calculateExpAndSp(levelDiff, damage, totalDamage, attacker.isPremium());
							long exp = expSp[0];
							int sp = expSp[1];
							
							if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
							{
								exp *= Config.L2JMOD_CHAMPION_REWARDS;
								sp *= Config.L2JMOD_CHAMPION_REWARDS;
							}
							
							exp *= 1 - penalty;
							
							// Check for an over-hit enabled strike
							L2Character overhitAttacker = getOverhitAttacker();
							if (isOverhit() && (overhitAttacker != null) && (overhitAttacker.getActingPlayer() != null) && (attacker == overhitAttacker.getActingPlayer()))
							{
								attacker.sendPacket(SystemMessageId.OVER_HIT);
								exp += calculateOverhitExp(exp);
							}
							
							// Distribute the Exp and SP between the L2PcInstance and its L2Summon
							if (!attacker.isDead())
							{
								final long addexp = Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null));
								final int addsp = (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null);
								
								attacker.addExpAndSp(addexp, addsp, useVitalityRate());
								if (addexp > 0)
								{
									attacker.updateVitalityPoints(getVitalityPoints(damage), true, false);
									PcCafePointsManager.getInstance().givePcCafePoint(attacker, addexp);
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
						
						final int[] expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage, attacker.isPremium());
						long exp_premium = expSp[0];
						int sp_premium = expSp[1];
						long exp = expSp[0];
						int sp = expSp[1];
						
						if (Config.L2JMOD_CHAMPION_ENABLE && isChampion())
						{
							exp *= Config.L2JMOD_CHAMPION_REWARDS;
							sp *= Config.L2JMOD_CHAMPION_REWARDS;
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
							exp += calculateOverhitExp(exp);
						}
						
						// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
						if (partyDmg > 0)
						{
							attackerParty.distributeXpAndSp(exp_premium, sp_premium, exp, sp, rewardedMembers, partyLvl, partyDmg, this);
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
			try
			{
				// If monster is on walk - stop it
				if (isWalker() && !isCoreAIDisabled() && WalkingManager.getInstance().isOnWalk(this))
				{
					WalkingManager.getInstance().stopMoving(this, false, true);
				}
				
				getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker);
				addDamageHate(attacker, damage, (damage * 100) / (getLevel() + 7));
				
				final L2PcInstance player = attacker.getActingPlayer();
				if (player != null)
				{
					if (getTemplate().getEventQuests(QuestEventType.ON_ATTACK) != null)
					{
						for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_ATTACK))
						{
							try
							{
								quest.notifyAttack(this, player, damage, attacker.isSummon(), skill);
							}
							catch (Exception e)
							{
								if (Config.DEBUG_SCRIPT_NOTIFIES)
								{
									if (skill == null)
									{
										_log.error("L2Attackable[notifyAttack] Skill is NULL.");
										_log.error("L2Attackable[notifyAttack] MobId is: " + String.valueOf(this.getId()));
									}
									
									if (damage == 0)
									{
										_log.error("L2Attackable[notifyAttack] Damage is 0");
									}
									
									_log.error("L2Attackable[notifyAttack] Attacker is: " + player.getName());
									e.printStackTrace();
								}
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
	}
	
	/**
	 * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
	 * @param attacker The L2Character that gave damages to this L2Attackable
	 * @param damage The number of damages given by the attacker L2Character
	 * @param aggro The hate (=damage) given by the attacker L2Character
	 */
	public void addDamageHate(L2Character attacker, int damage, int aggro)
	{
		if (attacker == null)
		{
			return;
		}
		
		L2PcInstance targetPlayer = attacker.getActingPlayer();
		// Get the AggroInfo of the attacker L2Character from the _aggroList of the L2Attackable
		AggroInfo ai = getAggroList().get(attacker);
		
		if (ai == null)
		{
			ai = new AggroInfo(attacker);
			getAggroList().put(attacker, ai);
		}
		ai.addDamage(damage);
		// traps does not cause aggro
		// making this hack because not possible to determine if damage made by trap
		// so just check for triggered trap here
		if ((targetPlayer == null) || (targetPlayer.getTraps().isEmpty()))
		{
			ai.addHate(aggro);
		}
		
		if (targetPlayer != null)
		{
			for (L2TrapInstance trap : targetPlayer.getTraps().values())
			{
				if ((trap != null) && !trap.isTriggered())
				{
					ai.addHate(aggro);
				}
			}
		}
		
		if ((targetPlayer != null) && (aggro == 0))
		{
			addDamageHate(attacker, 0, 1);
			
			// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
			if (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			}
			
			if (getTemplate().getEventQuests(QuestEventType.ON_AGGRO_RANGE_ENTER) != null)
			{
				for (Quest quest : getTemplate().getEventQuests(QuestEventType.ON_AGGRO_RANGE_ENTER))
				{
					quest.notifyAggroRangeEnter(this, targetPlayer, attacker.isSummon());
				}
			}
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
	
	public void reduceHate(L2Character target, int amount)
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
			
			for (AggroInfo ai : getAggroList().values())
			{
				if (ai == null)
				{
					return;
				}
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
		
		AggroInfo ai = getAggroList().get(target);
		if (ai == null)
		{
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
		AggroInfo ai = getAggroList().get(target);
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
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		L2Character mostHated = null;
		int maxHate = 0;
		
		// While Interacting over This Map Removing Object is Not Allowed
		// Go through the aggroList of the L2Attackable
		for (AggroInfo ai : getAggroList().values())
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
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		L2Character mostHated = null;
		L2Character secondMostHated = null;
		int maxHate = 0;
		List<L2Character> result = new ArrayList<>();
		
		// While iterating over this map removing objects is not allowed
		// Go through the aggroList of the L2Attackable
		for (AggroInfo ai : getAggroList().values())
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
		if (getAggroList().isEmpty() || isAlikeDead())
		{
			return null;
		}
		
		List<L2Character> result = new ArrayList<>();
		for (AggroInfo ai : getAggroList().values())
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
	public int getHating(final L2Character target)
	{
		if (getAggroList().isEmpty() || (target == null))
		{
			return 0;
		}
		
		final AggroInfo ai = getAggroList().get(target);
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
				getAggroList().remove(target);
				return 0;
			}
		}
		
		if (!ai.getAttacker().isVisible() || ai.getAttacker().isInvisible())
		{
			getAggroList().remove(target);
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
			ItemHolder item = null;
			if (cat.isSweep())
			{
				if (isSpoil())
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
					// Check if the autoLoot mode is active
					if (isFlying() || (!isRaid() && Config.AUTO_LOOT) || (isRaid() && Config.AUTO_LOOT_RAIDS))
					{
						player.doAutoLoot(this, item); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
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
			ItemHolder item = new ItemHolder(Config.L2JMOD_CHAMPION_REWARD_ID, ++champqty);
			
			if ((player.getLevel() <= getLevel()) && (Rnd.get(100) < Config.L2JMOD_CHAMPION_REWARD_LOWER_LVL_ITEM_CHANCE))
			{
				if (Config.AUTO_LOOT || isFlying())
				{
					player.addItem("ChampionLoot", item.getId(), item.getCount(), this, true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
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
					player.addItem("ChampionLoot", item.getId(), item.getCount(), this, true); // Give the item(s) to the L2PcInstance that has killed the L2Attackable
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
				final int itemId = drop.getEventDrop().getIdList()[Rnd.get(drop.getEventDrop().getIdList().length)];
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
		return getAggroList().isEmpty();
	}
	
	/**
	 * @param player The L2Character searched in the _aggroList of the L2Attackable
	 * @return True if the _aggroList of this L2Attackable contains the L2Character.
	 */
	public boolean containsTarget(L2Character player)
	{
		return getAggroList().containsKey(player);
	}
	
	/**
	 * Clear the _aggroList of the L2Attackable.
	 */
	public void clearAggroList()
	{
		getAggroList().clear();
		
		// clear overhit values
		_overhit = false;
		_overhitDamage = 0;
		_overhitAttacker = null;
	}
	
	/**
	 * @return True if a Dwarf use Sweep on the L2Attackable and if item can be spoiled.
	 */
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
				lootItems.add(ItemData.getInstance().createDummyItem(item.getId()).getItem());
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
	public synchronized ItemHolder[] takeHarvest()
	{
		ItemHolder[] harvest = _harvestItems;
		_harvestItems = null;
		return harvest;
	}
	
	/**
	 * Check if the corpse is too old.
	 * @param attacker the player to validate
	 * @param time the time to check
	 * @param sendMessage if {@code true} will send a message of corpse too old
	 * @return {@code true} if the corpse is too old
	 */
	public boolean isOldCorpse(L2PcInstance attacker, int time, boolean sendMessage)
	{
		if (DecayTaskManager.getInstance().getTasks().containsKey(this) && ((System.currentTimeMillis() - DecayTaskManager.getInstance().getTasks().get(this)) > time))
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
		if ((sweeper.getObjectId() != getIsSpoiledBy()) && !sweeper.isInLooterParty(getIsSpoiledBy()))
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
	private int[] calculateExpAndSp(int diff, int damage, long totalDamage, boolean isPremium)
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
			
			if (xp <= 0)
			{
				xp = 0;
				sp = 0;
			}
			else if (sp <= 0)
			{
				sp = 0;
			}
		}
		int[] tmp =
		{
			(int) xp,
			(int) sp
		};
		return tmp;
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
		setSpoil(false);
		// Clear all aggro char from list
		clearAggroList();
		// Clear Harvester Rewrard List
		_harvestItems = null;
		// Clear mod Seeded stat
		_seeded = false;
		_seedType = 0;
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
	 * @return True if this L2NpcInstance has drops that can be sweeped.
	 */
	public boolean isSpoil()
	{
		return _isSpoil;
	}
	
	/**
	 * Set the spoil state of this L2NpcInstance.
	 * @param isSpoil
	 */
	public void setSpoil(boolean isSpoil)
	{
		_isSpoil = isSpoil;
	}
	
	public final int getIsSpoiledBy()
	{
		return _isSpoiledBy;
	}
	
	public final void setIsSpoiledBy(int value)
	{
		_isSpoiledBy = value;
	}
	
	/**
	 * Sets state of the mob to seeded. Paramets needed to be set before.
	 * @param seeder
	 */
	public void setSeeded(L2PcInstance seeder)
	{
		if ((_seedType != 0) && (_seederObjId == seeder.getObjectId()))
		{
			setSeeded(_seedType, seeder.getLevel());
		}
	}
	
	/**
	 * Sets the seed parameters, but not the seed state
	 * @param id - id of the seed
	 * @param seeder - player who is sowind the seed
	 */
	public void setSeeded(int id, L2PcInstance seeder)
	{
		if (!_seeded)
		{
			_seedType = id;
			_seederObjId = seeder.getObjectId();
		}
	}
	
	private void setSeeded(int id, int seederLvl)
	{
		_seeded = true;
		_seedType = id;
		int count = 1;
		
		Set<Integer> skillIds = getTemplate().getSkills().keySet();
		
		if (skillIds != null)
		{
			for (int skillId : skillIds)
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
		}
		
		int diff = (getLevel() - (ManorData.getInstance().getSeedLevel(_seedType) - 5));
		
		// hi-lvl mobs bonus
		if (diff > 0)
		{
			count += diff;
		}
		
		_harvestItems = new ItemHolder[]
		{
			new ItemHolder(ManorData.getInstance().getCropType(_seedType), count * Config.RATE_DROP_MANOR)
		};
	}
	
	public int getSeederId()
	{
		return _seederObjId;
	}
	
	public int getSeedType()
	{
		return _seedType;
	}
	
	public boolean isSeeded()
	{
		return _seeded;
	}
	
	/**
	 * Set delay for onKill() call, in ms Default: 5000 ms
	 * @param delay
	 */
	public final void setOnKillDelay(int delay)
	{
		_onKillDelay = delay;
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
		clearAggroList();
		
		if (hasAI() && (getSpawn() != null))
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(getSpawn().getX(), getSpawn().getY(), getSpawn().getZ(), 0));
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
	
	public boolean canShowLevelInTitle()
	{
		return !(getName().equals("Chest"));
	}
}
