package l2r.gameserver.network.serverpackets;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class ExBrGamePoint extends L2GameServerPacket
{
	private final int _objId;
	private long _points;
	
	public ExBrGamePoint(L2PcInstance player)
	{
		_objId = player.getObjectId();
		
		if (Config.GAME_POINT_ITEM_ID == -1)
		{
			_points = player.getGamePoints();
		}
		else
		{
			_points = player.getInventory().getInventoryItemCount(Config.GAME_POINT_ITEM_ID, -100);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD5);
		writeD(_objId);
		writeQ(_points);
		writeD(0x00);
	}
}