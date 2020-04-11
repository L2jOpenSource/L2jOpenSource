package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

/**
 * @author Gnacik
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static int page;
	private static int minlvl;
	private static int maxlvl;
	private static int mode; // 1 - waitlist 0 - room waitlist
	
	@Override
	protected void readImpl()
	{
		page = readD();
		minlvl = readD();
		maxlvl = readD();
		mode = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, page, minlvl, maxlvl, mode));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:16 RequestListPartyMatchingWaitingRoom";
	}
	
}
