package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.L2Manor;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.util.Util;

@SuppressWarnings("unused")
public class RequestBuyProcure extends L2GameClientPacket
{
	private int listId;
	private int count;
	private int[] items;
	private List<CropProcure> procureList = new ArrayList<>();
	
	@Override
	protected void readImpl()
	{
		listId = readD();
		count = readD();
		
		if (count > 500) // protect server
		{
			count = 0;
			return;
		}
		
		if (count < 0) // protect server
		{
			count = 0;
			return;
		}
		
		items = new int[count * 2];
		for (int i = 0; i < count; i++)
		{
			readD();
			final int itemId = readD();
			items[i * 2 + 0] = itemId;
			final long cnt = readD();
			
			if (cnt > Integer.MAX_VALUE || cnt < 1)
			{
				count = 0;
				items = null;
				return;
			}
			
			items[i * 2 + 1] = (int) cnt;
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
		
		if (!getClient().getFloodProtectors().getManor().tryPerformAction("BuyProcure"))
		{
			return;
		}
		
		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0)
		{
			return;
		}
		
		if (count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check for buylist validity and calculates summary values
		int slots = 0;
		int weight = 0;
		
		if (!(player.getTarget() instanceof L2ManorManagerInstance))
		{
			return;
		}
		
		final L2ManorManagerInstance manor = (L2ManorManagerInstance) player.getTarget();
		for (int i = 0; i < count; i++)
		{
			final int itemId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			if (count > Integer.MAX_VALUE)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " items at the same time.", Config.DEFAULT_PUNISH);
				final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}
			
			final L2Item template = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward()));
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
		procureList = manor.getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT);
		
		for (int i = 0; i < count; i++)
		{
			final int itemId = items[i * 2 + 0];
			int count = items[i * 2 + 1];
			
			if (count < 0)
			{
				count = 0;
			}
			
			final int rewradItemId = L2Manor.getInstance().getRewardItem(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getReward());
			
			int rewradItemCount = 1; // L2Manor.getInstance().getRewardAmount(itemId, manor.getCastle().getCropReward(itemId));
			
			rewradItemCount = count / rewradItemCount;
			
			// Add item to Inventory and adjust update packet
			final L2ItemInstance item = player.getInventory().addItem("Manor", rewradItemId, rewradItemCount, player, manor);
			final L2ItemInstance iteme = player.getInventory().destroyItemByItemId("Manor", itemId, count, player, manor);
			
			if (item == null || iteme == null)
			{
				continue;
			}
			
			playerIU.addRemovedItem(iteme);
			if (item.getCount() > rewradItemCount)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}
			
			// Send Char Buy Messages
			SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(rewradItemId);
			sm.addNumber(rewradItemCount);
			player.sendPacket(sm);
			sm = null;
			
			// manor.getCastle().setCropAmount(itemId, manor.getCastle().getCrop(itemId, CastleManorManager.PERIOD_CURRENT).getAmount() - count);
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
		return "[C] C3 RequestBuyProcure";
	}
}
