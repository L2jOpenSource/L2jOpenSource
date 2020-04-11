package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * sample 0000: 17 1a 95 20 48 9b da 12 40 44 17 02 00 03 f0 fc ff 98 f1 ff ff ..... format ddddd
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class GetItem extends L2GameServerPacket
{
	private final L2ItemInstance item;
	private final int playerId;
	
	public GetItem(final L2ItemInstance item, final int playerId)
	{
		this.item = item;
		this.playerId = playerId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0d);
		writeD(playerId);
		writeD(item.getObjectId());
		
		writeD(item.getX());
		writeD(item.getY());
		writeD(item.getZ());
	}
	
	@Override
	public String getType()
	{
		return "[S] 0d GetItem";
	}
	
}
