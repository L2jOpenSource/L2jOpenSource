package l2r.gameserver.network.serverpackets;

import l2r.gameserver.model.actor.instance.L2FenceInstance;

/**
 * Format: (ch)ddddddd d: object id d: type (00 - no fence, 01 - only 4 columns, 02 - columns with fences) d: x coord d: y coord d: z coord d: width d: height
 */
public class ExColosseumFenceInfoPacket extends L2GameServerPacket
{
	private final L2FenceInstance _fence;
	
	public ExColosseumFenceInfoPacket(L2FenceInstance fence)
	{
		_fence = fence;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x03);
		
		writeD(_fence.getObjectId());
		writeD(_fence.getType());
		writeD(_fence.getX());
		writeD(_fence.getY());
		writeD(_fence.getZ());
		writeD(_fence.getWidth());
		writeD(_fence.getLength());
	}
}
