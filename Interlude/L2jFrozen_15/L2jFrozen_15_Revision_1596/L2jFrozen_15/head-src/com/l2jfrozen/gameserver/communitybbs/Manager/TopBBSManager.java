package com.l2jfrozen.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ShowBoard;

public class TopBBSManager extends BaseBBSManager
{
	@Override
	public void parsecmd(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("_bbstop"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			
			separateAndSend(content, activeChar);
			content = null;
		}
		else if (command.equals("_bbshome"))
		{
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/index.htm");
			
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/index.htm' </center></body></html>";
			}
			
			separateAndSend(content, activeChar);
			content = null;
		}
		else if (command.startsWith("_bbstop;"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			
			final int idp = Integer.parseInt(st.nextToken());
			
			String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + idp + ".htm");
			
			if (content == null)
			{
				content = "<html><body><br><br><center>404 :File Not foud: 'data/html/CommunityBoard/" + idp + ".htm' </center></body></html>";
			}
			separateAndSend(content, activeChar);
			st = null;
			content = null;
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
		//
	}
	
	private static TopBBSManager instance = new TopBBSManager();
	
	/**
	 * @return
	 */
	public static TopBBSManager getInstance()
	{
		return instance;
	}
}
