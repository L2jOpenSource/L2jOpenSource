package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSigns;

/**
 * sample format d
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShowMiniMap extends L2GameServerPacket
{
	private final int mapId;
	
	/**
	 * @param mapId
	 */
	public ShowMiniMap(final int mapId)
	{
		this.mapId = mapId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x9d);
		writeD(mapId);
		writeD(SevenSigns.getInstance().getCurrentPeriod());
	}
	
	@Override
	public String getType()
	{
		return "[S] 9d ShowMiniMap";
	}
}
