package com.l2jfrozen.gameserver.communitybbs.Manager;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ShowBoard;

public abstract class BaseBBSManager
{
	public abstract void parsecmd(String command, L2PcInstance activeChar);
	
	public abstract void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar);
	
	public static void separateAndSend(final String html, final L2PcInstance acha)
	{
		if (html == null)
		{
			return;
		}
		
		if (html.length() < 4090)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
			
		}
		else if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4090, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
			
		}
		else if (html.length() < 12270)
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
			acha.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "103"));
			
		}
	}
	
	/**
	 * @param html
	 * @param acha
	 */
	protected void send1001(final String html, final L2PcInstance acha)
	{
		if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "1001"));
		}
	}
	
	/**
	 * @param acha
	 */
	protected void send1002(final L2PcInstance acha)
	{
		send1002(acha, " ", " ", "0");
	}
	
	/**
	 * @param activeChar
	 * @param string
	 * @param string2
	 * @param string3
	 */
	protected void send1002(final L2PcInstance activeChar, final String string, final String string2, final String string3)
	{
		List<String> arg = new ArrayList<>();
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add("0");
		arg.add(activeChar.getName());
		arg.add(Integer.toString(activeChar.getObjectId()));
		arg.add(activeChar.getAccountName());
		arg.add("9");
		arg.add(string2);
		arg.add(string2);
		arg.add(string);
		arg.add(string3);
		arg.add(string3);
		arg.add("0");
		arg.add("0");
		activeChar.sendPacket(new ShowBoard(arg));
		arg = null;
	}
}
