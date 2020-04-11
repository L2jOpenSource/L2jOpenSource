package com.l2jfrozen.gameserver.communitybbs.Manager;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ShowBoard;

public class AdminBBSManager extends BaseBBSManager
{
	private static AdminBBSManager instance = null;
	
	/**
	 * @return
	 */
	public static AdminBBSManager getInstance()
	{
		if (instance == null)
		{
			instance = new AdminBBSManager();
		}
		return instance;
	}
	
	@Override
	public void parsecmd(final String command, final L2PcInstance activeChar)
	{
		if (activeChar.getAccessLevel().isGm())
		{
			return;
		}
		if (command.startsWith("admin_bbs"))
		{
			separateAndSend("<html><body><br><br><center>This Page is only an exemple :)<br><br>command=" + command + "</center></body></html>", activeChar);
		}
		else
		{
			
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			sb = null;
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		
	}
	
	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final L2PcInstance activeChar)
	{
		if (activeChar.getAccessLevel().isGm())
		{
			return;
		}
		
	}
}
