package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: (ch) dc d: character object id c: 1 if won 0 if failed
 * @author -Wooden-
 */
public class ExFishingEnd extends L2GameServerPacket
{
	private final boolean win;
	private L2Character activeChar;
	
	public ExFishingEnd(final boolean win, final L2PcInstance character)
	{
		this.win = win;
		activeChar = character;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x14);
		writeD(activeChar.getObjectId());
		writeC(win ? 1 : 0);
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:14 ExFishingEnd";
	}
	
}
