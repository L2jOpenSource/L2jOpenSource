package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.communitybbs.CommunityBoard;

/**
 * Format SSSSSS
 * @author -Wooden-
 */
public class RequestBBSwrite extends L2GameClientPacket
{
	private String url;
	private String arg1;
	private String arg2;
	private String arg3;
	private String arg4;
	private String arg5;
	
	@Override
	protected void readImpl()
	{
		url = readS();
		arg1 = readS();
		arg2 = readS();
		arg3 = readS();
		arg4 = readS();
		arg5 = readS();
	}
	
	@Override
	protected void runImpl()
	{
		CommunityBoard.getInstance().handleWriteCommands(getClient(), url, arg1, arg2, arg3, arg4, arg5);
	}
	
	@Override
	public String getType()
	{
		return "[C] 22 RequestBBSwrite";
	}
}
