package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2HennaInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class HennaEquipList extends L2GameServerPacket
{
	private final L2PcInstance player;
	private final L2HennaInstance[] hennaEquipList;
	
	public HennaEquipList(final L2PcInstance player, final L2HennaInstance[] hennaEquipList)
	{
		this.player = player;
		this.hennaEquipList = hennaEquipList;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe2);
		writeD(player.getAdena()); // activeChar current amount of aden
		writeD(3); // available equip slot
		// writeD(10); // total amount of symbol available which depends on difference classes
		writeD(hennaEquipList.length);
		
		for (final L2HennaInstance element : hennaEquipList)
		{
			/*
			 * Player must have at least one dye in inventory to be able to see the henna that can be applied with it.
			 */
			if (player.getInventory().getItemByItemId(element.getItemIdDye()) != null)
			{
				writeD(element.getSymbolId()); // symbolid
				writeD(element.getItemIdDye()); // itemid of dye
				writeD(element.getAmountDyeRequire()); // amount of dye require
				writeD(element.getPrice()); // amount of aden require
				writeD(1); // meet the requirement or not
			}
			else
			{
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] E2 HennaEquipList";
	}
	
}
