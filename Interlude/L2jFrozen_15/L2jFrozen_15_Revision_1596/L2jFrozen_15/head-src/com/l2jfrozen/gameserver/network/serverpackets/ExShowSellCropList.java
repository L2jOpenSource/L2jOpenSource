package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.L2Manor;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * format(packet 0xFE) ch dd [ddddcdcdddc] c - id h - sub id d - manor id d - size [ d - Object id d - crop id d - seed level c d - reward 1 id c d - reward 2 id d - manor d - buy residual d - buy price d - reward ]
 * @author l3x
 */

public class ExShowSellCropList extends L2GameServerPacket
{
	private int manorId = 1;
	private final Map<Integer, L2ItemInstance> cropsItems;
	private final Map<Integer, CropProcure> castleCrops;
	
	public ExShowSellCropList(final L2PcInstance player, final int manorId, final List<CropProcure> crops)
	{
		this.manorId = manorId;
		castleCrops = new HashMap<>();
		cropsItems = new HashMap<>();
		
		List<Integer> allCrops = L2Manor.getInstance().getAllCrops();
		for (final int cropId : allCrops)
		{
			final L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if (item != null)
			{
				cropsItems.put(cropId, item);
			}
		}
		
		for (final CropProcure crop : crops)
		{
			if (cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
			{
				castleCrops.put(crop.getId(), crop);
			}
		}
	}
	
	@Override
	public void runImpl()
	{
		// no long running
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);
		
		writeD(manorId); // manor id
		writeD(cropsItems.size()); // size
		
		for (final L2ItemInstance item : cropsItems.values())
		{
			writeD(item.getObjectId()); // Object id
			writeD(item.getItemId()); // crop id
			writeD(L2Manor.getInstance().getSeedLevelByCrop(item.getItemId())); // seed level
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 1)); // reward 1 id
			writeC(1);
			writeD(L2Manor.getInstance().getRewardItem(item.getItemId(), 2)); // reward 2 id
			
			if (castleCrops.containsKey(item.getItemId()))
			{
				final CropProcure crop = castleCrops.get(item.getItemId());
				writeD(manorId); // manor
				writeD(crop.getAmount()); // buy residual
				writeD(crop.getPrice()); // buy price
				writeC(crop.getReward()); // reward
			}
			else
			{
				writeD(0xFFFFFFFF); // manor
				writeD(0); // buy residual
				writeD(0); // buy price
				writeC(0); // reward
			}
			writeD(item.getCount()); // my crops
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:21 ExShowSellCropList";
	}
}
