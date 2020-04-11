package net.sf.l2j.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.commons.concurrent.ThreadPool;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.data.xml.HerbDropData;
import net.sf.l2j.gameserver.enums.BossInfoType;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.container.monster.OverhitState;
import net.sf.l2j.gameserver.model.actor.container.monster.SeedState;
import net.sf.l2j.gameserver.model.actor.container.monster.SpoilState;
import net.sf.l2j.gameserver.model.actor.container.npc.AbsorbInfo;
import net.sf.l2j.gameserver.model.actor.container.npc.AggroInfo;
import net.sf.l2j.gameserver.model.actor.container.npc.MinionList;
import net.sf.l2j.gameserver.model.actor.container.npc.RewardInfo;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.DropCategory;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * A monster extends {@link Attackable} class.<br>
 * <br>
 * It is an attackable {@link Creature}, with the capability to hold minions/master.
 */
public class Monster extends Attackable
{
	private final Map<Integer, AbsorbInfo> _absorbersList = new ConcurrentHashMap<>();
	
	private final OverhitState _overhitState = new OverhitState(this);
	private final SpoilState _spoilState = new SpoilState();
	private final SeedState _seedState = new SeedState(this);
	
	private Monster _master;
	private MinionList _minionList;
	
	private ScheduledFuture<?> _ccTask;
	
	private CommandChannel _firstCcAttacker;
	
	private long _lastCcAttack;
	
	private boolean _isRaid;
	private boolean _isMinion;
	
	private boolean _isChampion;
	
	public Monster(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/**
	 * Distribute Exp and SP rewards to Player (including Summon owner) that hit the L2Attackable and to their Party members.
	 * <ul>
	 * <li>Get the Player owner of the Servitor (if necessary) and L2Party in progress</li>
	 * <li>Calculate the Experience and SP rewards in function of the level difference</li>
	 * <li>Add Exp and SP rewards to Player (including Summon penalty) and to Party members in the known area of the last attacker</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT>
	 * @param lastAttacker The Creature that has killed the L2Attackable
	 */
	@Override
	protected void calculateRewards(Creature lastAttacker)
	{
		if (getAggroList().isEmpty())
			return;
		
		// Creates an empty list of rewards.
		final Map<Creature, RewardInfo> rewards = new ConcurrentHashMap<>();
		
		Player maxDealer = null;
		int maxDamage = 0;
		long totalDamage = 0;
		
		// Go through the _aggroList of the L2Attackable.
		for (AggroInfo info : getAggroList().values())
		{
			if (!(info.getAttacker() instanceof Playable))
				continue;
			
			// Get the Creature corresponding to this attacker.
			final Playable attacker = (Playable) info.getAttacker();
			
			// Get damages done by this attacker.
			final int damage = info.getDamage();
			if (damage <= 1)
				continue;
			
			// Check if attacker isn't too far from this.
			if (!MathUtil.checkIfInRange(Config.PARTY_RANGE, this, attacker, true))
				continue;
			
			final Player attackerPlayer = attacker.getActingPlayer();
			
			totalDamage += damage;
			
			// Calculate real damages (Summoners should get own damage plus summon's damage).
			RewardInfo reward = rewards.get(attacker);
			if (reward == null)
			{
				reward = new RewardInfo(attacker);
				rewards.put(attacker, reward);
			}
			reward.addDamage(damage);
			
			if (attacker instanceof Summon)
			{
				reward = rewards.get(attackerPlayer);
				if (reward == null)
				{
					reward = new RewardInfo(attackerPlayer);
					rewards.put(attackerPlayer, reward);
				}
				reward.addDamage(damage);
			}
			
			if (reward.getDamage() > maxDamage)
			{
				maxDealer = attackerPlayer;
				maxDamage = reward.getDamage();
			}
		}
		
		// Command channel restriction ; if a CC is registered, the main contributor is the channel leader, no matter the participation of the channel, and no matter the damage done by other participants.
		if (_firstCcAttacker != null)
			maxDealer = _firstCcAttacker.getLeader();
		
		// Manage Base, Quests and Sweep drops of the L2Attackable.
		doItemDrop(getTemplate(), (maxDealer != null && maxDealer.isOnline()) ? maxDealer : lastAttacker);
		
		for (RewardInfo reward : rewards.values())
		{
			if (reward.getAttacker() instanceof Summon)
				continue;
			
			// Attacker to be rewarded.
			final Player attacker = reward.getAttacker().getActingPlayer();
			
			// Total amount of damage done.
			final int damage = reward.getDamage();
			
			// Get party.
			final Party attackerParty = attacker.getParty();
			
			// Penalty applied to the attacker's XP
			final float penalty = attacker.hasServitor() ? ((Servitor) attacker.getSummon()).getExpPenalty() : 0;
			
			// If there's NO party in progress.
			if (attackerParty == null)
			{
				// Calculate Exp and SP rewards.
				if (!attacker.isDead() && attacker.knows(this))
				{
					// Calculate the difference of level between this attacker and the L2Attackable.
					final int levelDiff = attacker.getLevel() - getLevel();
					
					final int[] expSp = calculateExpAndSp(levelDiff, damage, totalDamage);
					long exp = expSp[0];
					int sp = expSp[1];
					
					exp *= 1 - penalty;
					
					// Test over-hit.
					if (_overhitState.isValidOverhit(attacker))
					{
						attacker.sendPacket(SystemMessageId.OVER_HIT);
						exp += _overhitState.calculateOverhitExp(exp);
					}
					
					// Set new karma.
					attacker.updateKarmaLoss(exp);
					
					// Distribute the Exp and SP between the Player and its L2Summon.
					attacker.addExpAndSp(exp, sp, rewards);
				}
			}
			// Share with party members.
			else
			{
				int partyDmg = 0;
				float partyMul = 1;
				int partyLvl = 0;
				
				// Get all Creature that can be rewarded in the party.
				final List<Player> rewardedMembers = new ArrayList<>();
				
				// Go through all Player in the party.
				final List<Player> groupMembers = (attackerParty.isInCommandChannel()) ? attackerParty.getCommandChannel().getMembers() : attackerParty.getMembers();
				
				final Map<Creature, RewardInfo> playersWithPets = new HashMap<>();
				
				for (Player partyPlayer : groupMembers)
				{
					if (partyPlayer == null || partyPlayer.isDead())
						continue;
					
					// Get the RewardInfo of this Player from L2Attackable rewards
					final RewardInfo reward2 = rewards.get(partyPlayer);
					
					// If the Player is in the L2Attackable rewards add its damages to party damages
					if (reward2 != null)
					{
						if (MathUtil.checkIfInRange(Config.PARTY_RANGE, this, partyPlayer, true))
						{
							partyDmg += reward2.getDamage(); // Add Player damages to party damages
							rewardedMembers.add(partyPlayer);
							
							if (partyPlayer.getLevel() > partyLvl)
								partyLvl = (attackerParty.isInCommandChannel()) ? attackerParty.getCommandChannel().getLevel() : partyPlayer.getLevel();
						}
						rewards.remove(partyPlayer); // Remove the Player from the L2Attackable rewards
						
						playersWithPets.put(partyPlayer, reward2);
						if (partyPlayer.hasPet() && rewards.containsKey(partyPlayer.getSummon()))
							playersWithPets.put(partyPlayer.getSummon(), rewards.get(partyPlayer.getSummon()));
					}
					// Add Player of the party (that have attacked or not) to members that can be rewarded and in range of the monster.
					else
					{
						if (MathUtil.checkIfInRange(Config.PARTY_RANGE, this, partyPlayer, true))
						{
							rewardedMembers.add(partyPlayer);
							if (partyPlayer.getLevel() > partyLvl)
								partyLvl = (attackerParty.isInCommandChannel()) ? attackerParty.getCommandChannel().getLevel() : partyPlayer.getLevel();
						}
					}
				}
				
				// If the party didn't killed this L2Attackable alone
				if (partyDmg < totalDamage)
					partyMul = ((float) partyDmg / totalDamage);
				
				// Calculate the level difference between Party and L2Attackable
				final int levelDiff = partyLvl - getLevel();
				
				// Calculate Exp and SP rewards
				final int[] expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage);
				long exp = expSp[0];
				int sp = expSp[1];
				
				exp *= partyMul;
				sp *= partyMul;
				
				// Test over-hit.
				if (_overhitState.isValidOverhit(attacker))
				{
					attacker.sendPacket(SystemMessageId.OVER_HIT);
					exp += _overhitState.calculateOverhitExp(exp);
				}
				
				// Distribute Experience and SP rewards to Player Party members in the known area of the last attacker
				if (partyDmg > 0)
					attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, playersWithPets);
			}
		}
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		// FIXME: to test to allow monsters hit others monsters
		if (attacker instanceof Monster)
			return false;
		
		return true;
	}
	
	@Override
	public boolean isAggressive()
	{
		return getTemplate().getAggroRange() > 0;
	}
	
	@Override
	public void onSpawn()
	{
		// Generate minions and spawn them (initial call and regular minions respawn are handled in the same method).
		if (!getTemplate().getMinionData().isEmpty())
			getMinionList().spawnMinions();
		
		super.onSpawn();
		
		// Clear over-hit state.
		_overhitState.clear();
		
		// Clear spoil state.
		_spoilState.clear();
		
		// Clear seed state.
		_seedState.clear();
		
		_absorbersList.clear();
	}
	
	@Override
	public void onTeleported()
	{
		super.onTeleported();
		
		if (hasMinions())
			getMinionList().onMasterTeleported();
	}
	
	@Override
	public void deleteMe()
	{
		if (hasMinions())
			getMinionList().onMasterDeletion();
		else if (_master != null)
			_master.getMinionList().onMinionDeletion(this);
		
		super.deleteMe();
	}
	
	@Override
	public Monster getMaster()
	{
		return _master;
	}
	
	@Override
	public boolean isRaidBoss()
	{
		return _isRaid && !_isMinion;
	}
	
	@Override
	public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if (attacker != null && isRaidBoss())
		{
			final Party party = attacker.getParty();
			if (party != null)
			{
				final CommandChannel cc = party.getCommandChannel();
				if (BossInfoType.isCcMeetCondition(cc, getNpcId()))
				{
					if (_ccTask == null)
					{
						_ccTask = ThreadPool.scheduleAtFixedRate(() -> checkCcLastAttack(), 1000, 1000);
						_lastCcAttack = System.currentTimeMillis();
						_firstCcAttacker = cc;
						
						// Broadcast message.
						broadcastPacket(new ExShowScreenMessage(BossInfoType.getBossInfo(getNpcId()).getCcRightsMsg().replaceAll("%s", cc.getLeader().getName()), 10000));
					}
					else if (_firstCcAttacker.equals(cc))
						_lastCcAttack = System.currentTimeMillis();
				}
			}
		}
		// Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}
	
	/**
	 * Set this object as part of raid (it can be either a boss or a minion).<br>
	 * <br>
	 * This state affects behaviors such as auto loot configs, Command Channel acquisition, or even Config related to raid bosses.<br>
	 * <br>
	 * A raid boss can't be lethal-ed, and a raid curse occurs if the level difference is too high.
	 * @param isRaid : if true, this object will be set as a raid.
	 */
	public void setRaid(boolean isRaid)
	{
		_isRaid = isRaid;
	}
	
	@Override
	public boolean isRaidRelated()
	{
		return _isRaid;
	}
	
	@Override
	public boolean isMinion()
	{
		return _isMinion;
	}
	
	/**
	 * Set this {@link Monster} as a minion instance.
	 * @param isRaidMinion : If true, this instance is considered a raid minion.
	 */
	public void setMinion(boolean isRaidMinion)
	{
		_isRaid = isRaidMinion;
		_isMinion = true;
	}
	
	public OverhitState getOverhitState()
	{
		return _overhitState;
	}
	
	public SpoilState getSpoilState()
	{
		return _spoilState;
	}
	
	public SeedState getSeedState()
	{
		return _seedState;
	}
	
	/**
	 * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.
	 * @param user : The Player who attacked the monster.
	 * @param crystal : The ItemInstance which was used to register.
	 */
	public void addAbsorber(Player user, ItemInstance crystal)
	{
		// If the Creature attacker isn't already in the _absorbersList of this L2Attackable, add it
		AbsorbInfo ai = _absorbersList.get(user.getObjectId());
		if (ai == null)
		{
			// Create absorb info.
			_absorbersList.put(user.getObjectId(), new AbsorbInfo(crystal.getObjectId()));
		}
		else
		{
			// Add absorb info, unless already registered.
			if (!ai.isRegistered())
				ai.setItemId(crystal.getObjectId());
		}
	}
	
	public void registerAbsorber(Player user)
	{
		// Get AbsorbInfo for user.
		AbsorbInfo ai = _absorbersList.get(user.getObjectId());
		if (ai == null)
			return;
		
		// Check item being used and register player to mob's absorber list.
		if (user.getInventory().getItemByObjectId(ai.getItemId()) == null)
			return;
		
		// Register AbsorbInfo.
		if (!ai.isRegistered())
		{
			ai.setAbsorbedHpPercent((int) ((100 * getCurrentHp()) / getMaxHp()));
			ai.setRegistered(true);
		}
	}
	
	public AbsorbInfo getAbsorbInfo(int npcObjectId)
	{
		return _absorbersList.get(npcObjectId);
	}
	
	/**
	 * Calculate the XP and SP to distribute to the attacker of the {@link Monster}.
	 * @param diff : The difference of level between the attacker and the Monster.
	 * @param damage : The damages done by the attacker.
	 * @param totalDamage : The total damage done.
	 * @return an array consisting of xp and sp values.
	 */
	private int[] calculateExpAndSp(int diff, int damage, long totalDamage)
	{
		// Calculate damage ratio.
		double xp = (double) getExpReward() * damage / totalDamage;
		double sp = (double) getSpReward() * damage / totalDamage;
		
		// Calculate level ratio.
		if (diff > 5)
		{
			double pow = Math.pow((double) 5 / 6, diff - 5);
			xp = xp * pow;
			sp = sp * pow;
		}
		
		// Add champion ratio, if any.
		if (isChampion())
		{
			xp *= Config.CHAMPION_REWARDS;
			sp *= Config.CHAMPION_REWARDS;
		}
		
		// If the XP is inferior or equals 0, don't reward any SP. Both XP and SP can't be inferior to 0.
		if (xp <= 0)
		{
			xp = 0;
			sp = 0;
		}
		else if (sp <= 0)
			sp = 0;
		
		return new int[]
		{
			(int) xp,
			(int) sp
		};
	}
	
	public void setMaster(Monster master)
	{
		_master = master;
	}
	
	public boolean hasMinions()
	{
		return _minionList != null;
	}
	
	public MinionList getMinionList()
	{
		if (_minionList == null)
			_minionList = new MinionList(this);
		
		return _minionList;
	}
	
	@Override
	public final boolean isChampion()
	{
		return _isChampion;
	}
	
	public final void setChampion(boolean value)
	{
		_isChampion = value;
	}
	
	/**
	 * Teleport this {@link Monster} to its master.
	 */
	public void teleToMaster()
	{
		if (_master == null)
			return;
		
		// Init the position of the Minion and add it in the world as a visible object
		final int offset = (int) (100 + getCollisionRadius() + _master.getCollisionRadius());
		final int minRadius = (int) (_master.getCollisionRadius() + 30);
		
		int newX = Rnd.get(minRadius * 2, offset * 2); // x
		int newY = Rnd.get(newX, offset * 2); // distance
		newY = (int) Math.sqrt(newY * newY - newX * newX); // y
		
		if (newX > offset + minRadius)
			newX = _master.getX() + newX - offset;
		else
			newX = _master.getX() - newX + minRadius;
		
		if (newY > offset + minRadius)
			newY = _master.getY() + newY - offset;
		else
			newY = _master.getY() - newY + minRadius;
		
		teleportTo(newX, newY, _master.getZ(), 0);
	}
	
	/**
	 * Calculates quantity of items for specific drop acording to current situation.
	 * @param drop The L2DropData count is being calculated for
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @param isSweep if true, use spoil drop chance.
	 * @return the ItemHolder.
	 */
	private IntIntHolder calculateRewardItem(DropData drop, int levelModifier, boolean isSweep)
	{
		// Get default drop chance
		double dropChance = drop.getChance();
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int deepBlueDrop = 1;
			if (levelModifier > 0)
			{
				// We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
				// NOTE: This is valid only for adena drops! Others drops will still obey server's rate
				deepBlueDrop = 3;
				if (drop.getItemId() == 57)
				{
					deepBlueDrop *= (isRaidBoss()) ? (int) Config.RATE_DROP_ITEMS_BY_RAID : (int) Config.RATE_DROP_ITEMS;
					if (deepBlueDrop == 0) // avoid div by 0
						deepBlueDrop = 1;
				}
			}
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			dropChance = ((drop.getChance() - ((drop.getChance() * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Applies Drop rates
		if (drop.getItemId() == 57)
			dropChance *= Config.RATE_DROP_ADENA;
		else if (isSweep)
			dropChance *= Config.RATE_DROP_SPOIL;
		else
			dropChance *= (isRaidBoss()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		
		if (isChampion())
			dropChance *= Config.CHAMPION_REWARDS;
		
		// Set our limits for chance of drop
		if (dropChance < 1)
			dropChance = 1;
		
		// Get min and max Item quantity that can be dropped in one time
		final int minCount = drop.getMinDrop();
		final int maxCount = drop.getMaxDrop();
		
		// Get the item quantity dropped
		int itemCount = 0;
		
		// Check if the Item must be dropped
		int random = Rnd.get(DropData.MAX_CHANCE);
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
				itemCount += Rnd.get(minCount, maxCount);
			else if (minCount == maxCount)
				itemCount += minCount;
			else
				itemCount++;
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= DropData.MAX_CHANCE;
		}
		
		if (isChampion())
			if (drop.getItemId() == 57 || (drop.getItemId() >= 6360 && drop.getItemId() <= 6362))
				itemCount *= Config.CHAMPION_ADENAS_REWARDS;
			
		if (itemCount > 0)
			return new IntIntHolder(drop.getItemId(), itemCount);
		
		return null;
	}
	
	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation <br>
	 * Only a max of ONE item from a category is allowed to be dropped.
	 * @param categoryDrops The category to make checks on.
	 * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @return the ItemHolder.
	 */
	private IntIntHolder calculateCategorizedRewardItem(DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
			return null;
			
		// Get default drop chance for the category (that's the sum of chances for all items in the category)
		// keep track of the base category chance as it'll be used later, if an item is drop from the category.
		// for everything else, use the total "categoryDropChance"
		int basecategoryDropChance = categoryDrops.getCategoryChance();
		int categoryDropChance = basecategoryDropChance;
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int deepBlueDrop = (levelModifier > 0) ? 3 : 1;
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			categoryDropChance = ((categoryDropChance - ((categoryDropChance * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Applies Drop rates
		categoryDropChance *= (isRaidBoss()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
		
		if (isChampion())
			categoryDropChance *= Config.CHAMPION_REWARDS;
		
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
			categoryDropChance = 1;
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(DropData.MAX_CHANCE) < categoryDropChance)
		{
			DropData drop = categoryDrops.dropOne(isRaidBoss());
			if (drop == null)
				return null;
				
			// Now decide the quantity to drop based on the rates and penalties. To get this value
			// simply divide the modified categoryDropChance by the base category chance. This
			// results in a chance that will dictate the drops amounts: for each amount over 100
			// that it is, it will give another chance to add to the min/max quantities.
			//
			// For example, If the final chance is 120%, then the item should drop between
			// its min and max one time, and then have 20% chance to drop again. If the final
			// chance is 330%, it will similarly give 3 times the min and max, and have a 30%
			// chance to give a 4th time.
			// At least 1 item will be dropped for sure. So the chance will be adjusted to 100%
			// if smaller.
			
			double dropChance = drop.getChance();
			if (drop.getItemId() == 57)
				dropChance *= Config.RATE_DROP_ADENA;
			else
				dropChance *= (isRaidBoss()) ? Config.RATE_DROP_ITEMS_BY_RAID : Config.RATE_DROP_ITEMS;
			
			if (isChampion())
				dropChance *= Config.CHAMPION_REWARDS;
			
			if (dropChance < DropData.MAX_CHANCE)
				dropChance = DropData.MAX_CHANCE;
			
			// Get min and max Item quantity that can be dropped in one time
			final int min = drop.getMinDrop();
			final int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Check if the Item must be dropped
			int random = Rnd.get(DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
					itemCount += Rnd.get(min, max);
				else if (min == max)
					itemCount += min;
				else
					itemCount++;
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= DropData.MAX_CHANCE;
			}
			
			if (isChampion())
				if (drop.getItemId() == 57 || (drop.getItemId() >= 6360 && drop.getItemId() <= 6362))
					itemCount *= Config.CHAMPION_ADENAS_REWARDS;
				
			if (itemCount > 0)
				return new IntIntHolder(drop.getItemId(), itemCount);
		}
		return null;
	}
	
	/**
	 * @param lastAttacker The Player that has killed the L2Attackable
	 * @return the level modifier for drop
	 */
	private int calculateLevelModifierForDrop(Player lastAttacker)
	{
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			for (Creature atkChar : getAttackByList())
				if (atkChar.getLevel() > highestLevel)
					highestLevel = atkChar.getLevel();
				
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if (highestLevel - 9 >= getLevel())
				return ((highestLevel - (getLevel() + 8)) * 9);
		}
		return 0;
	}
	
	private static IntIntHolder calculateCategorizedHerbItem(DropCategory categoryDrops, int levelModifier)
	{
		if (categoryDrops == null)
			return null;
		
		int categoryDropChance = categoryDrops.getCategoryChance();
		
		// Applies Drop rates
		switch (categoryDrops.getCategoryType())
		{
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
		
		// Drop chance is affected by deep blue drop rule.
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int deepBlueDrop = (levelModifier > 0) ? 3 : 1;
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			categoryDropChance = ((categoryDropChance - ((categoryDropChance * levelModifier) / 100)) / deepBlueDrop);
		}
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(DropData.MAX_CHANCE) < Math.max(1, categoryDropChance))
		{
			final DropData drop = categoryDrops.dropOne(false);
			if (drop == null)
				return null;
			
			/*
			 * Now decide the quantity to drop based on the rates and penalties. To get this value, simply divide the modified categoryDropChance by the base category chance. This results in a chance that will dictate the drops amounts : for each amount over 100 that it is, it will give another
			 * chance to add to the min/max quantities. For example, if the final chance is 120%, then the item should drop between its min and max one time, and then have 20% chance to drop again. If the final chance is 330%, it will similarly give 3 times the min and max, and have a 30% chance to
			 * give a 4th time. At least 1 item will be dropped for sure. So the chance will be adjusted to 100% if smaller.
			 */
			double dropChance = drop.getChance();
			
			switch (categoryDrops.getCategoryType())
			{
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
			
			if (dropChance < DropData.MAX_CHANCE)
				dropChance = DropData.MAX_CHANCE;
			
			// Get min and max Item quantity that can be dropped in one time
			final int min = drop.getMinDrop();
			final int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			// Check if the Item must be dropped
			int random = Rnd.get(DropData.MAX_CHANCE);
			while (random < dropChance)
			{
				// Get the item quantity dropped
				if (min < max)
					itemCount += Rnd.get(min, max);
				else if (min == max)
					itemCount += min;
				else
					itemCount++;
				
				// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
				dropChance -= DropData.MAX_CHANCE;
			}
			
			if (itemCount > 0)
				return new IntIntHolder(drop.getItemId(), itemCount);
		}
		return null;
	}
	
	/**
	 * Manage Base & Quests drops of this {@link Attackable} using an associated {@link NpcTemplate} (can be its or another template).<br>
	 * <br>
	 * This method is called by {@link Attackable#calculateRewards}.
	 * @param npcTemplate : The template used to retrieve drops.
	 * @param mainDamageDealer : The Creature that made the most damage.
	 */
	public void doItemDrop(NpcTemplate npcTemplate, Creature mainDamageDealer)
	{
		if (mainDamageDealer == null)
			return;
		
		// Don't drop anything if the last attacker or owner isn't Player
		final Player player = mainDamageDealer.getActingPlayer();
		if (player == null)
			return;
		
		// level modifier in %'s (will be subtracted from drop chance)
		final int levelModifier = calculateLevelModifierForDrop(player);
		
		CursedWeaponManager.getInstance().checkDrop(this, player);
		
		// now throw all categorized drops and handle spoil.
		for (DropCategory cat : npcTemplate.getDropData())
		{
			IntIntHolder item = null;
			if (cat.isSweep())
			{
				if (getSpoilState().isSpoiled())
				{
					for (DropData drop : cat.getAllDrops())
					{
						item = calculateRewardItem(drop, levelModifier, true);
						if (item == null)
							continue;
						
						getSpoilState().getSweepItems().add(item);
					}
				}
			}
			else
			{
				if (getSeedState().isSeeded())
				{
					DropData drop = cat.dropSeedAllowedDropsOnly();
					if (drop == null)
						continue;
					
					item = calculateRewardItem(drop, levelModifier, false);
				}
				else
					item = calculateCategorizedRewardItem(cat, levelModifier);
				
				if (item != null)
				{
					// Check if the autoLoot mode is active
					if ((isRaidBoss() && Config.AUTO_LOOT_RAID) || (!isRaidBoss() && Config.AUTO_LOOT))
						player.doAutoLoot(this, item);
					else
						dropItem(player, item);
					
					// Broadcast message if RaidBoss was defeated
					if (isRaidBoss())
						broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2).addCharName(this).addItemName(item.getId()).addNumber(item.getValue()));
				}
			}
		}
		
		// Apply special item drop for champions.
		if (isChampion() && Config.CHAMPION_REWARD > 0)
		{
			int dropChance = Config.CHAMPION_REWARD;
			
			// Apply level modifier, if any/wanted.
			if (Config.DEEPBLUE_DROP_RULES)
			{
				int deepBlueDrop = (levelModifier > 0) ? 3 : 1;
				
				// Check if we should apply our maths so deep blue mobs will not drop that easy.
				dropChance = ((Config.CHAMPION_REWARD - ((Config.CHAMPION_REWARD * levelModifier) / 100)) / deepBlueDrop);
			}
			
			if (Rnd.get(100) < dropChance)
			{
				final IntIntHolder item = new IntIntHolder(Config.CHAMPION_REWARD_ID, Math.max(1, Rnd.get(1, Config.CHAMPION_REWARD_QTY)));
				if (Config.AUTO_LOOT)
					player.addItem("ChampionLoot", item.getId(), item.getValue(), this, true);
				else
					dropItem(player, item);
			}
		}
		
		// Herbs.
		if (getTemplate().getDropHerbGroup() > 0)
		{
			for (DropCategory cat : HerbDropData.getInstance().getHerbDroplist(getTemplate().getDropHerbGroup()))
			{
				final IntIntHolder item = calculateCategorizedHerbItem(cat, levelModifier);
				if (item != null)
				{
					if (Config.AUTO_LOOT_HERBS)
						player.addItem("Loot", item.getId(), 1, this, true);
					else
					{
						// If multiple similar herbs drop, split them and make a unique drop per item.
						final int count = item.getValue();
						if (count > 1)
						{
							item.setValue(1);
							for (int i = 0; i < count; i++)
								dropItem(player, item);
						}
						else
							dropItem(player, item);
					}
				}
			}
		}
	}
	
	/**
	 * Drop reward item.
	 * @param mainDamageDealer The player who made highest damage.
	 * @param holder The ItemHolder.
	 */
	private void dropItem(Player mainDamageDealer, IntIntHolder holder)
	{
		for (int i = 0; i < holder.getValue(); i++)
		{
			// Init the dropped ItemInstance and add it in the world as a visible object at the position where mob was last
			final ItemInstance item = ItemInstance.create(holder.getId(), holder.getValue(), mainDamageDealer, this);
			item.setDropProtection(mainDamageDealer.getObjectId(), isRaidBoss());
			item.dropMe(this, getX() + Rnd.get(-70, 70), getY() + Rnd.get(-70, 70), Math.max(getZ(), mainDamageDealer.getZ()) + 20);
			
			// If stackable, end loop as entire count is included in 1 instance of item.
			if (item.isStackable() || !Config.MULTIPLE_ITEM_DROP)
				break;
		}
	}
	
	/**
	 * Check CommandChannel loot priority every second. After 5min, the loot priority dissapears.
	 */
	private void checkCcLastAttack()
	{
		// We're still on time, do nothing.
		if (System.currentTimeMillis() - _lastCcAttack <= 300000)
			return;
		
		// Reset variables.
		_firstCcAttacker = null;
		_lastCcAttack = 0;
		
		// Set task to null.
		if (_ccTask != null)
		{
			_ccTask.cancel(false);
			_ccTask = null;
		}
		
		// Broadcast message.
		broadcastPacket(new ExShowScreenMessage(BossInfoType.getBossInfo(getNpcId()).getCcNoRightsMsg(), 10000));
	}
}