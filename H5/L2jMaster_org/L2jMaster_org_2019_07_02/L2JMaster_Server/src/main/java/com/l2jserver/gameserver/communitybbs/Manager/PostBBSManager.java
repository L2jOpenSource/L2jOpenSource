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

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.communitybbs.CommunityBoard;
import com.l2jserver.gameserver.communitybbs.BB.Forum;
import com.l2jserver.gameserver.communitybbs.BB.Post;
import com.l2jserver.gameserver.communitybbs.BB.Post.CPost;
import com.l2jserver.gameserver.communitybbs.BB.Topic;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ShowBoard;

public class PostBBSManager extends BaseBBSManager
{
	private final Map<Topic, Post> _postByTopic = new ConcurrentHashMap<>();
	
	public Post getGPosttByTopic(Topic t)
	{
		Post post = _postByTopic.get(t);
		if (post == null)
		{
			post = new Post(t);
			_postByTopic.put(t, post);
		}
		return post;
	}
	
	/**
	 * @param t
	 */
	public void delPostByTopic(Topic t)
	{
		_postByTopic.remove(t);
	}
	
	public void addPostByTopic(Post p, Topic t)
	{
		if (_postByTopic.get(t) == null)
		{
			_postByTopic.put(t, p);
		}
	}
	
	@Override
	public void parsecmd(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("_bbsposts;read;"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Memo Read", command);
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
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
			
			showPost((MemoBBSManager.getInstance().getTopicByID(idp)), ForumsBBSManager.getInstance().getForumByID(idf), activeChar, ind);
		}
		else if (command.startsWith("_bbsposts;edit;"))
		{
			CommunityBoard.getInstance().addBypass(activeChar, "Memo Edit", command);
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int idf = Integer.parseInt(st.nextToken());
			int idt = Integer.parseInt(st.nextToken());
			int idp = Integer.parseInt(st.nextToken());
			showEditPost((MemoBBSManager.getInstance().getTopicByID(idt)), ForumsBBSManager.getInstance().getForumByID(idf), activeChar, idp);
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
	 * @param topic
	 * @param forum
	 * @param activeChar
	 * @param idp
	 */
	private void showEditPost(Topic topic, Forum forum, L2PcInstance activeChar, int idp)
	{
		if (topic == null)
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error: This topic does not exist!</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else
		{
			final Post p = getGPosttByTopic(topic);
			if ((forum == null) || (p == null))
			{
				ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error: This topic does not exist!</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				showHtmlEditPost(topic, activeChar, forum, p);
			}
		}
	}
	
	/**
	 * @param topic
	 * @param forum
	 * @param activeChar
	 * @param ind
	 */
	private void showPost(Topic topic, Forum forum, L2PcInstance activeChar, int ind)
	{
		if ((forum == null) || (topic == null))
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error: This forum is not implemented yet!</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if (forum.getType() == Forum.MEMO)
		{
			showMemoPost(topic, activeChar, forum);
		}
		else
		{
			ShowBoard sb = new ShowBoard("<html><body><br><br><center>The forum: " + forum.getName() + " is not implemented yet!</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}
	
	/**
	 * @param topic
	 * @param activeChar
	 * @param forum
	 * @param p
	 */
	private void showHtmlEditPost(Topic topic, L2PcInstance activeChar, Forum forum, Post p)
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
		html.append("<td FIXWIDTH=550 height=29>" + topic.getName() + "</td>");
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
		html.append("<td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Post " + forum.getID() + ";" + topic.getID() + ";0 _ Content Content Content\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" ></td>");
		html.append("<td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\"> </td>");
		html.append("<td align=center FIXWIDTH=340>&nbsp;</td>");
		html.append("<td width=5></td>");
		html.append("</tr></table>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		send1001(html.toString(), activeChar);
		send1002(activeChar, p.getCPost(0).postTxt, topic.getName(), DateFormat.getInstance().format(new Date(topic.getDate())));
	}
	
	/**
	 * @param topic
	 * @param activeChar
	 * @param forum
	 */
	private void showMemoPost(Topic topic, L2PcInstance activeChar, Forum forum)
	{
		Post p = getGPosttByTopic(topic);
		StringBuilder html = new StringBuilder("<html><body><br><br>");
		html.append("<table border=0 width=755><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<center>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<table border=0 cellspacing=0 cellpadding=0 width=755 bgcolor=A7A19A>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td fixWIDTH=55 align=right valign=top>&$413; : &nbsp;</td>");
		html.append("<td fixWIDTH=420 valign=top>" + topic.getName() + "</td>");
		html.append("<td fixwidth=5></td>");
		html.append("<td fixwidth=80></td>");
		html.append("<td fixWIDTH=130></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("<tr>");
		html.append("<td align=right><font color=\"AAAAAA\" >&$417; : &nbsp;</font></td>");
		html.append("<td><font color=\"AAAAAA\">" + topic.getOwnerName() + "</font></td>");
		html.append("<td></td>");
		html.append("<td><font color=\"AAAAAA\">&$418; :</font></td>");
		html.append("<td><font color=\"AAAAAA\">" + String.format("%1$tY-%1$tm-%1$te %1$tH:%1tM:%1$tS", new Date(p.getCPost(0).postDate)) + "</font></td>");
		html.append("</tr>");
		html.append("<tr><td height=10></td></tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td fixwidth=5></td>");
		String Mes = p.getCPost(0).postTxt.replace(">", "&gt;");
		Mes = Mes.replace("<", "&lt;");
		Mes = Mes.replace("\n", "<br1>");
		html.append("<td FIXWIDTH=745 align=left>" + Mes + "</td>");
		html.append("<td fixqqwidth=5></td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		html.append("<img src=\"L2UI.squaregray\" width=\"755\" height=\"1\">");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"5\">");
		
		html.append("<table border=0 cellspacing=0 cellpadding=0 FIXWIDTH=755>");
		html.append("<tr>");
		html.append("<td width=50>");
		html.append("<button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\">");
		html.append("</td>");
		html.append("<td width=560 align=right><table border=0 cellspacing=0><tr>");
		html.append("<td FIXWIDTH=300></td><td><button value = \"&$424;\" action=\"bypass _bbsposts;edit;" + forum.getID() + ";" + topic.getID() + ";0\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" ></td>&nbsp;");
		html.append("<td><button value = \"&$425;\" action=\"bypass _bbstopics;del;" + forum.getID() + ";" + topic.getID() + "\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" ></td>&nbsp;");
		html.append("<td><button value = \"&$421;\" action=\"bypass _bbstopics;crea;" + forum.getID() + "\" back=\"l2ui_ct1.button.button_df_small_down\" width=70 height=25 fore=\"l2ui_ct1.button.button_df_small\" ></td>&nbsp;");
		html.append("</tr></table>");
		html.append("</td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<br>");
		html.append("<br></center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html.toString(), activeChar);
	}
	
	@Override
	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(ar1, ";");
		int idf = Integer.parseInt(st.nextToken());
		int idt = Integer.parseInt(st.nextToken());
		int idp = Integer.parseInt(st.nextToken());
		
		Forum f = ForumsBBSManager.getInstance().getForumByID(idf);
		if (f == null)
		{
			separateAndSend("<html><body><br><br><center>the forum: " + idf + " does not exist !</center><br><br></body></html>", activeChar);
		}
		else
		{
			Topic t = f.getTopic(idt);
			if (t == null)
			{
				separateAndSend("<html><body><br><br><center>the topic: " + idt + " does not exist !</center><br><br></body></html>", activeChar);
			}
			else
			{
				final Post p = getGPosttByTopic(t);
				if (p != null)
				{
					final CPost cp = p.getCPost(idp);
					if (cp == null)
					{
						separateAndSend("<html><body><br><br><center>the post: " + idp + " does not exist !</center><br><br></body></html>", activeChar);
					}
					else
					{
						p.getCPost(idp).postTxt = ar4;
						p.updatetxt(idp);
						parsecmd("_bbsposts;read;" + f.getID() + ";" + t.getID(), activeChar);
					}
				}
			}
		}
	}
	
	public static PostBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PostBBSManager _instance = new PostBBSManager();
	}
}