package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.L2Manor;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.util.Util;

/**
 * Format: (ch) d [dddd] d: size [ d obj id d item id d manor id d count ]
 * @author l3x
 */
public class RequestProcureCropList extends L2GameClientPacket
{
	private int size;
	
	private int[] items; // count*4
	
	@Override
	protected void readImpl()
	{
		size = readD();
		if (size * 16 > buf.remaining() || size > 500 || size < 1)
		{
			size = 0;
			return;
		}
		items = new int[size * 4];
		for (int i = 0; i < size; i++)
		{
			final int objId = readD();
			items[i * 4 + 0] = objId;
			final int itemId = readD();
			items[i * 4 + 1] = itemId;
			final int manorId = readD();
			items[i * 4 + 2] = manorId;
			long count = readD();
			
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			
			items[i * 4 + 3] = (int) count;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		L2Object target = player.getTarget();
		
		if (!(target instanceof L2ManorManagerInstance))
		{
			target = player.getLastFolkNPC();
		}
		
		if (!player.isGM() && (target == null || !(target instanceof L2ManorManagerInstance) || !player.isInsideRadius(target, L2NpcInstance.INTERACTION_DISTANCE, false, false)))
		{
			return;
		}
		
		if (size < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ManorManagerInstance manorManager = (L2ManorManagerInstance) target;
		
		final int currentManorId = manorManager.getCastle().getCastleId();
		
		// Calculate summary values
		int slots = 0;
		int weight = 0;
		
		for (int i = 0; i < size; i++)
		{
			final int itemId = items[i * 4 + 1];
			final int manorId = items[i * 4 + 2];
			final int count = items[i * 4 + 3];
			
			if (itemId == 0 || manorId == 0 || count == 0)
			{
				continue;
			}
			
			if (count < 1)
			{
				continue;
			}
			
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}
			
			try
			{
				final CropProcure crop = CastleManager.getInstance().getCastleById(manorId).getCrop(itemId, CastleManorManager.PERIOD_CURRENT);
				final int rewardItemId = L2Manor.getInstance().getRewardItem(itemId, crop.getReward());
				final L2Item template = ItemTable.getInstance().getTemplate(rewardItemId);
				weight += count * template.getWeight();
				
				if (!template.isStackable())
				{
					slots += count;
				}
				else if (player.getInventory().getItemByItemId(itemId) == null)
				{
					slots++;
				}
			}
			catch (final NullPointerException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				continue;
			}
		}
		
		if (!player.getInventory().validateWeight(weight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Proceed the purchase
		final InventoryUpdate playerIU = new InventoryUpdate();
		
		for (int i = 0; i < size; i++)
		{
			final int objId = items[i * 4 + 0];
			final int cropId = items[i * 4 + 1];
			final int manorId = items[i * 4 + 2];
			final int count = items[i * 4 + 3];
			
			if (objId == 0 || cropId == 0 || manorId == 0 || count == 0)
			{
				continue;
			}
			
			if (count < 1)
			{
				continue;
			}
			
			CropProcure crop = null;
			
			try
			{
				crop = CastleManager.getInstance().getCastleById(manorId).getCrop(cropId, CastleManorManager.PERIOD_CURRENT);
			}
			catch (final NullPointerException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				continue;
			}
			if (crop == null || crop.getId() == 0 || crop.getPrice() == 0)
			{
				continue;
			}
			
			int fee = 0; // fee for selling to other manors
			
			final int rewardItem = L2Manor.getInstance().getRewardItem(cropId, crop.getReward());
			
			if (count > crop.getAmount())
			{
				continue;
			}
			
			final int sellPrice = count * L2Manor.getInstance().getCropBasicPrice(cropId);
			final int rewardPrice = ItemTable.getInstance().getTemplate(rewardItem).getReferencePrice();
			
			if (rewardPrice == 0)
			{
				continue;
			}
			
			final int rewardItemCount = sellPrice / rewardPrice;
			if (rewardItemCount < 1)
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addNumber(count);
				player.sendPacket(sm);
				continue;
			}
			
			if (manorId != currentManorId)
			{
				fee = sellPrice * 5 / 100; // 5% fee for selling to other manor
			}
			
			if (player.getInventory().getAdena() < fee)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.FAILED_IN_TRADING_S2_OF_CROP_S1);
				sm.addItemName(cropId);
				sm.addNumber(count);
				player.sendPacket(sm);
				sm = new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				player.sendPacket(sm);
				continue;
			}
			
			// Add item to Inventory and adjust update packet
			L2ItemInstance itemDel = null;
			L2ItemInstance itemAdd = null;
			if (player.getInventory().getItemByObjectId(objId) != null)
			{
				// check if player have correct items count
				final L2ItemInstance item = player.getInventory().getItemByObjectId(objId);
				if (item.getCount() < count)
				{
					continue;
				}
				
				itemDel = player.getInventory().destroyItem("Manor", objId, count, player, manorManager);
				if (itemDel == null)
				{
					continue;
				}
				
				if (fee > 0)
				{
					player.getInventory().reduceAdena("Manor", fee, player, manorManager);
				}
				
				crop.setAmount(crop.getAmount() - count);
				
				if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					CastleManager.getInstance().getCastleById(manorId).updateCrop(crop.getId(), crop.getAmount(), CastleManorManager.PERIOD_CURRENT);
				}
				
				itemAdd = player.getInventory().addItem("Manor", rewardItem, rewardItemCount, player, manorManager);
			}
			else
			{
				continue;
			}
			
			if (itemAdd == null)
			{
				continue;
			}
			
			playerIU.addRemovedItem(itemDel);
			if (itemAdd.getCount() > rewardItemCount)
			{
				playerIU.addModifiedItem(itemAdd);
			}
			else
			{
				playerIU.addNewItem(itemAdd);
			}
			
			// Send System Messages
			SystemMessage sm = new SystemMessage(SystemMessageId.TRADED_S2_OF_CROP_S1);
			sm.addItemName(cropId);
			sm.addNumber(count);
			player.sendPacket(sm);
			
			if (fee > 0)
			{
				sm = new SystemMessage(SystemMessageId.S1_ADENA_HAS_BEEN_WITHDRAWN_TO_PAY_FOR_PURCHASING_FEES);
				sm.addNumber(fee);
				player.sendPacket(sm);
			}
			
			sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
			sm.addItemName(cropId);
			sm.addNumber(count);
			player.sendPacket(sm);
			
			if (fee > 0)
			{
				sm = new SystemMessage(SystemMessageId.DISSAPEARED_ADENA);
				sm.addNumber(fee);
				player.sendPacket(sm);
			}
			
			sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(rewardItem);
			sm.addNumber(rewardItemCount);
			player.sendPacket(sm);
		}
		
		// Send update packets
		player.sendPacket(playerIU);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
		
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:09 RequestProcureCropList";
	}
}
