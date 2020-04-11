package main.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.holder.AggroInfoHolder;
import com.l2jfrozen.gameserver.model.holder.RewardInfoHolder;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.util.Util;

import main.enums.ExpSpType;

/**
 * @author fissban
 */
public class NpcExpInstance
{
	private Map<ExpSpType, Double> settings = new HashMap<>();
	{
		settings.put(ExpSpType.EXP, 1.0);
		settings.put(ExpSpType.SP, 1.0);
	}
	
	public NpcExpInstance()
	{
		//
	}
	
	public void increaseRate(ExpSpType type, double bonus)
	{
		double oldValue = settings.get(type);
		settings.put(type, oldValue + bonus - 1);
	}
	
	public boolean hasSettings()
	{
		for (Double value : settings.values())
		{
			if (value > 1.0)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void init(L2Attackable npc, L2Character lastAttacker)
	{
		if (npc.getAggroList().isEmpty())
		{
			return;
		}
		
		// Creates an empty list of rewards.
		final Map<L2Character, RewardInfoHolder> rewards = new ConcurrentHashMap<>();
		
		L2PcInstance maxDealer = null;
		int maxDamage = 0;
		
		// Go through the npc.getAggroList() of the Attackable.
		for (AggroInfoHolder info : npc.getAggroList().values())
		{
			if (info == null)
			{
				continue;
			}
			
			// Get the L2Character corresponding to this attacker
			L2Character attacker = info.getAttacker();
			L2Character ddealer;
			
			// Get damages done by this attacker
			int damage = info.getDmg();
			
			// Prevent unwanted behavior
			if (damage > 1)
			{
				if (attacker instanceof L2SummonInstance || attacker instanceof L2PetInstance && ((L2PetInstance) attacker).getPetData().getOwnerExpTaken() > 0)
				{
					ddealer = ((L2Summon) attacker).getOwner();
				}
				else
				{
					ddealer = info.getAttacker();
				}
				
				// Check if ddealer isn't too far from this (killed monster)
				if (!Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, ddealer, true))
				{
					continue;
				}
				
				// Calculate real damages (Summoners should get own damage plus summon's damage)
				RewardInfoHolder reward = rewards.get(ddealer);
				if (reward == null)
				{
					reward = new RewardInfoHolder(ddealer, damage);
				}
				else
				{
					reward.addDamage(damage);
				}
				
				rewards.put(ddealer, reward);
				
				if (ddealer instanceof L2PlayableInstance && ((L2PlayableInstance) ddealer).getActingPlayer() != null && reward.getDmg() > maxDamage)
				{
					maxDealer = ((L2PlayableInstance) ddealer).getActingPlayer();
					maxDamage = reward.getDmg();
				}
				
			}
		}
		
		// Manage Base, Quests and Sweep drops of the Attackable.
		npc.doItemDrop(npc.getTemplate(), maxDealer != null && maxDealer.isOnline() ? maxDealer : lastAttacker);
		
		for (RewardInfoHolder reward : rewards.values())
		{
			if (reward == null)
			{
				continue;
			}
			
			// Attacker to be rewarded.
			final L2PcInstance attacker = reward.getAttacker().getActingPlayer();
			
			// Total amount of damage done.
			final int damage = reward.getDmg();
			
			// Get party.
			final L2Party attackerParty = attacker.getParty();
			
			// Penalty applied to the attacker's XP
			float penalty = 0;
			if (attacker instanceof L2PcInstance && attacker.getPet() instanceof L2SummonInstance)
			{
				penalty = ((L2SummonInstance) attacker.getPet()).getExpPenalty();
			}
			
			// If there's NO party in progress.
			if (attackerParty == null)
			{
				// Calculate Exp and SP rewards
				if (attacker.getKnownList().knowsObject(npc))
				{
					// Calculate the difference of level between this attacker (L2PcInstance or L2SummonInstance owner) and the L2Attackable
					// mob = 24, atk = 10, diff = -14 (full xp)
					// mob = 24, atk = 28, diff = 4 (some xp)
					// mob = 24, atk = 50, diff = 26 (no xp)
					int levelDiff = attacker.getLevel() - npc.getLevel();
					
					int[] tmp = calculateExpAndSp(npc, levelDiff, damage);
					long exp = tmp[0];
					exp *= 1 - penalty;
					int sp = tmp[1];
					
					if (Config.L2JMOD_CHAMPION_ENABLE && npc.isChampion())
					{
						exp *= Config.L2JMOD_CHAMPION_REWARDS;
						sp *= Config.L2JMOD_CHAMPION_REWARDS;
					}
					
					// Check for an over-hit enabled strike and VIP options
					if (attacker instanceof L2PcInstance)
					{
						final L2PcInstance player = attacker;
						if (npc.isOverhit() && attacker == npc.getOverhitAttacker())
						{
							player.sendPacket(new SystemMessage(SystemMessageId.OVER_HIT));
							exp += npc.calculateOverhitExp(exp);
						}
						if (player.isVIP())
						{
							exp = (long) (exp * Config.VIP_XPSP_RATE);
							sp = (int) (sp * Config.VIP_XPSP_RATE);
						}
					}
					
					// Distribute the Exp and SP between the L2PcInstance and its L2Summon
					if (!attacker.isDead())
					{
						attacker.addExpAndSp(Math.round(attacker.calcStat(Stats.EXPSP_RATE, exp, null, null)), (int) attacker.calcStat(Stats.EXPSP_RATE, sp, null, null));
					}
				}
			}
			// Share with party members.
			else
			{
				
				// share with party members
				int partyDmg = 0;
				float partyMul = 1.f;
				int partyLvl = 0;
				
				// Get all L2Character that can be rewarded in the party
				final List<L2PlayableInstance> rewardedMembers = new ArrayList<>();
				
				// Go through all L2PcInstance in the party
				List<L2PcInstance> groupMembers;
				
				if (attackerParty.isInCommandChannel())
				{
					groupMembers = attackerParty.getCommandChannel().getMembers();
				}
				else
				{
					groupMembers = attackerParty.getPartyMembers();
				}
				
				for (final L2PcInstance pl : groupMembers)
				{
					if (pl == null || pl.isDead())
					{
						continue;
					}
					
					// Get the RewardInfo of this L2PcInstance from L2Attackable rewards
					RewardInfoHolder reward2 = rewards.get(pl);
					
					// If the L2PcInstance is in the L2Attackable rewards add its damages to party damages
					if (reward2 != null)
					{
						if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, pl, true))
						{
							partyDmg += reward2.getDmg(); // Add L2PcInstance damages to party damages
							rewardedMembers.add(pl);
							
							if (pl.getLevel() > partyLvl)
							{
								if (attackerParty.isInCommandChannel())
								{
									partyLvl = attackerParty.getCommandChannel().getLevel();
								}
								else
								{
									partyLvl = pl.getLevel();
								}
							}
						}
						
						rewards.remove(pl); // Remove the L2PcInstance from the L2Attackable rewards
					}
					else
					{
						// Add L2PcInstance of the party (that have attacked or not) to members that can be rewarded
						// and in range of the monster.
						if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, pl, true))
						{
							rewardedMembers.add(pl);
							
							if (pl.getLevel() > partyLvl)
							{
								if (attackerParty.isInCommandChannel())
								{
									partyLvl = attackerParty.getCommandChannel().getLevel();
								}
								else
								{
									partyLvl = pl.getLevel();
								}
							}
						}
					}
					
					final L2PlayableInstance summon = pl.getPet();
					if (summon != null && summon instanceof L2PetInstance)
					{
						reward2 = rewards.get(summon);
						
						if (reward2 != null) // Pets are only added if they have done damage
						{
							if (Util.checkIfInRange(Config.ALT_PARTY_RANGE, npc, summon, true))
							{
								partyDmg += reward2.getDmg(); // Add summon damages to party damages
								rewardedMembers.add(summon);
								
								if (summon.getLevel() > partyLvl)
								{
									partyLvl = summon.getLevel();
								}
							}
							
							rewards.remove(summon); // Remove the summon from the L2Attackable rewards
						}
					}
				}
				
				// If the party didn't killed this L2Attackable alone
				if (partyDmg < npc.getMaxHp())
				{
					partyMul = partyDmg / (float) npc.getMaxHp();
				}
				
				// Avoid "over damage"
				if (partyDmg > npc.getMaxHp())
				{
					partyDmg = npc.getMaxHp();
				}
				
				// Calculate the level difference between Party and L2Attackable
				int levelDiff = partyLvl - npc.getLevel();
				
				// Calculate Exp and SP rewards
				int[] tmp = calculateExpAndSp(npc, levelDiff, partyDmg);
				int exp = tmp[0];
				int sp = tmp[1];
				
				exp *= partyMul;
				sp *= partyMul;
				
				// Check for an over-hit enabled strike
				// (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
				if (attacker instanceof L2PcInstance)
				{
					final L2PcInstance player = attacker;
					
					if (npc.isOverhit() && attacker == npc.getOverhitAttacker())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.OVER_HIT));
						exp += npc.calculateOverhitExp(exp);
					}
				}
				
				// Distribute Experience and SP rewards to L2PcInstance Party members in the known area of the last attacker
				if (partyDmg > 0)
				{
					attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl);
				}
			}
		}
	}
	
	/**
	 * Calculate the Experience and SP to distribute to attacker (Player, L2SummonInstance or L2Party) of the Attackable.
	 * @param  diff   The difference of level between attacker (Player, L2SummonInstance or L2Party) and the Attackable
	 * @param  damage The damages given by the attacker (Player, L2SummonInstance or L2Party)
	 * @return        an array consisting of xp and sp values.
	 */
	private static int[] calculateExpAndSp(L2Attackable npc, int diff, int damage)
	{
		double xp;
		double sp;
		
		if (diff < -5)
		{
			diff = -5; // makes possible to use ALT_GAME_EXPONENT configuration
		}
		
		xp = (double) npc.getExpReward() * damage / npc.getMaxHp();
		
		if (Config.ALT_GAME_EXPONENT_XP != 0)
		{
			xp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_XP);
		}
		
		sp = (double) npc.getSpReward() * damage / npc.getMaxHp();
		
		if (Config.ALT_GAME_EXPONENT_SP != 0)
		{
			sp *= Math.pow(2., -diff / Config.ALT_GAME_EXPONENT_SP);
		}
		
		if (Config.ALT_GAME_EXPONENT_XP == 0 && Config.ALT_GAME_EXPONENT_SP == 0)
		{
			if (diff > 5) // formula revised May 07
			{
				final double pow = Math.pow((double) 5 / 6, diff - 5);
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
		
		final int[] tmp =
		{
			(int) xp,
			(int) sp
		};
		
		return tmp;
	}
}
