package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class SellListProcure extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private final int money;
	private final Map<L2ItemInstance, Integer> sellList = new HashMap<>();
	private List<CropProcure> procureList = new ArrayList<>();
	private final int castle;
	
	public SellListProcure(final L2PcInstance player, final int castleId)
	{
		money = player.getAdena();
		activeChar = player;
		castle = castleId;
		procureList = CastleManager.getInstance().getCastleById(castle).getCropProcure(0);
		for (final CropProcure c : procureList)
		{
			final L2ItemInstance item = activeChar.getInventory().getItemByItemId(c.getId());
			if (item != null && c.getAmount() > 0)
			{
				sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE9);
		writeD(money); // money
		writeD(0x00); // lease ?
		writeH(sellList.size()); // list size
		
		for (final L2ItemInstance item : sellList.keySet())
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(sellList.get(item)); // count
			writeH(item.getItem().getType2());
			writeH(0); // unknown
			writeD(0); // price, u shouldnt get any adena for crops, only raw materials
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] E9 SellListProcure";
	}
}
