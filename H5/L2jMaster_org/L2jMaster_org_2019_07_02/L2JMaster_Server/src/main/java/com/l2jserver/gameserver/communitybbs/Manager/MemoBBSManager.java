/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.communitybbs.Manager;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jserver.gameserver.communitybbs.CommunityBoard;
import com.l2jserver.gameserver.communitybbs.BB.Forum;
import com.l2jserver.gameserver.communitybbs.BB.Post;
import com.l2jserver.gameserver.communitybbs.BB.Topic;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ShowBoard;


public class MemoBBSManager extends BaseBBSManager
{
	private final List<Topic> _table = new CopyOnWriteArrayList<>();
	private final Map<Forum, Integer> _maxId = new HashMap<>();
	
	protected MemoBBSManager()
	{
		// Prevent external initialization.
	}
	
	public void addTopic(Topic tt)
	{
		_table.add(tt);
	}
	
	/**
	 * @param topic
	 */
	public void delTopic(Topic topic)
	{
		_table.remove(topic);
	}
	
	public void setMaxID(int id, Forum f)
	{
		_maxId.put(f, id);
	}
	
	public int getMaxID(Forum f)
	{
		Integer i = _maxId.get(f);
		if (i == null)
		{
			return 0;
		}
		return i;
	}
	
	public Topic getTopicByID(int idf)
	{
		for (Topic t : _table)
		{
			if (t.getID() == idf)
			{
				return t;
			}
		}
		return null;
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.equals("_bbsmemo"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Memo Command", command);
			if (activeChar.getMemo() != null)
			{
				showTopics(activeChar.getMemo(), activeChar, 1, activeChar.getMemo().getID());
			}
		}
		else if (command.startsWith("_bbstopics;read"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Memo Read", command);
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			String index = null;
			if (st.hasMoreTokens())
			{
				index = st.nextToken();
			}
			int ind = 0;
			if (index == null)
			{
				ind = 1;
			}
			else
			{
				ind = Integer.parseInt(index);
			}
			showTopics(ForumsBBSManager.getInstance().getForumByID(idf), activeChar, ind, idf);
		}
		else if (command.startsWith("_bbstopics;crea"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Memo Create", command);
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			showNewTopic(ForumsBBSManager.getInstance().getForumByID(idf), activeChar, idf);
		}
		else if (command.startsWith("_bbstopics;del"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Memo Delete", command);
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idt = Integer.parseInt(st.nextToken());
			Forum f = ForumsBBSManager.getInstance().getForumByID(idf);
			if (f == null)
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " does not exist !</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				Topic t = f.getTopic(idt);
				if (t == null)
				{
					ShowBoard sb = new ShowBoard("<html><body><br><br><center>the topic: " + idt + " does not exist !</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				else
				{
					// CPost cp = null;
					Post p = PostBBSManager.getInstance().getGPosttByTopic(t);
					if (p != null)
					{
						p.deleteme(t);
					}
					t.deleteme(f);
					parsecmd("_bbsmemo", activeChar);
				}
			}
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * @param forum
	 * @param activeChar
	 * @param idf
	 */
	private void showNewTopic(Forum forum, L2PcInstance activeChar, int idf)
	{
		if (forum == null)
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (forum.getType() == Forum.MEMO)
		{
			showMemoNewTopics(forum, activeChar);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getName() + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * @param forum
	 * @param activeChar
	 */
	private void showMemoNewTopics(Forum forum, L2PcInstance activeChar)
	{
		StringBuilder html = new StringBuilder("<html>");
		html.append("<body><br><br>");
		html.append("<table border=0 width=755><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">" + forum.getName() + " Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td width=755><img src=\"L2UI.SquareGray\" width=\"755\" height=\"1\"></td></tr>");
		html.append("</table>");
		
		html.append("<table fixwidth=755 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td width=5 height=20></td></tr>");
		html.append("<tr>");
		html.append("<td width=5></td>");
		html.append("<td align=center FIXWIDTH=50 height=29>&$413;</td>");
		html.append("<td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td>");
		html.append("<td width=5></td>");
		html.append("</tr></table>");
		
		html.append("<table fixwidth=755 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=5></td>");
		html.append("<td align=center FIXWIDTH=50 height=29 valign=top>&$427;</td>");
		html.append("<td align=center FIXWIDTH=550><MultiEdit var =\"Content\" width=700 height=313></td>");
		html.append("<td width=5></td>");
		html.append("</tr>");
		html.append("<tr><td width=5 height=10></td></tr>");
		html.append("</table>");
		
		html.append("<table fixwidth=755 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td width=5></td>");
		html.append("<td align=center FIXWIDTH=50 height=29>&nbsp;</td>");
		html.append("<td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea " + forum.getID() + " Title Content Title\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" ></td>");
		html.append("<td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\"> </td>");
		html.append("<td align=center FIXWIDTH=340>&nbsp;</td>");
		html.append("<td width=5></td>");
		html.append("</tr></table>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		send1001(html.toString(), activeChar);
		send1002(activeChar);
	}
	
	/**
	 * @param forum
	 * @param activeChar
	 * @param index
	 * @param idf
	 */
	private void showTopics(Forum forum, L2PcInstance activeChar, int index, int idf)
	{
		if (forum == null)
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (forum.getType() == Forum.MEMO)
		{
			showMemoTopics(forum, activeChar, index);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getName() + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * @param forum
	 * @param activeChar
	 * @param index
	 */
	private void showMemoTopics(Forum forum, L2PcInstance activeChar, int index)
	{
		forum.vload();
		StringBuilder html = new StringBuilder("<html><body><br><br>");
		html.append("<table border=0 width=755><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<center>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=755 bgcolor=A7A19A>");
		html.append("<tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=500 align=center>&$413;</td>");
		html.append("<td FIXWIDTH=145 align=center></td>");
		html.append("<td FIXWIDTH=75 align=center>&$418;</td>");
		html.append("</tr>");
		html.append("</table>");
		
		for (int i = 0, j = getMaxID(forum) + 1; i < (12 * index); j--)
		{
			if (j < 0)
			{
				break;
			}
			Topic t = forum.getTopic(j);
			if (t != null)
			{
				if (i++ >= (12 * (index - 1)))
				{
					html.append("<table border=0 cellspacing=0 cellpadding=5 WIDTH=755>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("<td FIXWIDTH=500><a action=\"bypass _bbsposts;read;" + forum.getID() + ";" + t.getID() + "\">" + t.getName() + "</a></td>");
					html.append("<td FIXWIDTH=145 align=center></td>");
					html.append("<td FIXWIDTH=75 align=center>" + String.format("%1$te-%1$tm-%1$tY", new Date(t.getDate())) + "</td>");
					html.append("</tr>");
					html.append("</table>");
					html.append("<img src=\"L2UI.Squaregray\" width=\"755\" height=\"1\">");
				}
			}
		}
		
		html.append("<br>");
		html.append("<table width=755 cellspace=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=50>");
		html.append("<button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\">");
		html.append("</td>");
		html.append("<td width=510 align=center>");
		html.append("<table border=0><tr>");
		
		if (index == 1)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		else
		{
			html.append("<td><button action=\"bypass _bbstopics;read;" + forum.getID() + ";" + (index - 1) + "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		}
		int nbp;
		nbp = forum.getTopicSize() / 8;
		if ((nbp * 8) != ClanTable.getInstance().getClans().size())
		{
			nbp++;
		}
		for (int i = 1; i <= nbp; i++)
		{
			if (i == index)
			{
				html.append("<td> " + i + " </td>");
			}
			else
			{
				html.append("<td><a action=\"bypass _bbstopics;read;" + forum.getID() + ";" + i + "\"> " + i + " </a></td>");
			}
		}
		if (index == nbp)
		{
			html.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		else
		{
			html.append("<td><button action=\"bypass _bbstopics;read;" + forum.getID() + ";" + (index + 1) + "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		}
		
		html.append("</tr></table> </td> ");
		html.append("<td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;" + forum.getID() + "\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" ></td></tr>");
		html.append("<tr><td width=5 height=10></td></tr>");
		html.append("<tr> ");
		html.append("<td></td>");
		html.append("<td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=15></td>");
		html.append("<td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\"> </td> </tr></table> </td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<br>");
		html.append("<br>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		if (ar1.equals("crea"))
		{
			Forum f = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if (f == null)
			{
				separateAndSend("<html><body><br><br><center>the forum: " + ar2 + " is not implemented yet</center><br><br></body></html>", activeChar);
			}
			else
			{
				f.vload();
				Topic t = new Topic(Topic.ConstructorType.CREATE, MemoBBSManager.getInstance().getMaxID(f) + 1, Integer.parseInt(ar2), ar5, Calendar.getInstance().getTimeInMillis(), activeChar.getName(), activeChar.getObjectId(), Topic.MEMO, 0);
				f.addTopic(t);
				MemoBBSManager.getInstance().setMaxID(t.getID(), f);
				Post p = new Post(activeChar.getName(), activeChar.getObjectId(), Calendar.getInstance().getTimeInMillis(), t.getID(), f.getID(), ar4);
				PostBBSManager.getInstance().addPostByTopic(p, t);
				parsecmd("_bbsmemo", activeChar);
			}
		}
		else if (ar1.equals("del"))
		{
			Forum f = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if (f == null)
			{
				separateAndSend("<html><body><br><br><center>the forum: " + ar2 + " does not exist !</center><br><br></body></html>", activeChar);
			}
			else
			{
				Topic t = f.getTopic(Integer.parseInt(ar3));
				if (t == null)
				{
					separateAndSend("<html><body><br><br><center>the topic: " + ar3 + " does not exist !</center><br><br></body></html>", activeChar);
				}
				else
				{
					// CPost cp = null;
					Post p = PostBBSManager.getInstance().getGPosttByTopic(t);
					if (p != null)
					{
						p.deleteme(t);
					}
					t.deleteme(f);
					parsecmd("_bbsmemo", activeChar);
				}
			}
		}
		else
		{
			separateAndSend("<html><body><br><br><center>the command: " + ar1 + " is not implemented yet</center><br><br></body></html>", activeChar);
		}
	}
	
	public static MemoBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MemoBBSManager _instance = new MemoBBSManager();
	}
}