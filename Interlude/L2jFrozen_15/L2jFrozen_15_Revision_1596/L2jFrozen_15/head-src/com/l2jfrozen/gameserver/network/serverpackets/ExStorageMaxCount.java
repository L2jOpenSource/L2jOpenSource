package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: (ch)ddddddd d: Number of Inventory Slots d: Number of Warehouse Slots d: Number of Freight Slots (unconfirmed) (200 for a low level dwarf) d: Private Sell Store Slots (unconfirmed) (4 for a low level dwarf) d: Private Buy Store Slots (unconfirmed) (5 for a low level dwarf) d: Dwarven
 * Recipe Book Slots d: Normal Recipe Book Slots
 * @author -Wooden- format from KenM
 */
public class ExStorageMaxCount extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int inventory;
	private final int warehouse;
	private final int freight;
	private final int privateSell;
	private final int privateBuy;
	private final int recipeD;
	private final int recipe;
	
	public ExStorageMaxCount(final L2PcInstance character)
	{
		activeChar = character;
		inventory = activeChar.getInventoryLimit();
		warehouse = activeChar.GetWareHouseLimit();
		privateSell = activeChar.GetPrivateSellStoreLimit();
		privateBuy = activeChar.GetPrivateBuyStoreLimit();
		freight = activeChar.GetFreightLimit();
		recipeD = activeChar.GetDwarfRecipeLimit();
		recipe = activeChar.GetCommonRecipeLimit();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x2e);
		
		writeD(inventory);
		writeD(warehouse);
		writeD(freight);
		writeD(privateSell);
		writeD(privateBuy);
		writeD(recipeD);
		writeD(recipe);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:2E ExStorageMaxCount";
	}
	
}
