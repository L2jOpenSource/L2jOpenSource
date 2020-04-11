package com.l2jfrozen.gameserver.communitybbs.Manager;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

import javolution.text.TextBuilder;

public class ClanBBSManager extends BaseBBSManager
{
	private static ClanBBSManager instance = new ClanBBSManager();
	
	/**
	 * @return
	 */
	public static ClanBBSManager getInstance()
	{
		return instance;
	}
	
	/**
	 * @param command
	 * @param activeChar
	 */
	@Override
	public void parsecmd(final String command, final L2PcInstance activeChar)
	{
		if (command.equals("_bbsclan"))
		{
			if (activeChar.getClan() != null)
			{
				if (activeChar.getClan().getLevel() >= 2)
				{
					clanhome(activeChar);
				}
				else
				{
					clanlist(activeChar, 1);
				}
			}
			else
			{
				clanlist(activeChar, 1);
			}
		}
		else if (command.startsWith("_bbsclan_clanlist"))
		{
			if (command.equals("_bbsclan_clanlist"))
			{
				clanlist(activeChar, 1);
			}
			else if (command.startsWith("_bbsclan_clanlist;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				final int index = Integer.parseInt(st.nextToken());
				clanlist(activeChar, index);
				st = null;
			}
		}
		else if (command.startsWith("_bbsclan_clanhome"))
		{
			if (command.equals("_bbsclan_clanhome"))
			{
				clanhome(activeChar);
			}
			else if (command.startsWith("_bbsclan_clanhome;"))
			{
				StringTokenizer st = new StringTokenizer(command, ";");
				st.nextToken();
				final int index = Integer.parseInt(st.nextToken());
				clanhome(activeChar, index);
				st = null;
			}
		}
		else if (command.startsWith("_bbsclan_clannotice_edit;"))
		{
			if (activeChar != null && activeChar.getClan() != null)
			{
				clanNotice(activeChar, activeChar.getClan().getClanId());
			}
		}
		else if (command.startsWith("_bbsclan_clannotice_enable"))
		{
			activeChar.getClan().setNoticeEnabled(true);
			clanNotice(activeChar, activeChar.getClan().getClanId());
		}
		else if (command.startsWith("_bbsclan_clannotice_disable"))
		{
			activeChar.getClan().setNoticeEnabled(false);
			clanNotice(activeChar, activeChar.getClan().getClanId());
		}
		else
		{
			separateAndSend("<html><body><br><br><center>Commande : " + command + " pas encore implante</center><br><br></body></html>", activeChar);
		}
	}
	
	/**
	 * @param activeChar
	 * @param clanId
	 */
	private void clanNotice(final L2PcInstance activeChar, final int clanId)
	{
		final L2Clan cl = ClanTable.getInstance().getClan(clanId);
		if (cl != null)
		{
			if (cl.getLevel() < 2)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.NO_CB_IN_MY_CLAN));
				parsecmd("_bbsclan_clanlist", activeChar);
			}
			else
			{
				final TextBuilder html = new TextBuilder("<html><body><br><br>" + "<table border=0 width=610><tr><td width=10></td><td width=600 align=left>" + "<a action=\"bypass _bbshome\">HOME</a> &gt; " + "<a action=\"bypass _bbsclan_clanlist\"> CLAN COMMUNITY </a>  &gt; " + "<a action=\"bypass _bbsclan_clanhome;" + String.valueOf(clanId) + "\"> &amp;$802; </a>" + "</td></tr>" + "</table>");
				if (activeChar.isClanLeader())
				{
					html.append("<br><br><center>" + "<table width=610 border=0 cellspacing=0 cellpadding=0>" + "<tr><td fixwidth=610><font color=\"AAAAAA\">The Clan Notice function allows the clan leader to send messages through a pop-up window to clan members at login.</font> </td></tr>" + "<tr><td height=20></td></tr>");
					
					if (activeChar.getClan().isNoticeEnabled())
					{
						html.append("<tr><td fixwidth=610> Clan Notice Function:&nbsp;&nbsp;&nbsp;on&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;<a action=\"bypass _bbsclan_clannotice_disable\">off</a>");
					}
					else
					{
						html.append("<tr><td fixwidth=610> Clan Notice Function:&nbsp;&nbsp;&nbsp;<a action=\"bypass _bbsclan_clannotice_enable\">on</a>&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;off");
					}
					
					html.append("</td></tr>" + "</table>" + "<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">" + "<br> <br>" + "<table width=610 border=0 cellspacing=2 cellpadding=0>" + "<tr><td>Edit Notice: </td></tr>" + "<tr><td height=5></td></tr>" + "<tr><td>" + "<MultiEdit var =\"Content\" width=610 height=100>" + "</td></tr>" + "</table>" + "<br>"
						+ "<table width=610 border=0 cellspacing=0 cellpadding=0>" + "<tr><td height=5></td></tr>" + "<tr>" + "<td align=center FIXWIDTH=65><button value=\"&$140;\" action=\"Write Notice Set _ Content Content Content\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>" + "<td align=center FIXWIDTH=45></td>"
						+ "<td align=center FIXWIDTH=500></td>" + "</tr>" + "</table>" + "</center>" + "</body>" + "</html>");
					send1001(html.toString(), activeChar);
					send1002(activeChar, activeChar.getClan().getNotice(), " ", "0");
				}
				else
				{
					html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">" + "<center>" + "<table border=0 cellspacing=0 cellpadding=0><tr>" + "<td>You are not your clan's leader, and therefore cannot change the clan notice</td>" + "</tr></table>");
					if (activeChar.getClan().isNoticeEnabled())
					{
						html.append("<table border=0 cellspacing=0 cellpadding=0>" + "<tr>" + "<td>The current clan notice:</td>" + "</tr>" + "<tr><td fixwidth=5></td>" + "<td FIXWIDTH=600 align=left>" + activeChar.getClan().getNotice() + "</td>" + "<td fixqqwidth=5></td>" + "</tr>" + "</table>");
					}
					html.append("</center>" + "</body>" + "</html>");
					separateAndSend(html.toString(), activeChar);
				}
			}
		}
	}
	
	private void clanlist(final L2PcInstance activeChar, int index)
	{
		if (index < 1)
		{
			index = 1;
		}
		// header
		TextBuilder html = new TextBuilder("<html><body><br><br><center>");
		html.append("<br1><br1><table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td FIXWIDTH=15>&nbsp;</td>");
		html.append("<td width=610 height=30 align=left>");
		html.append("<a action=\"bypass _bbsclan_clanlist\"> CLAN COMMUNITY </a>");
		html.append("</td></tr></table>");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=5></td>");
		html.append("<td fixWIDTH=600>");
		html.append("<a action=\"bypass _bbsclan_clanhome;" + (activeChar.getClan() != null ? activeChar.getClan().getClanId() : 0) + "\">[GO TO MY CLAN]</a>&nbsp;&nbsp;");
		html.append("</td>");
		html.append("<td fixWIDTH=5></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		// body
		html.append("<br>");
		html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=5A5A5A width=610>");
		html.append("<tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=200 align=center>CLAN NAME</td>");
		html.append("<td FIXWIDTH=200 align=center>CLAN LEADER</td>");
		html.append("<td FIXWIDTH=100 align=center>CLAN LEVEL</td>");
		html.append("<td FIXWIDTH=100 align=center>CLAN MEMBERS</td>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.Squareblank\" width=\"1\" height=\"5\">");
		int i = 0;
		for (final L2Clan cl : ClanTable.getInstance().getClans())
		{
			if (i > (index + 1) * 7)
			{
				break;
			}
			
			if (i++ >= (index - 1) * 7)
			{
				html.append("<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">");
				html.append("<table border=0 cellspacing=0 cellpadding=0 width=610>");
				html.append("<tr> ");
				html.append("<td FIXWIDTH=5></td>");
				html.append("<td FIXWIDTH=200 align=center><a action=\"bypass _bbsclan_clanhome;" + cl.getClanId() + "\">" + cl.getName() + "</a></td>");
				html.append("<td FIXWIDTH=200 align=center>" + cl.getLeaderName() + "</td>");
				html.append("<td FIXWIDTH=100 align=center>" + cl.getLevel() + "</td>");
				html.append("<td FIXWIDTH=100 align=center>" + cl.getMembersCount() + "</td>");
				html.append("<td FIXWIDTH=5></td>");
				html.append("</tr>");
				html.append("<tr><td height=5></td></tr>");
				html.append("</table>");
				html.append("<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"3\">");
				html.append("<img src=\"L2UI.SquareGray\" width=\"610\" height=\"1\">");
			}
		}
		html.append("<img src=\"L2UI.SquareBlank\" width=\"610\" height=\"2\">");
		html.append("<table cellpadding=0 cellspacing=2 border=0><tr>");
		
		if (index == 1)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			html.append("<td><button action=\"_bbsclan_clanlist;" + (index - 1) + "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		
		i = 0;
		int nbp;
		
		nbp = ClanTable.getInstance().getClans().length / 8;
		
		if (nbp * 8 != ClanTable.getInstance().getClans().length)
		{
			nbp++;
		}
		
		for (i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				html.append("<td> " + i + " </td>");
			}
			else
			{
				html.append("<td><a action=\"bypass _bbsclan_clanlist;" + i + "\"> " + i + " </a></td>");
			}
			
		}
		
		if (index == nbp)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			html.append("<td><button action=\"bypass _bbsclan_clanlist;" + (index + 1) + "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		
		html.append("</tr></table>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td width=610><img src=\"sek.cbui141\" width=\"610\" height=\"1\"></td></tr>");
		html.append("</table>");
		html.append("<table border=0><tr><td><combobox width=65 var=keyword list=\"Name;Ruler\"></td><td><edit var = \"Search\" width=130 height=11 " + "length=\"16\"></td>");
		// TODO: search (Write in BBS)
		html.append("<td><button value=\"&$420;\" action=\"Write 5 -1 0 Search keyword keyword\" back=\"l2ui_ch3.smallbutton2_down\" width=65 " + "height=20 fore=\"l2ui_ch3.smallbutton2\"> </td> </tr></table>");
		html.append("<br>");
		html.append("<br>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html.toString(), activeChar);
		html = null;
	}
	
	/**
	 * @param activeChar
	 */
	private void clanhome(final L2PcInstance activeChar)
	{
		clanhome(activeChar, activeChar.getClan().getClanId());
	}
	
	/**
	 * @param activeChar
	 * @param clanId
	 */
	private void clanhome(final L2PcInstance activeChar, final int clanId)
	{
		L2Clan cl = ClanTable.getInstance().getClan(clanId);
		
		if (cl != null)
		{
			if (cl.getLevel() < 2)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.NO_CB_IN_MY_CLAN));
				parsecmd("_bbsclan_clanlist", activeChar);
			}
			else
			{
				TextBuilder html = new TextBuilder("<html><body><center><br><br>");
				html.append("<br1><br1><table border=0 cellspacing=0 cellpadding=0>");
				html.append("<tr><td FIXWIDTH=15>&nbsp;</td>");
				html.append("<td width=610 height=30 align=left>");
				html.append("<a action=\"bypass _bbshome\">HOME</a> &gt; <a action=\"bypass _bbsclan_clanlist\"> CLAN COMMUNITY </a>  &gt; " + "<a action=\"bypass _bbsclan_clanhome;" + clanId + "\"> &amp;$802; </a>");
				html.append("</td></tr></table>");
				html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=434343>");
				html.append("<tr><td height=10></td></tr>");
				html.append("<tr>");
				html.append("<td fixWIDTH=5></td>");
				html.append("<td fixwidth=600>");
				html.append("<a action=\"bypass _bbsclan_clanhome;" + clanId + ";announce\">[CLAN ANNOUNCEMENT]</a> <a action=\"bypass _bbsclan_clanhome;" + clanId + ";cbb\">[CLAN BULLETIN BOARD]</a>");
				html.append("<a action=\"bypass _bbsclan_clanhome;" + clanId + ";cmail\">[CLAN MAIL]</a>&nbsp;&nbsp;");
				html.append("<a action=\"bypass _bbsclan_clannotice_edit;" + clanId + ";cnotice\">[CLAN NOTICE]</a>&nbsp;&nbsp;");
				html.append("</td>");
				html.append("<td fixWIDTH=5></td>");
				html.append("</tr>");
				html.append("<tr><td height=10></td></tr>");
				html.append("</table>");
				html.append("<table border=0 cellspacing=0 cellpadding=0 width=610>");
				html.append("<tr><td height=10></td></tr>");
				html.append("<tr><td fixWIDTH=5></td>");
				html.append("<td fixwidth=290 valign=top>");
				html.append("</td>");
				html.append("<td fixWIDTH=5></td>");
				html.append("<td fixWIDTH=5 align=center valign=top><img src=\"l2ui.squaregray\" width=2  height=128></td>");
				html.append("<td fixWIDTH=5></td>");
				html.append("<td fixwidth=295>");
				html.append("<table border=0 cellspacing=0 cellpadding=0 width=295>");
				html.append("<tr>");
				html.append("<td fixWIDTH=100 align=left>CLAN NAME</td>");
				html.append("<td fixWIDTH=195 align=left>" + cl.getName() + "</td>");
				html.append("</tr>");
				html.append("<tr><td height=7></td></tr>");
				html.append("<tr>");
				html.append("<td fixWIDTH=100 align=left>CLAN LEVEL</td>");
				html.append("<td fixWIDTH=195 align=left height=16>" + cl.getLevel() + "</td>");
				html.append("</tr>");
				html.append("<tr><td height=7></td></tr>");
				html.append("<tr>");
				html.append("<td fixWIDTH=100 align=left>CLAN MEMBERS</td>");
				html.append("<td fixWIDTH=195 align=left height=16>" + cl.getMembersCount() + "</td>");
				html.append("</tr>");
				html.append("<tr><td height=7></td></tr>");
				html.append("<tr>");
				html.append("<td fixWIDTH=100 align=left>CLAN LEADER</td>");
				html.append("<td fixWIDTH=195 align=left height=16>" + cl.getLeaderName() + "</td>");
				html.append("</tr>");
				html.append("<tr><td height=7></td></tr>");
				// ADMINISTRATOR ??
				/*
				 * html.append("<tr>"); html.append("<td fixWIDTH=100 align=left>ADMINISTRATOR</td>"); html.append("<td fixWIDTH=195 align=left height=16>"+cl.getLeaderName()+"</td>"); html.append("</tr>");
				 */
				html.append("<tr><td height=7></td></tr>");
				html.append("<tr>");
				html.append("<td fixWIDTH=100 align=left>ALLIANCE</td>");
				html.append("<td fixWIDTH=195 align=left height=16>" + (cl.getAllyName() != null ? cl.getAllyName() : "") + "</td>");
				html.append("</tr>");
				html.append("</table>");
				html.append("</td>");
				html.append("<td fixWIDTH=5></td>");
				html.append("</tr>");
				html.append("<tr><td height=10></td></tr>");
				html.append("</table>");
				// TODO: the BB for clan :)
				// html.append("<table border=0 cellspacing=0 cellpadding=0 width=610 bgcolor=333333>");
				html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
				html.append("<img src=\"L2UI.squaregray\" width=\"610\" height=\"1\">");
				html.append("<br>");
				html.append("</center>");
				html.append("<br> <br>");
				html.append("</body>");
				html.append("</html>");
				separateAndSend(html.toString(), activeChar);
				html = null;
			}
		}
		
		cl = null;
	}
	
	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final L2PcInstance activeChar)
	{
		if (ar1.equals("Set"))
		{
			activeChar.getClan().setNotice(ar4);
			parsecmd("_bbsclan_clanhome;" + activeChar.getClan().getClanId(), activeChar);
		}
	}
}