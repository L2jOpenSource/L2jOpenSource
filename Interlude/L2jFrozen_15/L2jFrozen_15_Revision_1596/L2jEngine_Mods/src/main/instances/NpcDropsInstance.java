package main.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Attackable.RewardItem;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2DropCategory;
import com.l2jfrozen.gameserver.model.L2DropData;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MinionInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

import main.data.memory.ObjectData;
import main.enums.ItemDropType;
import main.holders.DropBonusHolder;
import main.holders.RewardHolder;
import main.holders.objects.PlayerHolder;
import main.util.UtilInventory;

/**
 * @author fissban
 */
public class NpcDropsInstance
{
	private final Map<ItemDropType, DropBonusHolder> settings = new HashMap<>();
	{
		settings.put(ItemDropType.NORMAL, new DropBonusHolder());
		settings.put(ItemDropType.SPOIL, new DropBonusHolder());
		settings.put(ItemDropType.SEED, new DropBonusHolder());
		settings.put(ItemDropType.HERB, new DropBonusHolder());
	}
	
	private static final List<Integer> HERBS_SPECIAL_DROP = new ArrayList<>();
	static
	{
		HERBS_SPECIAL_DROP.add(8612); // Herb of Warrior
		HERBS_SPECIAL_DROP.add(8613); // Herb of Mystic
		HERBS_SPECIAL_DROP.add(8614); // Herb of Recovery
	}
	
	private static final List<Integer> HERBS_COMMON_DROP = new ArrayList<>();
	static
	{
		HERBS_COMMON_DROP.add(8607); // Herb of Magic
		HERBS_COMMON_DROP.add(8609); // Herb of Casting Speed
		HERBS_COMMON_DROP.add(8611); // Herb of Speed
		HERBS_COMMON_DROP.add(8606); // Herb of Power
		HERBS_COMMON_DROP.add(8608); // Herb of Atk. Spd.
		HERBS_COMMON_DROP.add(8610); // Herb of Critical Attack
	}
	
	private static final List<Integer> HERBS_SUPERIOR_DROP = new ArrayList<>();
	static
	{
		HERBS_SUPERIOR_DROP.add(8605); // Superior Herb of Mana
		HERBS_SUPERIOR_DROP.add(8602); // Superior Herb of Life
	}
	
	private static final List<Integer> HERBS_HP_MP_DROP = new ArrayList<>();
	static
	{
		HERBS_HP_MP_DROP.add(8600); // Herb of Life
		HERBS_HP_MP_DROP.add(8603); // Herb of Mana
	}
	
	public NpcDropsInstance()
	{
		//
	}
	
	public void increaseDrop(ItemDropType type, double chance, double amount)
	{
		settings.get(type).increaseAmountBonus(amount);
		settings.get(type).increaseChanceBonus(chance);
	}
	
	public boolean hasSettings()
	{
		for (DropBonusHolder holder : settings.values())
		{
			if (holder.getAmountBonus() > 1.0 || holder.getChanceBonus() > 1.0)
			{
				return true;
			}
		}
		return false;
	}
	
	public void init(L2Attackable npc, L2Character mainDamageDealer)
	{
		// -------------------------------------------------------------------------------------------------------------------
		
		if (mainDamageDealer == null)
		{
			return;
		}
		
		// Don't drop anything if the last attacker or owner isn't Player
		L2PcInstance player = mainDamageDealer.getActingPlayer();
		if (player == null)
		{
			return;
		}
		
		// level modifier in %'s (will be subtracted from drop chance)
		int levelModifier = calculateLevelModifierForDrop(npc, player);
		
		// Check the drop of a cursed weapon
		if (levelModifier == 0 && player.getLevel() > 20)
		{
			CursedWeaponsManager.getInstance().checkDrop(npc, player);
		}
		
		// now throw all categorized drops and handle spoil.
		for (L2DropCategory cat : npc.getTemplate().getDropData())
		{
			List<RewardHolder> items = new ArrayList<>();
			if (cat.isSweep())
			{
				if (npc.isSpoil())
				{
					for (L2DropData drop : cat.getAllDrops())
					{
						RewardHolder item = calculateRewardItem(npc, player, drop, levelModifier, true);
						if (item == null)
						{
							continue;
						}
						
						items.add(item);
						
					}
					
					npc.setSweepItems(items.toArray(new RewardItem[items.size()]));
				}
			}
			else
			{
				RewardHolder item = null;
				if (npc.isSeeded())
				{
					L2DropData drop = cat.dropSeedAllowedDropsOnly();
					if (drop == null)
					{
						continue;
					}
					
					item = calculateRewardItem(npc, player, drop, levelModifier, false);
				}
				else
				{
					item = calculateCategorizedRewardItem(npc, player, cat, levelModifier);
				}
				
				if (item != null)
				{
					// Check if the autoLoot mode is active
					if (((npc instanceof L2RaidBossInstance || npc instanceof L2GrandBossInstance) && Config.AUTO_LOOT_BOSS) || Config.AUTO_LOOT)
					{
						// Give this or these Item(s) to the Player that has killed the Attackable
						UtilInventory.giveItems(ObjectData.get(PlayerHolder.class, player), item);
					}
					else
					{
						UtilInventory.dropItem(player, npc, item.getRewardId(), item.getRewardCount()); // drop the item on the ground
					}
					
					// Broadcast message if RaidBoss was defeated
					if (npc instanceof L2RaidBossInstance)
					{
						npc.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2).addNpcName(npc.getNpcId()).addItemName(item.getRewardId()).addNumber(item.getRewardCount()));
					}
				}
			}
		}
		
		// Herbs.
		int random = 0; // note *10
		
		for (int herb : HERBS_SPECIAL_DROP)
		{
			random = Rnd.get(1000); // note *10
			if (random < Config.RATE_DROP_SPECIAL_HERBS)
			{
				UtilInventory.dropItem(player, npc, herb, 1);
				break;
			}
		}
		
		for (int herb : HERBS_COMMON_DROP)
		{
			random = Rnd.get(100);
			if (random < Config.RATE_DROP_COMMON_HERBS)
			{
				UtilInventory.dropItem(player, npc, herb, 1);
			}
		}
		
		for (int herb : HERBS_SUPERIOR_DROP)
		{
			random = Rnd.get(1000); // note *10
			if (random < Config.RATE_DROP_SUPERIOR_HERBS)
			{
				UtilInventory.dropItem(player, npc, herb, 1);
			}
		}
		
		for (int herb : HERBS_HP_MP_DROP)
		{
			random = Rnd.get(100);
			if (random < Config.RATE_DROP_MP_HP_HERBS)
			{
				UtilInventory.dropItem(player, npc, herb, 1);
			}
		}
	}
	
	// ----------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Calculates quantity of items for specific drop according to current situation.
	 * @param  drop          The L2DropData count is being calculated for
	 * @param  lastAttacker  The Player that has killed the Attackable
	 * @param  levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @param  isSweep       if true, use spoil drop chance.
	 * @return               the ItemHolder.
	 */
	private RewardHolder calculateRewardItem(L2Attackable npc, L2PcInstance lastAttacker, L2DropData drop, int levelModifier, boolean isSweep)
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
					deepBlueDrop *= npc.isRaid() ? 1 : (int) Config.RATE_DROP_ITEMS;
					if (deepBlueDrop == 0)
					{
						deepBlueDrop = 1;
					}
				}
			}
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			dropChance = (drop.getChance() - drop.getChance() * levelModifier / 100) / deepBlueDrop;
		}
		
		// Applies Drop rates
		if (drop.getItemId() == 57)
		{
			if (npc instanceof L2RaidBossInstance)
			{
				dropChance *= Config.ADENA_RAID;
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				dropChance *= Config.ADENA_BOSS;
			}
			else if (npc instanceof L2MinionInstance)
			{
				dropChance *= Config.ADENA_MINON;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ADENA;
			}
			
		}
		else if (isSweep)
		{
			if (npc instanceof L2RaidBossInstance)
			{
				dropChance *= Config.SPOIL_RAID;
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				dropChance *= Config.SPOIL_BOSS;
			}
			else if (npc instanceof L2MinionInstance)
			{
				dropChance *= Config.SPOIL_MINON;
			}
			else
			{
				dropChance *= Config.RATE_DROP_SPOIL;
				
				if (lastAttacker.isVIP())
				{
					dropChance *= Config.VIP_SPOIL_RATE;
				}
			}
		}
		else
		{
			if (npc instanceof L2RaidBossInstance)
			{
				dropChance *= Config.ITEMS_RAID;
			}
			else if (npc instanceof L2GrandBossInstance)
			{
				dropChance *= Config.ITEMS_BOSS;
			}
			else if (npc instanceof L2MinionInstance)
			{
				dropChance *= Config.ITEMS_MINON;
			}
			else
			{
				dropChance *= Config.RATE_DROP_ITEMS;
			}
		}
		
		// Custom for all mods
		dropChance *= settings.get(isSweep ? ItemDropType.SPOIL : ItemDropType.SEED).getChanceBonus();
		
		if (dropChance < 1)
		{
			dropChance = 1;
		}
		
		// Get min and max Item quantity that can be dropped in one time
		final int minCount = drop.getMinDrop();
		final int maxCount = drop.getMaxDrop();
		
		// Get the item quantity dropped
		int itemCount = 0;
		
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
		
		if (drop.getItemId() >= 6360 && drop.getItemId() <= 6362)
		{
			itemCount *= Config.RATE_DROP_SEAL_STONES;
		}
		
		// Custom for all mods
		itemCount *= settings.get(isSweep ? ItemDropType.SPOIL : ItemDropType.SEED).getAmountBonus();
		
		if (itemCount > 0)
		{
			return new RewardHolder(drop.getItemId(), itemCount);
		}
		
		return null;
	}
	
	/**
	 * Calculates quantity of items for specific drop CATEGORY according to current situation <br>
	 * Only a max of ONE item from a category is allowed to be dropped.
	 * @param  lastAttacker  The Player that has killed the Attackable
	 * @param  categoryDrops The category to make checks on.
	 * @param  levelModifier level modifier in %'s (will be subtracted from drop chance)
	 * @return               the ItemHolder.
	 */
	private RewardHolder calculateCategorizedRewardItem(L2NpcInstance npc, L2PcInstance lastAttacker, L2DropCategory categoryDrops, int levelModifier)
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
		
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int deepBlueDrop = levelModifier > 0 ? 3 : 1;
			
			// Check if we should apply our maths so deep blue mobs will not drop that easy
			categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop;
		}
		
		// Applies Drop rates
		if (npc instanceof L2RaidBossInstance)
		{
			categoryDropChance *= Config.ITEMS_RAID;
		}
		else if (npc instanceof L2GrandBossInstance)
		{
			categoryDropChance *= Config.ITEMS_BOSS;
		}
		else if (npc instanceof L2MinionInstance)
		{
			categoryDropChance *= Config.ITEMS_MINON;
		}
		else
		{
			categoryDropChance *= Config.RATE_DROP_ITEMS;
		}
		
		// Custom for all mods
		categoryDropChance *= settings.get(ItemDropType.NORMAL).getChanceBonus();
		
		// Set our limits for chance of drop
		if (categoryDropChance < 1)
		{
			categoryDropChance = 1;
		}
		
		// Check if an Item from this category must be dropped
		if (Rnd.get(L2DropData.MAX_CHANCE) < categoryDropChance)
		{
			L2DropData drop = categoryDrops.dropOne(npc.isRaid());
			if (drop == null)
			{
				return null;
			}
			
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
			int dropChance = 0;
			
			switch (drop.getItemId())
			{
				case 6662:
				{ // core ring
					if (Config.CORE_RING_CHANCE > 0)
					{
						dropChance = (10000 * Config.CORE_RING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
				}
					break;
				case 6661:
				{ // orfen earring
					if (Config.ORFEN_EARRING_CHANCE > 0)
					{
						dropChance = (10000 * Config.ORFEN_EARRING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
				}
					break;
				case 6659:
				{ // zaken earring
					if (Config.ZAKEN_EARRING_CHANCE > 0)
					{
						dropChance = (10000 * Config.ZAKEN_EARRING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
				}
					break;
				case 6660:
				{ // aq ring
					if (Config.QA_RING_CHANCE > 0)
					{
						dropChance = (10000 * Config.QA_RING_CHANCE);
					}
					else
					{
						dropChance = drop.getChance();
					}
				}
					break;
				default:
				{
					dropChance = drop.getChance();
				}
			}
			
			if (drop.getItemId() == 57)
			{
				if (npc instanceof L2RaidBossInstance)
				{
					dropChance *= Config.ADENA_RAID;
				}
				else if (npc instanceof L2GrandBossInstance)
				{
					dropChance *= Config.ADENA_BOSS;
				}
				else if (npc instanceof L2MinionInstance)
				{
					dropChance *= Config.ADENA_MINON;
				}
				else
				{
					dropChance *= Config.RATE_DROP_ADENA;
				}
			}
			else
			{
				if (npc instanceof L2RaidBossInstance)
				{
					dropChance *= Config.ITEMS_RAID;
				}
				else if (npc instanceof L2GrandBossInstance)
				{
					dropChance *= Config.ITEMS_BOSS;
				}
				else if (npc instanceof L2MinionInstance)
				{
					dropChance *= Config.ITEMS_MINON;
				}
				else
				{
					dropChance *= Config.RATE_DROP_ITEMS;
				}
			}
			
			// Custom for all mods
			dropChance *= settings.get(ItemDropType.NORMAL).getChanceBonus();
			
			if (dropChance < L2DropData.MAX_CHANCE)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			// Get min and max Item quantity that can be dropped in one time
			final int min = drop.getMinDrop();
			final int max = drop.getMaxDrop();
			
			// Get the item quantity dropped
			int itemCount = 0;
			
			if (!Config.MULTIPLY_QUANTITY_BY_CHANCE && dropChance > L2DropData.MAX_CHANCE && drop.getItemId() != 57)
			{
				dropChance = L2DropData.MAX_CHANCE;
			}
			
			// Count and chance adjustment for high rate servers
			if (dropChance > L2DropData.MAX_CHANCE && !Config.PRECISE_DROP_CALCULATION)
			{
				final int multiplier = dropChance / L2DropData.MAX_CHANCE;
				
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
			
			// Custom for all mods
			itemCount *= settings.get(ItemDropType.NORMAL).getAmountBonus();
			
			if (itemCount > 0)
			{
				return new RewardHolder(drop.getItemId(), itemCount);
			}
		}
		return null;
	}
	
	/**
	 * @param  lastAttacker The Player that has killed the Attackable
	 * @return              the level modifier for drop
	 */
	private int calculateLevelModifierForDrop(L2NpcInstance npc, L2PcInstance lastAttacker)
	{
		if (Config.DEEPBLUE_DROP_RULES)
		{
			int highestLevel = lastAttacker.getLevel();
			
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			for (L2Character atkChar : ((L2Attackable) npc).getAttackByList())
			{
				if (atkChar.getLevel() > highestLevel)
				{
					highestLevel = atkChar.getLevel();
				}
			}
			
			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if (highestLevel - 9 >= npc.getLevel())
			{
				return (highestLevel - (npc.getLevel() + 8)) * 9;
			}
		}
		return 0;
	}
}
