package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author godson
 */
public class ExOlympiadMode extends L2GameServerPacket
{
	private int mode;
	private final L2PcInstance activeChar;
	
	/**
	 * @param mode   : <BR>
	 *                   0 = return<BR>
	 *                   1 = side 1<BR>
	 *                   2 = side 2<BR>
	 *                   3 = spectate
	 * @param player
	 */
	public ExOlympiadMode(final int mode, final L2PcInstance player)
	{
		activeChar = player;
		this.mode = mode;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (activeChar == null)
		{
			return;
		}
		
		if (mode == 3)
		{
			activeChar.setObserverMode(true);
		}
		
		writeC(0xfe);
		writeH(0x2b);
		writeC(mode);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:2B ExOlympiadMode";
	}
}
