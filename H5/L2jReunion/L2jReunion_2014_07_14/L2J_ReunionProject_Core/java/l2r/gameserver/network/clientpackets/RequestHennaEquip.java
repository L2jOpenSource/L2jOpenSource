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

import l2r.Config;
import l2r.gameserver.datatables.xml.HennaData;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.L2Henna;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.InventoryUpdate;
import l2r.gameserver.util.Util;

/**
 * @author Zoey76
 */
public final class RequestHennaEquip extends L2GameClientPacket
{
	private static final String _C__6F_REQUESTHENNAEQUIP = "[C] 6F RequestHennaEquip";
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaEquip"))
		{
			return;
		}
		
		if (activeChar.getHennaEmptySlots() == 0)
		{
			activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
			sendActionFailed();
			return;
		}
		
		final L2Henna henna = HennaData.getInstance().getHenna(_symbolId);
		if (henna == null)
		{
			_log.warn(getClass().getSimpleName() + ": Invalid Henna Id: " + _symbolId + " from player " + activeChar);
			sendActionFailed();
			return;
		}
		
		final long _count = activeChar.getInventory().getInventoryItemCount(henna.getDyeItemId(), -1);
		if (henna.isAllowedClass(activeChar.getClassId()) && (_count >= henna.getWearCount()) && (activeChar.getAdena() >= henna.getWearFee()) && activeChar.addHenna(henna))
		{
			activeChar.destroyItemByItemId("Henna", henna.getDyeItemId(), henna.getWearCount(), activeChar, true);
			activeChar.getInventory().reduceAdena("Henna", henna.getWearFee(), activeChar, activeChar.getLastFolkNPC());
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
			activeChar.sendPacket(iu);
			activeChar.sendPacket(SystemMessageId.SYMBOL_ADDED);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
			if (!activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !henna.isAllowedClass(activeChar.getClassId()))
			{
				Util.handleIllegalPlayerAction(activeChar, "Exploit attempt: Character " + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna.", Config.DEFAULT_PUNISH);
			}
			sendActionFailed();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__6F_REQUESTHENNAEQUIP;
	}
}
