package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;

/**
 * Format: (ch) dd [dddc] d - manor id d - size [ d - crop id d - sales d - price c - reward type ]
 * @author l3x
 */
public class RequestSetCrop extends L2GameClientPacket
{
	private int size;
	private int manorId;
	private int[] items; // size*4
	
	@Override
	protected void readImpl()
	{
		manorId = readD();
		size = readD();
		
		if (size * 13 > buf.remaining() || size > 500 || size < 1)
		{
			size = 0;
			return;
		}
		
		items = new int[size * 4];
		
		for (int i = 0; i < size; i++)
		{
			final int itemId = readD();
			items[i * 4 + 0] = itemId;
			final int sales = readD();
			items[i * 4 + 1] = sales;
			final int price = readD();
			items[i * 4 + 2] = price;
			final int type = readC();
			items[i * 4 + 3] = type;
		}
	}
	
	@Override
	protected void runImpl()
	{
		if (size < 1)
		{
			return;
		}
		
		final List<CropProcure> crops = new ArrayList<>();
		for (int i = 0; i < size; i++)
		{
			final int id = items[i * 4 + 0];
			final int sales = items[i * 4 + 1];
			final int price = items[i * 4 + 2];
			final int type = items[i * 4 + 3];
			
			if (id > 0)
			{
				final CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
				crops.add(s);
			}
		}
		
		CastleManager.getInstance().getCastleById(manorId).setCropProcure(crops, CastleManorManager.PERIOD_NEXT);
		
		if (Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			CastleManager.getInstance().getCastleById(manorId).saveCropData(CastleManorManager.PERIOD_NEXT);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0B RequestSetCrop";
	}
}
