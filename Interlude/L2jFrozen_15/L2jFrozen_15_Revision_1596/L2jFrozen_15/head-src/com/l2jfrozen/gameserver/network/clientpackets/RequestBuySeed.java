package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.SeedProduction;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ManorManagerInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.util.Util;

/**
 * Format: cdd[dd] c // id (0xC4) d // manor id d // seeds to buy [ d // seed id d // count ]
 * @author l3x
 */
public class RequestBuySeed extends L2GameClientPacket
{
	private int count;
	private int manorId;
	private int[] items; // size count * 2
	
	@Override
	protected void readImpl()
	{
		manorId = readD();
		count = readD();
		
		if (count > 500 || count * 8 < buf.remaining() || count < 1) // check values
		{
			count = 0;
			return;
		}
		
		items = new int[count * 2];
		
		for (int i = 0; i < count; i++)
		{
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
		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;
		
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getManor().tryPerformAction("BuySeed"))
		{
			return;
		}
		
		if (count < 1)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Object target = player.getTarget();
		
		if (!(target instanceof L2ManorManagerInstance))
		{
			target = player.getLastFolkNPC();
		}
		
		if (!(target instanceof L2ManorManagerInstance))
		{
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(manorId);
		
		for (int i = 0; i < count; i++)
		{
			final int seedId = items[i * 2 + 0];
			final int count = items[i * 2 + 1];
			int price = 0;
			int residual = 0;
			
			final SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			price = seed.getPrice();
			residual = seed.getCanProduce();
			
			if (price <= 0)
			{
				return;
			}
			
			if (residual < count)
			{
				return;
			}
			
			totalPrice += count * price;
			
			final L2Item template = ItemTable.getInstance().getTemplate(seedId);
			totalWeight += count * template.getWeight();
			if (!template.isStackable())
			{
				slots += count;
			}
			else if (player.getInventory().getItemByItemId(seedId) == null)
			{
				slots++;
			}
		}
		
		if (totalPrice > Integer.MAX_VALUE)
		{
			Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + Integer.MAX_VALUE + " adena worth of goods.", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!player.getInventory().validateWeight(totalWeight))
		{
			sendPacket(new SystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
			return;
		}
		
		if (!player.getInventory().validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.SLOTS_FULL));
			return;
		}
		
		// Charge buyer
		if (totalPrice < 0 || !player.reduceAdena("Buy", (int) totalPrice, target, false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}
		
		// Adding to treasury for Manor Castle
		castle.addToTreasuryNoTax((int) totalPrice);
		
		// Proceed the purchase
		final InventoryUpdate playerIU = new InventoryUpdate();
		for (int i = 0; i < count; i++)
		{
			final int seedId = items[i * 2 + 0];
			int count = items[i * 2 + 1];
			
			if (count < 0)
			{
				count = 0;
			}
			
			// Update Castle Seeds Amount
			final SeedProduction seed = castle.getSeed(seedId, CastleManorManager.PERIOD_CURRENT);
			seed.setCanProduce(seed.getCanProduce() - count);
			if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				CastleManager.getInstance().getCastleById(manorId).updateSeed(seed.getId(), seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			}
			
			// Add item to Inventory and adjust update packet
			final L2ItemInstance item = player.getInventory().addItem("Buy", seedId, count, player, target);
			
			if (item.getCount() > count)
			{
				playerIU.addModifiedItem(item);
			}
			else
			{
				playerIU.addNewItem(item);
			}
			
			// Send Char Buy Messages
			SystemMessage sm = null;
			sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(seedId);
			sm.addNumber(count);
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
		return "[C] C4 RequestBuySeed";
	}
}
