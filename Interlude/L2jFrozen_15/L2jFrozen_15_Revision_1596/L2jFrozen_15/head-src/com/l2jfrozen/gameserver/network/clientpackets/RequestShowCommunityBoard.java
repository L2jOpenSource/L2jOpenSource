package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.communitybbs.CommunityBoard;

import main.EngineModsManager;

public final class RequestShowCommunityBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int unknown;
	
	/**
	 * packet type id 0x57 sample 57 01 00 00 00 // unknown (always 1?) format: cd
	 */
	@Override
	protected void readImpl()
	{
		unknown = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (EngineModsManager.onCommunityBoard(getClient().getActiveChar(), Config.BBS_DEFAULT))
		{
			return;
		}
		
		CommunityBoard.getInstance().handleCommands(getClient(), Config.BBS_DEFAULT);
	}
	
	@Override
	public String getType()
	{
		return "[C] 57 RequestShowBoard";
	}
}
