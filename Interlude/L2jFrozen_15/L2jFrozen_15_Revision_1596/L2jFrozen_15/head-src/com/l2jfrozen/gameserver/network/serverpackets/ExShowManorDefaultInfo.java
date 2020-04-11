
package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;

import com.l2jfrozen.gameserver.model.L2Manor;

/**
 * format(packet 0xFE) ch cd [ddddcdcd] c - id h - sub id c d - size [ d - level d - seed price d - seed level d - crop price c d - reward 1 id c d - reward 2 id ]
 * @author l3x
 */
public class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private List<Integer> crops = null;
	
	public ExShowManorDefaultInfo()
	{
		crops = L2Manor.getInstance().getAllCrops();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x1E);
		writeC(0);
		writeD(crops.size());
		for (final int cropId : crops)
		{
			writeD(cropId); // crop Id
			writeD(L2Manor.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(L2Manor.getInstance().getSeedBasicPriceByCrop(cropId)); // seed price
			writeD(L2Manor.getInstance().getCropBasicPrice(cropId)); // crop price
			writeC(1); // rewrad 1 Type
			writeD(L2Manor.getInstance().getRewardItem(cropId, 1)); // Rewrad 1 Type Item Id
			writeC(1); // rewrad 2 Type
			writeD(L2Manor.getInstance().getRewardItem(cropId, 2)); // Rewrad 2 Type Item Id
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:1E ExShowManorDefaultInfo";
	}
}
