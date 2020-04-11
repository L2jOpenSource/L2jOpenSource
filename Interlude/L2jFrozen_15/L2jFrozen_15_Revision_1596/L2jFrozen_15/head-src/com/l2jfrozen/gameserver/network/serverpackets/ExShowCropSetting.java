package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager;
import com.l2jfrozen.gameserver.managers.CastleManorManager.CropProcure;
import com.l2jfrozen.gameserver.model.L2Manor;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * format(packet 0xFE) ch dd [ddcdcdddddddcddc] c - id h - sub id d - manor id d - size [ d - crop id d - seed level c d - reward 1 id c d - reward 2 id d - next sale limit d d - min crop price d - max crop price d - today buy d - today price c - today reward d - next buy d - next price c - next
 * reward ]
 * @author l3x
 */
public class ExShowCropSetting extends L2GameServerPacket
{
	private final int cpManorId;
	private final int count;
	private final int[] cropData; // data to send, size:count*14
	
	@Override
	public void runImpl()
	{
	}
	
	public ExShowCropSetting(final int manorId)
	{
		cpManorId = manorId;
		final Castle c = CastleManager.getInstance().getCastleById(cpManorId);
		final List<Integer> crops = L2Manor.getInstance().getCropsForCastle(cpManorId);
		count = crops.size();
		cropData = new int[count * 14];
		int i = 0;
		for (final int cr : crops)
		{
			cropData[i * 14 + 0] = cr;
			cropData[i * 14 + 1] = L2Manor.getInstance().getSeedLevelByCrop(cr);
			cropData[i * 14 + 2] = L2Manor.getInstance().getRewardItem(cr, 1);
			cropData[i * 14 + 3] = L2Manor.getInstance().getRewardItem(cr, 2);
			cropData[i * 14 + 4] = L2Manor.getInstance().getCropPuchaseLimit(cr);
			cropData[i * 14 + 5] = 0; // Looks like not used
			cropData[i * 14 + 6] = L2Manor.getInstance().getCropBasicPrice(cr) * 60 / 100;
			cropData[i * 14 + 7] = L2Manor.getInstance().getCropBasicPrice(cr) * 10;
			CropProcure cropPr = c.getCrop(cr, CastleManorManager.PERIOD_CURRENT);
			if (cropPr != null)
			{
				cropData[i * 14 + 8] = cropPr.getStartAmount();
				cropData[i * 14 + 9] = cropPr.getPrice();
				cropData[i * 14 + 10] = cropPr.getReward();
			}
			else
			{
				cropData[i * 14 + 8] = 0;
				cropData[i * 14 + 9] = 0;
				cropData[i * 14 + 10] = 0;
			}
			cropPr = c.getCrop(cr, CastleManorManager.PERIOD_NEXT);
			if (cropPr != null)
			{
				cropData[i * 14 + 11] = cropPr.getStartAmount();
				cropData[i * 14 + 12] = cropPr.getPrice();
				cropData[i * 14 + 13] = cropPr.getReward();
			}
			else
			{
				cropData[i * 14 + 11] = 0;
				cropData[i * 14 + 12] = 0;
				cropData[i * 14 + 13] = 0;
			}
			i++;
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x20); // SubId
		
		writeD(cpManorId); // manor id
		writeD(count); // size
		
		for (int i = 0; i < count; i++)
		{
			writeD(cropData[i * 14 + 0]); // crop id
			writeD(cropData[i * 14 + 1]); // seed level
			writeC(1);
			writeD(cropData[i * 14 + 2]); // reward 1 id
			writeC(1);
			writeD(cropData[i * 14 + 3]); // reward 2 id
			
			writeD(cropData[i * 14 + 4]); // next sale limit
			writeD(cropData[i * 14 + 5]); // ???
			writeD(cropData[i * 14 + 6]); // min crop price
			writeD(cropData[i * 14 + 7]); // max crop price
			
			writeD(cropData[i * 14 + 8]); // today buy
			writeD(cropData[i * 14 + 9]); // today price
			writeC(cropData[i * 14 + 10]); // today reward
			
			writeD(cropData[i * 14 + 11]); // next buy
			writeD(cropData[i * 14 + 12]); // next price
			writeC(cropData[i * 14 + 13]); // next reward
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:20 ExShowCropSetting";
	}
}
