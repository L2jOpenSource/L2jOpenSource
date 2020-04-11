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
package l2r.gameserver.network.clientpackets;

import java.util.ArrayList;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.datatables.xml.MultiSell;
import l2r.gameserver.model.Elementals;
import l2r.gameserver.model.L2Augmentation;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.PcInventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.multisell.Entry;
import l2r.gameserver.model.multisell.Ingredient;
import l2r.gameserver.model.multisell.PreparedListContainer;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ItemList;
import l2r.gameserver.network.serverpackets.StatusUpdate;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * The Class MultiSellChoose.
 */
public class MultiSellChoose extends L2GameClientPacket
{
	private static final String _C__B0_MULTISELLCHOOSE = "[C] B0 MultiSellChoose";
	
	private int _listId;
	private int _entryId;
	private long _amount;
	@SuppressWarnings("unused")
	private int _unk1;
	@SuppressWarnings("unused")
	private int _unk2;
	@SuppressWarnings("unused")
	private int _unk3;
	@SuppressWarnings("unused")
	private int _unk7;
	@SuppressWarnings("unused")
	private int _unk4;
	@SuppressWarnings("unused")
	private int _unk5;
	@SuppressWarnings("unused")
	private int _unk6;
	@SuppressWarnings("unused")
	private int _unk8;
	@SuppressWarnings("unused")
	private int _unk9;
	@SuppressWarnings("unused")
	private int _unk10;
	@SuppressWarnings("unused")
	private int _unk11;
	
	@Override
	protected void readImpl()
	{
		_listId = readD();
		_entryId = readD();
		_amount = readQ();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readH(); // elemental attributes
		_unk5 = readH(); // elemental attributes
		_unk6 = readH(); // elemental attributes
		_unk7 = readH(); // elemental attributes
		_unk8 = readH(); // elemental attributes
		_unk9 = readH(); // elemental attributes
		_unk10 = readH(); // elemental attributes
		_unk11 = readH(); // elemental attributes
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
			player.setMultiSell(null);
			return;
		}
		
		if ((_amount < 1) || (_amount > 5000))
		{
			player.setMultiSell(null);
			return;
		}
		
		PreparedListContainer list = player.getMultiSell();
		if ((list == null) || (list.getListId() != _listId))
		{
			player.setMultiSell(null);
			return;
		}
		
		L2Npc target = player.getLastFolkNPC();
		if (!player.isGM() && (((target == null) || !list.checkNpcObjectId(target.getObjectId()) || !target.canInteract(player)) && !player.isAioMultisell()))
		{
			player.setMultiSell(null);
			return;
		}
		
		for (Entry entry : list.getEntries())
		{
			if (entry.getEntryId() == _entryId)
			{
				if (!entry.isStackable() && (_amount > 1))
				{
					_log.error("Character: " + player.getName() + " is trying to set amount > 1 on non-stackable multisell, id:" + _listId + ":" + _entryId);
					player.setMultiSell(null);
					return;
				}
				
				final PcInventory inv = player.getInventory();
				
				int slots = 0;
				int weight = 0;
				for (Ingredient e : entry.getProducts())
				{
					if (e.getId() < 0)
					{
						continue;
					}
					
					if (!e.isStackable())
					{
						slots += e.getItemCount() * _amount;
					}
					else if (player.getInventory().getItemByItemId(e.getId()) == null)
					{
						slots++;
					}
					weight += e.getItemCount() * _amount * e.getWeight();
				}
				
				if (!inv.validateWeight(weight))
				{
					player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
					return;
				}
				
				if (!inv.validateCapacity(slots))
				{
					player.sendPacket(SystemMessageId.SLOTS_FULL);
					return;
				}
				
				ArrayList<Ingredient> ingredientsList = new ArrayList<>(entry.getIngredients().size());
				// Generate a list of distinct ingredients and counts in order to check if the correct item-counts
				// are possessed by the player
				boolean newIng;
				for (Ingredient e : entry.getIngredients())
				{
					newIng = true;
					// at this point, the template has already been modified so that enchantments are properly included
					// whenever they need to be applied. Uniqueness of items is thus judged by item id AND enchantment level
					for (int i = ingredientsList.size(); --i >= 0;)
					{
						Ingredient ex = ingredientsList.get(i);
						// if the item was already added in the list, merely increment the count
						// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
						if ((ex.getId() == e.getId()) && (ex.getEnchantLevel() == e.getEnchantLevel()))
						{
							if ((ex.getItemCount() + e.getItemCount()) > Config.MAX_ADENA)
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
								return;
							}
							// two same ingredients, merge into one and replace old
							final Ingredient ing = ex.getCopy();
							ing.setItemCount(ex.getItemCount() + e.getItemCount());
							ingredientsList.set(i, ing);
							newIng = false;
							break;
						}
					}
					if (newIng)
					{
						// if it's a new ingredient, just store its info directly (item id, count, enchantment)
						ingredientsList.add(e);
					}
				}
				
				// now check if the player has sufficient items in the inventory to cover the ingredients' expences
				for (Ingredient e : ingredientsList)
				{
					if ((e.getItemCount() * _amount) > Config.MAX_ADENA)
					{
						player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
						return;
					}
					if (e.getId() < 0)
					{
						if (!MultiSell.checkSpecialIngredient(e.getId(), e.getItemCount() * _amount, player))
						{
							return;
						}
					}
					else
					{
						// if this is not a list that maintains enchantment, check the count of all items that have the given id.
						// otherwise, check only the count of items with exactly the needed enchantment level
						final long required = ((Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient()) ? (e.getItemCount() * _amount) : e.getItemCount());
						if (inv.getInventoryItemCount(e.getId(), list.getMaintainEnchantment() ? e.getEnchantLevel() : -1, false) < required)
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
							sm.addItemName(e.getTemplate());
							sm.addLong(required);
							player.sendPacket(sm);
							return;
						}
					}
				}
				
				FastList<L2Augmentation> augmentation = FastList.newInstance();
				Elementals[] elemental = null;
				/** All ok, remove items and add final product */
				
				try
				{
					for (Ingredient e : entry.getIngredients())
					{
						if (e.getId() < 0)
						{
							if (!MultiSell.getSpecialIngredient(e.getId(), e.getItemCount() * _amount, player))
							{
								return;
							}
						}
						else
						{
							L2ItemInstance itemToTake = inv.getItemByItemId(e.getId()); // initialize and initial guess for the item to take.
							if (itemToTake == null)
							{ // this is a cheat, transaction will be aborted and if any items already taken will not be returned back to inventory!
								_log.error("Character: " + player.getName() + " is trying to cheat in multisell, id:" + _listId + ":" + _entryId);
								player.setMultiSell(null);
								return;
							}
							
							// if (itemToTake.isEquipped())
							// {
							// this is a cheat, transaction will be aborted and if any items already taken will not be returned back to inventory!
							// _log.error("Character: " + player.getName() + " is trying to cheat in multisell, exchanging equipped item, merchatnt id:" + merchant.getId());
							// player.setMultiSell(null);
							// return;
							// }
							
							if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.getMaintainIngredient())
							{
								// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
								if (itemToTake.isStackable())
								{
									if (!player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true))
									{
										player.setMultiSell(null);
										return;
									}
								}
								else
								{
									// for non-stackable items, one of two scenaria are possible:
									// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
									// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level
									
									// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
									if (list.getMaintainEnchantment())
									{
										// loop through this list and remove (one by one) each item until the required amount is taken.
										L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getId(), e.getEnchantLevel(), false);
										for (int i = 0; i < (e.getItemCount() * _amount); i++)
										{
											if (inventoryContents[i].isAugmented())
											{
												augmentation.add(inventoryContents[i].getAugmentation());
											}
											if (inventoryContents[i].getElementals() != null)
											{
												elemental = inventoryContents[i].getElementals();
											}
											if (!player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true))
											{
												player.setMultiSell(null);
												return;
											}
										}
									}
									else
									// b) enchantment is not maintained. Get the instances with the LOWEST enchantment level
									{
										// NOTE: There are 2 ways to achieve the above goal.
										// 1) Get all items that have the correct itemId, loop through them until the lowest enchantment
										// level is found. Repeat all this for the next item until proper count of items is reached.
										// 2) Get all items that have the correct itemId, sort them once based on enchantment level,
										// and get the range of items that is necessary.
										// Method 1 is faster for a small number of items to be exchanged.
										// Method 2 is faster for large amounts.
										//
										// EXPLANATION:
										// Worst case scenario for algorithm 1 will make it run in a number of cycles given by:
										// m*(2n-m+1)/2 where m is the number of items to be exchanged and n is the total
										// number of inventory items that have a matching id.
										// With algorithm 2 (sort), sorting takes n*log(n) time and the choice is done in a single cycle
										// for case b (just grab the m first items) or in linear time for case a (find the beginning of items
										// with correct enchantment, index x, and take all items from x to x+m).
										// Basically, whenever m > log(n) we have: m*(2n-m+1)/2 = (2nm-m*m+m)/2 >
										// (2nlogn-logn*logn+logn)/2 = nlog(n) - log(n*n) + log(n) = nlog(n) + log(n/n*n) =
										// nlog(n) + log(1/n) = nlog(n) - log(n) = (n-1)log(n)
										// So for m < log(n) then m*(2n-m+1)/2 > (n-1)log(n) and m*(2n-m+1)/2 > nlog(n)
										//
										// IDEALLY:
										// In order to best optimize the performance, choose which algorithm to run, based on whether 2^m > n
										// if ( (2<<(e.getItemCount()// _amount)) < inventoryContents.length )
										// // do Algorithm 1, no sorting
										// else
										// // do Algorithm 2, sorting
										//
										// CURRENT IMPLEMENTATION:
										// In general, it is going to be very rare for a person to do a massive exchange of non-stackable items
										// For this reason, we assume that algorithm 1 will always suffice and we keep things simple.
										// If, in the future, it becomes necessary that we optimize, the above discussion should make it clear
										// what optimization exactly is necessary (based on the comments under "IDEALLY").
										//
										
										// choice 1. Small number of items exchanged. No sorting.
										for (int i = 1; i <= (e.getItemCount() * _amount); i++)
										{
											L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getId(), false);
											
											itemToTake = inventoryContents[0];
											// get item with the LOWEST enchantment level from the inventory...
											// +0 is lowest by default...
											if (itemToTake.getEnchantLevel() > 0)
											{
												for (L2ItemInstance item : inventoryContents)
												{
													if (item.getEnchantLevel() < itemToTake.getEnchantLevel())
													{
														itemToTake = item;
														// nothing will have enchantment less than 0. If a zero-enchanted
														// item is found, just take it
														if (itemToTake.getEnchantLevel() == 0)
														{
															break;
														}
													}
												}
											}
											if (!player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true))
											{
												player.setMultiSell(null);
												return;
											}
										}
									}
								}
							}
						}
					}
					
					// Generate the appropriate items
					for (Ingredient e : entry.getProducts())
					{
						if (e.getId() < 0)
						{
							MultiSell.addSpecialProduct(e.getId(), e.getItemCount() * _amount, player);
						}
						else
						{
							if (e.isStackable())
							{
								inv.addItem("Multisell", e.getId(), e.getItemCount() * _amount, player, player.getTarget());
							}
							else
							{
								L2ItemInstance product = null;
								for (int i = 0; i < (e.getItemCount() * _amount); i++)
								{
									product = inv.addItem("Multisell", e.getId(), 1, player, player.getTarget());
									if ((product != null) && list.getMaintainEnchantment())
									{
										if (i < augmentation.size())
										{
											product.setAugmentation(new L2Augmentation(augmentation.get(i).getAugmentationId()));
										}
										if (elemental != null)
										{
											for (Elementals elm : elemental)
											{
												product.setElementAttr(elm.getElement(), elm.getValue());
											}
										}
										product.setEnchantLevel(e.getEnchantLevel());
										product.updateDatabase();
									}
								}
							}
							// msg part
							SystemMessage sm;
							
							if ((e.getItemCount() * _amount) > 1)
							{
								sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
								sm.addItemName(e.getId());
								sm.addLong(e.getItemCount() * _amount);
								player.sendPacket(sm);
								sm = null;
							}
							else
							{
								if (list.getMaintainEnchantment() && (e.getEnchantLevel() > 0))
								{
									sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2);
									sm.addLong(e.getEnchantLevel());
									sm.addItemName(e.getId());
								}
								else
								{
									sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
									sm.addItemName(e.getId());
								}
								player.sendPacket(sm);
								sm = null;
							}
						}
					}
					player.sendPacket(new ItemList(player, false));
					
					StatusUpdate su = new StatusUpdate(player);
					su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
					player.sendPacket(su);
					su = null;
				}
				finally
				{
					FastList.recycle(augmentation);
				}
				
				// finally, give the tax to the castle...
				if (entry.getTaxAmount() > 0)
				{
					if (target != null)
					{
						target.getCastle().addToTreasury(entry.getTaxAmount() * _amount);
					}
				}
				
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__B0_MULTISELLCHOOSE;
	}
}
