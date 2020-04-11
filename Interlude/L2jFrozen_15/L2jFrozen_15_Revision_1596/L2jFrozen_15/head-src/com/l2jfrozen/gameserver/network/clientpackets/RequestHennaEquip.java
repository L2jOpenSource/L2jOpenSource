package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.csv.HennaTable;
import com.l2jfrozen.gameserver.datatables.sql.HennaTreeTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Henna;
import com.l2jfrozen.gameserver.util.Util;

public final class RequestHennaEquip extends L2GameClientPacket
{
	private int symbolId;
	
	/**
	 * packet type id 0xbb format: cd
	 */
	@Override
	protected void readImpl()
	{
		symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaEquip"))
		{
			return;
		}
		
		final L2Henna template = HennaTable.getInstance().getTemplate(symbolId);
		
		if (template == null)
		{
			return;
		}
		
		final L2HennaInstance temp = new L2HennaInstance(template);
		int count = 0;
		
		/*
		 * Prevents henna drawing exploit: 1) talk to L2SymbolMakerInstance 2) RequestHennaList 3) Don't close the window and go to a GrandMaster and change your subclass 4) Get SymbolMaker range again and press draw You could draw any kind of henna just having the required subclass...
		 */
		boolean cheater = true;
		
		for (final L2HennaInstance h : HennaTreeTable.getInstance().getAvailableHenna(activeChar.getClassId()))
		{
			if (h.getSymbolId() == temp.getSymbolId())
			{
				cheater = false;
				break;
			}
		}
		
		if (activeChar.getInventory() != null && activeChar.getInventory().getItemByItemId(temp.getItemIdDye()) != null)
		{
			count = activeChar.getInventory().getItemByItemId(temp.getItemIdDye()).getCount();
		}
		
		if (!cheater && count >= temp.getAmountDyeRequire() && activeChar.getAdena() >= temp.getPrice() && activeChar.addHenna(temp))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
			sm.addNumber(temp.getItemIdDye());
			activeChar.sendPacket(sm);
			sm = null;
			activeChar.sendPacket(new SystemMessage(SystemMessageId.SYMBOL_ADDED));
			
			// HennaInfo hi = new HennaInfo(temp,activeChar);
			// activeChar.sendPacket(hi);
			
			activeChar.getInventory().reduceAdena("Henna", temp.getPrice(), activeChar, activeChar.getLastFolkNPC());
			final L2ItemInstance dyeToUpdate = activeChar.getInventory().destroyItemByItemId("Henna", temp.getItemIdDye(), temp.getAmountDyeRequire(), activeChar, activeChar.getLastFolkNPC());
			
			// update inventory
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
			iu.addModifiedItem(dyeToUpdate);
			activeChar.sendPacket(iu);
			
			final ItemList il = new ItemList(getClient().getActiveChar(), true);
			sendPacket(il);
			
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_DRAW_SYMBOL));
			
			if (!activeChar.isGM() && cheater)
			{
				Util.handleIllegalPlayerAction(activeChar, "Exploit attempt: Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] bc RequestHennaEquip";
	}
}
