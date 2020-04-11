package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.SeedProduction;

/**
 * Format: (ch) dd [ddd] d - manor id d - size [ d - seed id d - sales d - price ]
 * @author l3x
 */
public class RequestSetSeed extends L2GameClientPacket
{
	private int size;
	private int manorId;
	private int[] items; // size*3
	
	@Override
	protected void readImpl()
	{
		manorId = readD();
		size = readD();
		
		if (size * 12 > buf.remaining() || size > 500 || size < 1)
		{
			size = 0;
			return;
		}
		
		items = new int[size * 3];
		
		for (int i = 0; i < size; i++)
		{
			final int itemId = readD();
			items[i * 3 + 0] = itemId;
			final int sales = readD();
			items[i * 3 + 1] = sales;
			final int price = readD();
			items[i * 3 + 2] = price;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (size < 1)
		{
			return;
		}
		
		final List<SeedProduction> seeds = new ArrayList<>();
		
		for (int i = 0; i < size; i++)
		{
			final int id = items[i * 3 + 0];
			final int sales = items[i * 3 + 1];
			final int price = items[i * 3 + 2];
			if (id > 0)
			{
				final SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
				seeds.add(s);
			}
		}
		
		CastleManager.getInstance().getCastleById(manorId).setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(manorId).saveSeedData(CastleManorManager.PERIOD_NEXT);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0A RequestSetSeed";
	}
}
