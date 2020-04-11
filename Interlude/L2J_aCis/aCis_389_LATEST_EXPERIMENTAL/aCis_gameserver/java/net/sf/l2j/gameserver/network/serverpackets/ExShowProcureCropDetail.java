package net.sf.l2j.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.manor.CropProcure;

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private final int _cropId;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<>();
		
		for (Castle c : CastleManager.getInstance().getCastles())
		{
			final CropProcure cropItem = CastleManorManager.getInstance().getCropProcure(c.getCastleId(), cropId, false);
			if (cropItem != null && cropItem.getAmount() > 0)
				_castleCrops.put(c.getCastleId(), cropItem);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x22);
		
		writeD(_cropId);
		writeD(_castleCrops.size());
		
		for (Map.Entry<Integer, CropProcure> entry : _castleCrops.entrySet())
		{
			final CropProcure crop = entry.getValue();
			
			writeD(entry.getKey());
			writeD(crop.getAmount());
			writeD(crop.getPrice());
			writeC(crop.getReward());
		}
	}
}