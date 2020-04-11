package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * format(packet 0xFE) ch dd [dddc] c - id h - sub id d - crop id d - size [ d - manor name d - buy residual d - buy price c - reward type ]
 * @author l3x
 */
public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int spCropId;
	private final Map<Integer, CropProcure> castleCrops;
	
	public ExShowProcureCropDetail(final int cropId)
	{
		spCropId = cropId;
		castleCrops = new HashMap<>();
		
		for (final Castle c : CastleManager.getInstance().getCastles())
		{
			final CropProcure cropItem = c.getCrop(spCropId, CastleManorManager.PERIOD_CURRENT);
			if (cropItem != null && cropItem.getAmount() > 0)
			{
				castleCrops.put(c.getCastleId(), cropItem);
			}
		}
	}
	
	@Override
	public void runImpl()
	{
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x22);
		
		writeD(spCropId); // crop id
		writeD(castleCrops.size()); // size
		
		for (final int manorId : castleCrops.keySet())
		{
			final CropProcure crop = castleCrops.get(manorId);
			writeD(manorId); // manor name
			writeD(crop.getAmount()); // buy residual
			writeD(crop.getPrice()); // buy price
			writeC(crop.getReward()); // reward type
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:22 ExShowProcureCropDetail";
	}
	
}
