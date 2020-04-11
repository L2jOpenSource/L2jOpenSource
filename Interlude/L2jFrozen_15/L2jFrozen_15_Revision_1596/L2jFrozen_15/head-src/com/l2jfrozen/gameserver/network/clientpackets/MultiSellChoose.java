package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2Augmentation;
import com.l2jfrozen.gameserver.model.PcInventory;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.multisell.L2Multisell;
import com.l2jfrozen.gameserver.model.multisell.MultiSellEntry;
import com.l2jfrozen.gameserver.model.multisell.MultiSellIngredient;
import com.l2jfrozen.gameserver.model.multisell.MultiSellListContainer;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Armor;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;

/**
 * @author programmos
 */
public class MultiSellChoose extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(MultiSellChoose.class);
	private int listId;
	private int entryId;
	private int amount;
	private int enchantment;
	private int transactionTax; // local handling of taxation
	
	@Override
	protected void readImpl()
	{
		listId = readD();
		entryId = readD();
		amount = readD();
		// enchantment = readH(); // Commented this line because it did NOT work!
		enchantment = entryId % 100000;
		entryId = entryId / 100000;
		transactionTax = 0; // Initialize tax amount to 0...
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getMultiSell().tryPerformAction("multisell choose"))
		{
			player.setMultiSellId(-1);
			return;
		}
		
		if (amount < 1 || amount > 5000)
		{
			player.setMultiSellId(-1);
			return;
		}
		
		final L2NpcInstance merchant = player.getTarget() instanceof L2NpcInstance ? (L2NpcInstance) player.getTarget() : null;
		
		// Possible fix to Multisell Radius
		if (merchant == null || !player.isInsideRadius(merchant, L2NpcInstance.INTERACTION_DISTANCE, false, false))
		{
			player.setMultiSellId(-1);
			return;
		}
		
		final MultiSellListContainer list = L2Multisell.getInstance().getList(listId);
		
		final int selectedList = player.getMultiSellId();
		if (list == null || list.getListId() != listId || selectedList != listId)
		{
			player.setMultiSellId(-1);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.setMultiSellId(-1);
			return;
		}
		
		for (final MultiSellEntry entry : list.getEntries())
		{
			if (entry.getEntryId() == entryId)
			{
				doExchange(player, entry, list.getApplyTaxes(), list.getMaintainEnchantment(), enchantment);
				
				// dnt change multisell on exchange to avoid new window open need
				// player.setMultiSellId(-1);
				return;
			}
		}
	}
	
	private void doExchange(final L2PcInstance player, final MultiSellEntry templateEntry, final boolean applyTaxes, final boolean maintainEnchantment, final int enchantment)
	{
		final PcInventory inv = player.getInventory();
		boolean maintainItemFound = false;
		
		// given the template entry and information about maintaining enchantment and applying taxes re-create the instance of
		// the entry that will be used for this exchange i.e. change the enchantment level of select ingredient/products and adena amount appropriately.
		final L2NpcInstance merchant = player.getTarget() instanceof L2NpcInstance ? (L2NpcInstance) player.getTarget() : null;
		
		// if(merchant == null) TODO: Check this
		// return;
		
		final MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);
		
		// Generate a list of distinct ingredients and counts in order to check if the correct item-counts
		// are possessed by the player
		List<MultiSellIngredient> ingredientsList = new ArrayList<>();
		boolean newIng = true;
		
		for (final MultiSellIngredient e : entry.getIngredients())
		{
			newIng = true;
			
			// at this point, the template has already been modified so that enchantments are properly included
			// whenever they need to be applied. Uniqueness of items is thus judged by item id AND enchantment level
			for (final MultiSellIngredient ex : ingredientsList)
			{
				// if the item was already added in the list, merely increment the count
				// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
				if (ex.getItemId() == e.getItemId() && ex.getEnchantmentLevel() == e.getEnchantmentLevel())
				{
					if ((double) ex.getItemCount() + e.getItemCount() > Integer.MAX_VALUE)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
						ingredientsList.clear();
						ingredientsList = null;
						return;
					}
					ex.setItemCount(ex.getItemCount() + e.getItemCount());
					newIng = false;
				}
			}
			if (newIng)
			{
				// If there is a maintainIngredient, then we do not need to check the enchantment parameter as the enchant level will be checked elsewhere
				if (maintainEnchantment)
				{
					maintainItemFound = true;
				}
				
				// if it's a new ingredient, just store its info directly (item id, count, enchantment)
				ingredientsList.add(new MultiSellIngredient(e));
			}
		}
		
		// If there is no maintainIngredient, then we must make sure that the enchantment is not kept from the client packet, as it may have been forged
		if (!maintainItemFound)
		{
			for (final MultiSellIngredient product : entry.getProducts())
			{
				product.setEnchantmentLevel(0);
			}
		}
		
		// now check if the player has sufficient items in the inventory to cover the ingredients' expences
		for (final MultiSellIngredient e : ingredientsList)
		{
			if ((double) e.getItemCount() * amount > Integer.MAX_VALUE)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
				ingredientsList.clear();
				ingredientsList = null;
				return;
			}
			
			if (e.getItemId() != 65336 && e.getItemId() != 65436)
			{
				// if this is not a list that maintains enchantment, check the count of all items that have the given id.
				// otherwise, check only the count of items with exactly the needed enchantment level
				if (inv.getInventoryItemCount(e.getItemId(), maintainEnchantment ? e.getEnchantmentLevel() : -1) < (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient() ? e.getItemCount() * amount : e.getItemCount()))
				{
					player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					ingredientsList.clear();
					ingredientsList = null;
					return;
				}
			}
			else
			{
				if (e.getItemId() == 65336)
				{
					if (player.getClan() == null)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER));
						return;
					}
					
					if (!player.isClanLeader())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED));
						return;
					}
					
					if (player.getClan().getReputationScore() < e.getItemCount() * amount)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW));
						return;
					}
				}
				if (e.getItemId() == 65436 && e.getItemCount() * amount > player.getPcBangScore())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
					return;
				}
			}
		}
		
		ingredientsList.clear();
		ingredientsList = null;
		final List<L2Augmentation> augmentation = new ArrayList<>();
		/** All ok, remove items and add final product */
		
		for (final MultiSellIngredient e : entry.getIngredients())
		{
			if (e.getItemId() != 65336 && e.getItemId() != 65436)
			{
				for (final MultiSellIngredient a : entry.getProducts())
				{
					if (player.getInventoryLimit() < inv.getSize() + amount && !ItemTable.getInstance().createDummyItem(a.getItemId()).isStackable())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
						return;
					}
					if (player.getInventoryLimit() < inv.getSize() && ItemTable.getInstance().createDummyItem(a.getItemId()).isStackable())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
						return;
					}
				}
				L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId()); // initialize and initial guess for the item to take.
				
				// this is a cheat, transaction will be aborted and if any items already tanken will not be returned back to inventory!
				if (itemToTake == null)
				{
					LOGGER.warn("Character: " + player.getName() + " is trying to cheat in multisell, merchatnt id:" + (merchant != null ? merchant.getNpcId() : 0));
					return;
				}
				if (itemToTake.fireEvent("MULTISELL", (Object[]) null) != null)
				{
					return;
				}
				
				if (itemToTake.isWear())
				{
					LOGGER.warn("Character: " + player.getName() + " is trying to cheat in multisell with weared item");
					return;
				}
				
				if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMantainIngredient())
				{
					// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
					if (itemToTake.isStackable())
					{
						if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * amount), player.getTarget(), true))
						{
							return;
						}
					}
					else
					{
						// for non-stackable items, one of two scenaria are possible:
						// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
						// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level
						
						// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
						if (maintainEnchantment)
						{
							// loop through this list and remove (one by one) each item until the required amount is taken.
							final L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), e.getEnchantmentLevel());
							for (int i = 0; i < e.getItemCount() * amount; i++)
							{
								if (inventoryContents[i].isAugmented())
								{
									augmentation.add(inventoryContents[i].getAugmentation());
								}
								
								if (inventoryContents[i].isEquipped())
								{
									if (inventoryContents[i].isAugmented())
									{
										inventoryContents[i].getAugmentation().removeBoni(player);
									}
								}
								
								if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
								{
									return;
								}
							}
						}
						else
						// b) enchantment is not maintained. Get the instances with the LOWEST enchantment level
						{
							/*
							 * NOTE: There are 2 ways to achieve the above goal. 1) Get all items that have the correct itemId, loop through them until the lowest enchantment level is found. Repeat all this for the next item until proper count of items is reached. 2) Get all items that have the correct itemId, sort them once
							 * based on enchantment level, and get the range of items that is necessary. Method 1 is faster for a small number of items to be exchanged. Method 2 is faster for large amounts. EXPLANATION: Worst case scenario for algorithm 1 will make it run in a number of cycles given by: m*(2n-m+1)/2 where m is
							 * the number of items to be exchanged and n is the total number of inventory items that have a matching id. With algorithm 2 (sort), sorting takes n*LOGGER(n) time and the choice is done in a single cycle for case b (just grab the m first items) or in linear time for case a (find the beginning of
							 * items with correct enchantment, index x, and take all items from x to x+m). Basically, whenever m > LOGGER(n) we have: m*(2n-m+1)/2 = (2nm-m*m+m)/2 > (2nlogn-logn*logn+logn)/2 = nlog(n) - LOGGER(n*n) + LOGGER(n) = nlog(n) + LOGGER(n/n*n) = nlog(n) + LOGGER(1/n) = nlog(n) - LOGGER(n) =
							 * (n-1)LOGGER(n) So for m < LOGGER(n) then m*(2n-m+1)/2 > (n-1)LOGGER(n) and m*(2n-m+1)/2 > nlog(n) IDEALLY: In order to best optimize the performance, choose which algorithm to run, based on whether 2^m > n if ( (2<<(e.getItemCount() * amount)) < inventoryContents.length ) // do Algorithm 1, no
							 * sorting else // do Algorithm 2, sorting CURRENT IMPLEMENTATION: In general, it is going to be very rare for a person to do a massive exchange of non-stackable items For this reason, we assume that algorithm 1 will always suffice and we keep things simple. If, in the future, it becomes necessary
							 * that we optimize, the above discussion should make it clear what optimization exactly is necessary (based on the comments under "IDEALLY").
							 */
							
							// choice 1. Small number of items exchanged. No sorting.
							for (int i = 1; i <= e.getItemCount() * amount; i++)
							{
								final L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());
								
								itemToTake = inventoryContents[0];
								// get item with the LOWEST enchantment level from the inventory...
								// +0 is lowest by default...
								if (itemToTake.getEnchantLevel() > 0)
								{
									for (final L2ItemInstance inventoryContent : inventoryContents)
									{
										if (inventoryContent.getEnchantLevel() < itemToTake.getEnchantLevel())
										{
											itemToTake = inventoryContent;
											// nothing will have enchantment less than 0. If a zero-enchanted
											// item is found, just take it
											if (itemToTake.getEnchantLevel() == 0)
											{
												break;
											}
										}
									}
								}
								
								if (itemToTake.isEquipped())
								{
									if (itemToTake.isAugmented())
									{
										itemToTake.getAugmentation().removeBoni(player);
									}
								}
								
								if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
								{
									return;
								}
								
							}
						}
					}
				}
			}
			else
			{
				if (e.getItemId() == 65336)
				{
					final int repCost = player.getClan().getReputationScore() - e.getItemCount();
					player.getClan().setReputationScore(repCost, true);
					player.sendPacket(new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(e.getItemCount()));
					player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				}
				else
				{
					player.reducePcBangScore(e.getItemCount() * amount);
					player.sendPacket(new SystemMessage(SystemMessageId.USING_S1_PCPOINT).addNumber(e.getItemCount()));
				}
			}
		}
		// Generate the appropriate items
		for (final MultiSellIngredient e : entry.getProducts())
		{
			if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
			{
				inv.addItem("Multisell[" + listId + "]", e.getItemId(), (e.getItemCount() * amount), player, player.getTarget());
			}
			else
			{
				L2ItemInstance product = null;
				for (int i = 0; i < e.getItemCount() * amount; i++)
				{
					product = inv.addItem("Multisell[" + listId + "]", e.getItemId(), 1, player, player.getTarget());
					if (maintainEnchantment && (product != null))
					{
						if (i < augmentation.size())
						{
							product.setAugmentation(new L2Augmentation(product, augmentation.get(i).getAugmentationId(), augmentation.get(i).getSkill(), true));
						}
						product.setEnchantLevel(e.getEnchantmentLevel());
					}
				}
			}
			// Msg part
			SystemMessage sm;
			
			if (e.getItemCount() * amount > 1)
			{
				sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(e.getItemId());
				sm.addNumber(e.getItemCount() * amount);
				player.sendPacket(sm);
			}
			else
			{
				if (maintainEnchantment && e.getEnchantmentLevel() > 0)
				{
					sm = new SystemMessage(SystemMessageId.ACQUIRED);
					sm.addNumber(e.getEnchantmentLevel());
					sm.addItemName(e.getItemId());
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.EARNED_ITEM);
					sm.addItemName(e.getItemId());
				}
				player.sendPacket(sm);
			}
		}
		player.sendPacket(new ItemList(player, false));
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
		player.broadcastUserInfo();
		
		// Finally, give the tax to the castle...
		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
		{
			merchant.getCastle().addToTreasury(transactionTax * amount);
		}
	}
	
	// Regarding taxation, the following appears to be the case:
	// a) The count of aa remains unchanged (taxes do not affect aa directly).
	// b) 5/6 of the amount of aa is taxed by the normal tax rate.
	// c) the resulting taxes are added as normal adena value.
	// d) normal adena are taxed fully.
	// e) Items other than adena and ancient adena are not taxed even when the list is taxable.
	// example: If the template has an item worth 120aa, and the tax is 10%,
	// then from 120aa, take 5/6 so that is 100aa, apply the 10% tax in adena (10a)
	// so the final price will be 120aa and 10a!
	private MultiSellEntry prepareEntry(final L2NpcInstance merchant, final MultiSellEntry templateEntry, final boolean applyTaxes, final boolean maintainEnchantment, int enchantLevel)
	{
		final MultiSellEntry newEntry = new MultiSellEntry();
		newEntry.setEntryId(templateEntry.getEntryId());
		int totalAdenaCount = 0;
		boolean hasIngredient = false;
		
		for (final MultiSellIngredient ing : templateEntry.getIngredients())
		{
			// Load the ingredient from the template
			final MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
			
			if (newIngredient.getItemId() == 57 && newIngredient.isTaxIngredient())
			{
				double taxRate = 0.0;
				
				if (applyTaxes)
				{
					if (merchant != null && merchant.getIsInTown())
					{
						taxRate = merchant.getCastle().getTaxRate();
					}
				}
				
				transactionTax = (int) Math.round(newIngredient.getItemCount() * taxRate);
				totalAdenaCount += transactionTax;
				continue; // Do not yet add this adena amount to the list as non-taxIngredient adena might be entered later (order not guaranteed)
			}
			else if (ing.getItemId() == 57) // && !ing.isTaxIngredient()
			{
				totalAdenaCount += newIngredient.getItemCount();
				continue; // Do not yet add this adena amount to the list as taxIngredient adena might be entered later (order not guaranteed)
			}
			// If it is an armor/weapon, modify the enchantment level appropriately, if necessary
			else if (maintainEnchantment)
			{
				final L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				if (tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
				{
					newIngredient.setEnchantmentLevel(enchantLevel);
					hasIngredient = true;
				}
			}
			
			// finally, add this ingredient to the entry
			newEntry.addIngredient(newIngredient);
		}
		// Next add the adena amount, if any
		if (totalAdenaCount > 0)
		{
			newEntry.addIngredient(new MultiSellIngredient(57, totalAdenaCount, false, false));
		}
		
		// Now modify the enchantment level of products, if necessary
		for (final MultiSellIngredient ing : templateEntry.getProducts())
		{
			// Load the ingredient from the template
			final MultiSellIngredient newIngredient = new MultiSellIngredient(ing);
			
			if (maintainEnchantment && hasIngredient)
			{
				// If it is an armor/weapon, modify the enchantment level appropriately
				// (note, if maintain enchantment is "false" this modification will result to a +0)
				final L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
				
				if (tempItem instanceof L2Armor || tempItem instanceof L2Weapon)
				{
					if (enchantLevel == 0 && maintainEnchantment)
					{
						enchantLevel = ing.getEnchantmentLevel();
					}
					newIngredient.setEnchantmentLevel(enchantLevel);
				}
			}
			newEntry.addProduct(newIngredient);
		}
		return newEntry;
	}
	
	@Override
	public String getType()
	{
		return "[C] A7 MultiSellChoose";
	}
}