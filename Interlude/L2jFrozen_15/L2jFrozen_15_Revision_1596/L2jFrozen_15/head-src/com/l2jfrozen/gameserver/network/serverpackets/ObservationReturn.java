package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ObservationReturn extends L2GameServerPacket
{
	// ddSS
	private final L2PcInstance activeChar;
	
	/**
	 * @param observer
	 */
	public ObservationReturn(final L2PcInstance observer)
	{
		activeChar = observer;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe0);
		writeD(activeChar.getObsX());
		writeD(activeChar.getObsY());
		writeD(activeChar.getObsZ());
	}
	
	@Override
	public String getType()
	{
		return "[S] E0 ObservationReturn";
	}
}
