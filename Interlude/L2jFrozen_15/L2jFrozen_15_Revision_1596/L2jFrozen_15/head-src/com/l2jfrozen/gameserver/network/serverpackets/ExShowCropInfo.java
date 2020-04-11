package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.L2Manor;

/**
 * Format: ch cddd[ddddcdcdcd] c - id (0xFE) h - sub id (0x1D) c d - manor id d d - size [ d - crop id d - residual buy d - start buy d - buy price c - reward type d - seed level c - reward 1 items d - reward 1 item id c - reward 2 items d - reward 2 item id ]
 * @author l3x
 */

public class ExShowCropInfo extends L2GameServerPacket
{
	private List<CropProcure> cropProcures;
	private final int manorId;
	
	public ExShowCropInfo(final int manorId, final List<CropProcure> crops)
	{
		this.manorId = manorId;
		cropProcures = crops;
		if (cropProcures == null)
		{
			cropProcures = new ArrayList<>();
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x1D); // SubId
		writeC(0);
		writeD(manorId); // Manor ID
		writeD(0);
		writeD(cropProcures.size());
		for (final CropProcure crop : cropProcures)
		{
			writeD(crop.getId()); // Crop id
			writeD(crop.getAmount()); // Buy residual
			writeD(crop.getStartAmount()); // Buy
			writeD(crop.getPrice()); // Buy price
			writeC(crop.getReward()); // Reward
			writeD(L2Manor.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeC(1); // rewrad 1 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 1)); // Rewrad 1 Type Item Id
			writeC(1); // rewrad 2 Type
			writeD(L2Manor.getInstance().getRewardItem(crop.getId(), 2)); // Rewrad 2 Type Item Id
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:1D ExShowCropInfo";
	}
	
}
