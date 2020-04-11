package main.engine.community;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.managers.FortSiegeManager;
import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.objects.PlayerHolder;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class ClanCommunityBoard extends AbstractMod
{
	public ClanCommunityBoard()
	{
		registerMod(ConfigData.ENABLE_BBS_CLAN);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onCommunityBoard(PlayerHolder ph, String command)
	{
		L2PcInstance player = ph.getInstance();
		
		if (command.startsWith("_bbsclan"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			// bbsclan
			st.nextToken();
			// bypass
			String bypass = st.hasMoreTokens() ? st.nextToken() : "listClans";
			
			HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
			hb.append(Html.START);
			hb.append("<br>");
			hb.append("<center>");
			hb.append(bbsHead(bypass));
			
			switch (bypass)
			{
				case "info":
				{
					// TODO: develpement!
					// int clanId = Integer.parseInt(st.nextToken());
					
					// Clan clan = ClanTable.getInstance().getClan(clanId);
					
					break;
				}
				case "listClans":
				{
					// list of all the clans
					// page
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
					
					hb.append(bbsListClan(page));
					break;
				}
				case "myClan":
				{
					boolean hasClan = false;
					if (player.getClanId() > 0)
					{
						hasClan = true;
					}
					
					hb.append(Html.headCommunity(hasClan ? player.getClan().getName() : "No Name"));
					hb.append("<table border=0 cellspacing=0 cellpadding=0>");
					hb.append("<tr>");
					// Lateral menu start -----------------------------------------
					hb.append("<td align=center fixwidth=100>");
					hb.append(bbsMyClanLeft());
					hb.append("</td>");
					// Lateral menu end -------------------------------------------
					
					String center = st.hasMoreTokens() ? st.nextToken() : "membersPage";
					int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
					
					hb.append("<td align=center fixwidth=460>");
					hb.append(topInfoClan());
					
					if (!hasClan)
					{
						center = "noHasClan";
						hb.append(bodyInfoNoClan(0));
					}
					else
					{
						// A clan info is displayed
						hb.append(bodyInfoClan(player.getClan(), 0));
					}
					
					switch (center)
					{
						case "noHasClan":
						{
							// TODO Could you put it in a method to occupy less space, no?
							
							hb.append("<br>");
							hb.append("<table border=0 cellspacing=0 cellpadding=0 width=460");
							hb.append("<tr><td align=center>");
							hb.append("Actualmente no tienes un clan.<br>");
							hb.append(Html.fontColor("LEVEL", "Deseas crear uno?"), "<br>");
							hb.append("Ingresa el nombre del clan.<br>");
							hb.append("<edit var=\"name\" width=120><br>");
							hb.append("<button value=\"Next\" action=\"bypass _bbsclan createClan $name\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
							hb.append("</td></tr>");
							hb.append("</table>");
							break;
						}
						case "membersPage":
						{
							hb.append(bbsInfoMembers(ph, page));
							break;
						}
						case "createAllyPage":
						{
							hb.append("<br>");
							hb.append("<table border=0 cellspacing=0 cellpadding=0 width=460");
							hb.append("<tr><td align=center>");
							hb.append(Html.fontColor("LEVEL", "Quieres tener una alianza?"), "<br>");
							hb.append("Ingresa el nombre de la alianza<br>");
							hb.append("<edit var=\"name\" width=120><br>");
							hb.append("<button value=\"Next\" action=\"bypass _bbsclan createAlly $name\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
							hb.append("</td></tr>");
							hb.append("</table>");
							break;
						}
						case "disolveClanPage":
						{
							hb.append("<br>");
							hb.append("<table border=0 cellspacing=0 cellpadding=0 width=460");
							hb.append("<tr><td align=center>");
							hb.append(Html.fontColor("LEVEL", "Quieres disolver tu clan?"), "<br>");
							hb.append("<button value=\"Yes\" action=\"bypass _bbsclan disolveClan\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
							hb.append("</td></tr>");
							hb.append("</table>");
							break;
						}
						case "disolveAllyPage":
						case "createAcademyPage":
						case "createRoyalPage":
						case "createKnightPage":
						{
							hb.append("<br>");
							hb.append("<table border=0 cellspacing=0 cellpadding=0 width=460");
							hb.append("<tr><td align=center>");
							hb.append(Html.fontColor("LEVEL", "Not working yet"), "<br>");
							hb.append("</td></tr>");
							hb.append("</table>");
							break;
						}
						case "changeClanLeaderPage":
						{
							hb.append("<br>");
							hb.append("<table border=0 cellspacing=0 cellpadding=0 width=460");
							hb.append("<tr><td align=center>");
							hb.append(Html.fontColor("LEVEL", "Deseas hacer a otro jugador el lider del clan?"), "<br>");
							hb.append("Ingresa el nombre del jugador<br>");
							hb.append("<edit var=\"name\" width=120><br>");
							hb.append("<button value=\"Next\" action=\"bypass _bbsclan changeClanLeader $name\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
							hb.append("</td></tr>");
							hb.append("</table>");
							break;
						}
						case "increaseClanLvlPage":
						{
							hb.append(increaseClanLvlPage(player));
							break;
						}
						case "changeClanLeader":
						{
							String name = st.nextToken();
							if (!st.hasMoreTokens())
							{
								hb.append("No has ingresado el nombre del nuevo leader!");
							}
							else if (!player.isClanLeader())
							{
								hb.append("Solo el lider de clan puede ejecutar esta accion");
							}
							else if (player.getName().equalsIgnoreCase(name))
							{
								hb.append("Te estas asignando a ti mismo el nuevo leader?");
							}
							else
							{
								final L2Clan clan = player.getClan();
								final L2ClanMember member = clan.getClanMember(name);
								
								if (member == null)
								{
									hb.append("No existe ese usuario en tu clan");
								}
								else if (!member.isOnline())
								{
									hb.append("El usuario no esta online");
								}
								
								clan.setLeader(member);
								hb.append("Felicitaciones, ahora el nuevo leader es " + name);
							}
							
							break;
						}
						case "increaseClanLvl":
						{
							if (increaseClanLevel(player))
							{
								player.broadcastPacket(new MagicSkillUser(player, player, 5103, 1, 0, 0));
								hb.append("Felicitaciones, has subido con exito tu clan");
								hb.append("<button value=\"Back\" action=\"bypass _bbsclan myClan\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, ">");
							}
							else
							{
								hb.append("Lo siento, no cumples con los requisitos!");
								hb.append("<button value=\"Back\" action=\"bypass _bbsclan myClan\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, ">");
							}
							break;
						}
						case "learnSkill":
						{
							break;
						}
					}
					
					hb.append("</td>");
					// index end --------------------------------------------------------
					hb.append("</tr>");
					hb.append("</table>");
					break;
				}
				case "disolveClan":
				{
					hb.append(dissolveClan(player));
					break;
				}
				case "createClan":
				{
					if (!st.hasMoreTokens())
					{
						hb.append("Ingresa un nombre por favor!");
					}
					else
					{
						String clanName = st.nextToken();
						if (ClanTable.getInstance().createClan(player, clanName) != null)
						{
							hb.append("Tu clan fue creado con exito");
						}
						else
						{
							hb.append("No se pudo crear el clan");
						}
						
						hb.append("<button value=Back action=\"bypass _bbsclan myClan\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
					}
					break;
				}
				case "createAlly":
				{
					if (!st.hasMoreTokens())
					{
						hb.append("Enter a name please!");
					}
					else
					{
						String allyName = st.nextToken();
						hb.append(createAlly(player, allyName));
					}
					break;
				}
				case "eject":
				{
					if (!st.hasMoreTokens())
					{
						// bypass
						break;
					}
					
					L2ClanMember member = player.getClan().getClanMember(st.nextToken());
					
					hb.append(ejectMember(player, member));
					
					break;
				}
			}
			
			hb.append("</center>");
			hb.append(Html.END);
			
			sendCommunity(ph, hb.toString());
			return true;
		}
		return false;
	}
	
	// XXX HTML LIST CLAN -----------------------------------------------------------------------------
	
	private static String bbsMyClanLeft()
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		hb.append("<table width=100 border=0 cellspacing=0 cellpadding=0>");// marco top -> start
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackLeft, 16, 22), "</td>");
		hb.append("<td width=68 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 68, 22), "</td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackRight, 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<br>");
		hb.append("<table border=0 cellspacing=0 cellpadding=2 width=100 height=22>");
		hb.append("<tr><td width=93 height=22 align=center><button value=\"Members\" action=\"bypass _bbsclan myClan membersPage\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td></tr>");
		hb.append("<tr><td width=93 align=center><button value=\"Create Ally\" action=\"bypass _bbsclan myClan createAllyPage\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td></tr>");
		hb.append("<tr><td width=93 align=center><button value=\"Disolve Ally\" action=\"bypass _bbsclan myClan disolveClanPage\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td></tr>");
		hb.append("<tr><td width=93 align=center><button value=\"Disolve Clan\" action=\"bypass _bbsclan myClan disolveAllyPage\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td></tr>");
		hb.append("<tr><td width=93 align=center><button value=\"Change Leader\" action=\"bypass _bbsclan myClan changeClanLeaderPage\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td></tr>");
		hb.append("<tr><td width=93 align=center><button value=\"Increase Lvl\" action=\"bypass _bbsclan myClan increaseClanLvlPage\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td></tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	private static String bbsInfoMembers(PlayerHolder ph, int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<br>");
		hb.append("<table><tr><td>", Html.fontColor("FF8000", "MEMBERS"), "</td></tr></table>");
		
		hb.append("<table border=0 cellspacing=0 cellpadding=0 width=460 height=22");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackLeft, 16, 22), "</td>");
		hb.append("<td fixwidth=98 align=center><button value=Name width=98 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=30 align=center><button value=Lvl width=30 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=50 align=center><button value=Online width=50 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=50 align=center><button value=Rebirth width=50 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=25 align=center><button value=aio width=25 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=25 align=center><button value=vip width=25 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=100 align=center><button value=Class width=100 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=50 align=center><button value=Eject width=50 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td width=16 valign=top align=center>", Html.image(L2UI_CH3.FrameBackRight, 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		int MAX_PER_PAGE = 10;
		int searchPage = MAX_PER_PAGE * (page - 1);
		int count = 0;
		int color = 0;
		
		for (L2ClanMember member : ph.getInstance().getClan().getMembers())
		{
			if (member == null)
			{
				continue;
			}
			// min
			if (count < searchPage)
			{
				count++;
				continue;
			}
			// max
			if (count >= (searchPage + MAX_PER_PAGE))
			{
				continue;
			}
			
			// 50
			hb.append("<table width=460 ", (color % 2) == 0 ? "bgcolor=000000 " : "", "border=0 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td fixwidth=114 align=center>", member.getName(), "</td>");
			hb.append("<td fixwidth=30 align=center>", getColorLevel(member.getLevel()), "</td>");
			hb.append("<td fixwidth=50 align=center>", member.isOnline() ? Html.fontColor("3CFF00", "online") : Html.fontColor("FF0000", "offline"), "</td>");
			hb.append("<td fixwidth=50 align=center>", ph.getRebirth(), "</td>");
			hb.append("<td fixwidth=25 align=center>", ph.isAio(), "</td>");
			hb.append("<td fixwidth=25 align=center>", ph.isVip(), "</td>");
			hb.append("<td fixwidth=100 align=center>", ClassId.values()[member.getClassId()], "</td>");
			hb.append("<td fixwidth=66 align=center><button action=\"bypass _bbsclan eject ", member.getName(), "\" width=16 height=16 back=", L2UI.bbs_delete_down, " fore=", L2UI.bbs_delete, "></td>");
			hb.append("</tr>");
			hb.append("</table>");
			count++;
			color++;
		}
		
		hb.append("<br>");
		hb.append("<table>");
		hb.append("<tr>");
		int currentPage = 1;
		int size = ph.getInstance().getClan().getMembersCount();
		for (int i = 0; i < size; i++)
		{
			if ((i % MAX_PER_PAGE) == 0)
			{
				if (currentPage == page)
				{
					hb.append("<td width=20>", Html.fontColor("LEVEL", currentPage), "</td>");
				}
				else
				{
					hb.append("<td width=20><a action=\"bypass _bbsclan membersPage ", currentPage, "\">", currentPage, "</a></td>");
				}
				currentPage++;
			}
		}
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	private static String topInfoClan()
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<table width=460 height=22 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center>", Html.image(L2UI_CH3.FrameBackLeft, 16, 22), "</td>");
		hb.append("<td fixwidth=84 align=center><button value=Name width=84 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=30 align=center><button value=Lvl width=30 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=90 align=center><button value=Leader width=90 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=40 align=center><button value=Members width=40 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=100 align=center><button value=Ally width=100 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td fixwidth=84 align=center><button value=Castle width=84 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td width=16 valign=top align=center>", Html.image(L2UI_CH3.FrameBackRight, 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	private static String bodyInfoClan(L2Clan clan, int color)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<table width=460 height=22 ", (color % 2) == 0 ? "bgcolor=000000 " : "", "border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td fixwidth=16 height=22 align=center>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td>");
		hb.append("<td fixwidth=84 height=22 align=center>", Html.fontColor("FF8000", clan.getName()), "</td>");
		hb.append("<td fixwidth=30 align=center>", getClanColorLevel(clan.getLevel()), "</td>");
		hb.append("<td fixwidth=90 align=center>", clan.getLeader().getName(), "</td>");
		hb.append("<td fixwidth=40 align=center>", clan.getMembersCount(), "</td>");
		hb.append("<td fixwidth=100 align=center>", clan.getAllyId() > 0 ? clan.getAllyName() : "No Ally", "</td>");
		hb.append("<td fixwidth=100 align=center>", clan.hasCastle() ? CastleManager.getInstance().getCastleById(clan.getClanId()).getName() : "No Castle", "</td>");
		// hb.append("<td fixwidth=75 align=center><button value=Info action=\"bypass _bbsclan infor " + clan.getClanId() + "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	private static String bodyInfoNoClan(int color)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<table width=460 ", (color % 2) == 0 ? "bgcolor=000000 " : "", "border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td fixwidth=100 align=center>-</td>");
		hb.append("<td fixwidth=30 align=center>-</td>");
		hb.append("<td fixwidth=90 align=center>-</td>");
		hb.append("<td fixwidth=40 align=center>-</td>");
		hb.append("<td fixwidth=100 align=center>-</td>");
		hb.append("<td fixwidth=100 align=center>-</td>");
		// hb.append("<td fixwidth=75 align=center><button value=Info action=\"bypass _bbsclan infor " + clan.getClanId() + "\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, "></td>");
		hb.append("</tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	private static String bbsListClan(int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		hb.append(topInfoClan());
		
		int MAX_PER_PAGE = 15;
		int searchPage = MAX_PER_PAGE * (page - 1);
		int count = 0;
		int color = 0;
		
		List<L2Clan> clansList = new ArrayList<>();
		
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			clansList.add(clan);
		}
		
		// se ordena el listado segun sus scores
		// Collections.sort(clansList, (p1, p2) -> new Integer(p1.getReputationScore()).compareTo(new Integer(p2.getReputationScore())));
		
		for (L2Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan == null)
			{
				continue;
			}
			// min
			if (count < searchPage)
			{
				count++;
				continue;
			}
			// max
			if (count >= (searchPage + MAX_PER_PAGE))
			{
				continue;
			}
			
			hb.append(bodyInfoClan(clan, color));
			hb.append(Html.image(L2UI.SquareGray, 460, 1));
			color++;
			count++;
		}
		
		int currentPage = 1;
		int size = ClanTable.getInstance().getClans().length;
		
		hb.append("<br>");
		hb.append("<table>");
		hb.append("<tr>");
		for (int i = 0; i < size; i++)
		{
			if ((i % MAX_PER_PAGE) == 0)
			{
				if (currentPage == page)
				{
					hb.append("<td width=20>", Html.fontColor("LEVEL", currentPage), "</td>");
				}
				else
				{
					hb.append("<td width=20><a action=\"bypass _bbsclan listClans ", currentPage, "\">", currentPage, "</a></td>");
				}
				
				currentPage++;
			}
		}
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	private static String increaseClanLvlPage(L2PcInstance player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		hb.append("Requiere:<br1>");
		
		switch (player.getClan().getLevel())
		{
			case 0:
				hb.append(Html.fontColor("LEVEL", "SP: "), "30.000", " - ");
				hb.append(Html.fontColor("LEVEL", "Adena: "), "650.000");
				break;
			case 1:
				hb.append(Html.fontColor("LEVEL", "SP: "), "150.000", " - ");
				hb.append(Html.fontColor("LEVEL", "Adena: "), "2.500.000");
				break;
			case 2:
				hb.append(Html.fontColor("LEVEL", "SP: "), "500.000", " - ");
				hb.append(Html.fontColor("LEVEL", "Proof of Blood: "), "1");
				break;
			case 3:
				hb.append(Html.fontColor("LEVEL", "SP: "), "1.400.000", " - ");
				hb.append(Html.fontColor("LEVEL", "Proof of Alliance: "), "1");
				break;
			case 4:
				hb.append(Html.fontColor("LEVEL", "SP: 3.500.000"), " - ");
				hb.append(Html.fontColor("LEVEL", "Proof of Aspiration: "), "1");
				break;
			default:
				hb.append(Html.fontColor("LEVEL", "Maximo Nivel"));
		}
		
		hb.append("<button value=\"Increase Level\" action=\"bypass _bbsclan myClan increaseClanLvl\" width=75 height=21 back=", L2UI_CH3.Btn1_normalOn, " fore=", L2UI_CH3.Btn1_normal, ">");
		
		return hb.toString();
	}
	
	private static String dissolveClan(L2PcInstance player)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		if (!player.isClanLeader())
		{
			hb.append("Only the leader can dissolve the clan!");
		}
		else if (player.getClan().getAllyId() != 0)
		{
			hb.append("You can not dissolve a clan by being in an alliance!");
		}
		else if (player.getClan().isAtWar() != 0)
		{
			hb.append("You can not dissolve the clan by being in a war!");
		}
		
		else if (player.getClan().hasCastle() || player.getClan().getHasHideout() != 0)
		{
			hb.append("You can not dissolve the clan if you have a Clan Hall or a Castle!");
		}
		else if (player.getClan().getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			hb.append("Your clan is already in the process of dissolution!");
		}
		else
		{
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId()))
				{
					hb.append("Your clan has requested to participate in a castle siege!");
				}
			}
			
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (FortSiegeManager.getInstance().checkIsRegistered(player.getClan(), fort.getFortId()))
				{
					hb.append("Your clan has requested to participate in a fort siege!");
				}
			}
			
			if (Config.ALT_CLAN_DISSOLVE_DAYS > 0)
			{
				player.getClan().setDissolvingExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L));
				player.getClan().updateClanInDB();
				
				ClanTable.getInstance().scheduleRemoveClan(player.getClan().getClanId());
				hb.append("Your clan began its destruction process!");
			}
			else
			{
				ClanTable.getInstance().destroyClan(player.getClan().getClanId());
				hb.append("Your clan has been destroyed successfully!");
			}
			
			// The clan leader should take the XP penalty of a full death.
			player.deathPenalty(false);
		}
		
		return hb.toString();
	}
	
	private static String createAlly(L2PcInstance player, String allyName)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		if (!player.isClanLeader())
		{
			hb.append("Only the leader can create an alliance");
		}
		else if (player.getClan().getAllyId() != 0)
		{
			hb.append("You already have an alliance");
		}
		else if (player.getClan().getLevel() < 5)
		{
			hb.append("Your clan must be at least level 5 to create an alliance");
		}
		else if (player.getClan().getAllyPenaltyExpiryTime() > System.currentTimeMillis())
		{
			hb.append("You are penalized for the creation of alliances");
		}
		else if (player.getClan().getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			hb.append("You can not create alliances if your clan is dissolving");
		}
		else if (ClanTable.getInstance().isAllyExists(allyName))
		{
			hb.append("The name of the alliance already exists");
		}
		else
		{
			for (Castle castle : CastleManager.getInstance().getCastles())
			{
				if (SiegeManager.getInstance().checkIsRegistered(player.getClan(), castle.getCastleId()))
				{
					hb.append("Your clan has requested to participate in a castle siege!");
					return hb.toString();
				}
			}
			
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (FortSiegeManager.getInstance().checkIsRegistered(player.getClan(), fort.getFortId()))
				{
					hb.append("Your clan has requested to participate in a fort siege!");
					return hb.toString();
				}
			}
			
			player.getClan().setAllyId(player.getClan().getClanId());
			player.getClan().setAllyName(allyName);
			player.getClan().setAllyPenaltyExpiryTime(0, 0);
			player.getClan().updateClanInDB();
			player.sendPacket(new UserInfo(player));
			
			hb.append("The alliance", allyName, " it was created successfully!");
		}
		
		return hb.toString();
	}
	
	private static String ejectMember(L2PcInstance player, L2ClanMember member)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		if (member == null)
		{
			// posible bypass
			return hb.toString();
		}
		
		if (player.getClan().getClanMember(member.getName()) == null)
		{
			hb.append(Html.fontColor("LEVEL", "This character is not in your clan"));
			return hb.toString();
		}
		
		// no te puedes echar a ti mismo
		if (member.getName().equals(player.getName()))
		{
			hb.append(Html.fontColor("LEVEL", "You can not kick yourself out of the clan!"));
			return hb.toString();
		}
		
		// chequeamos los privilegios del player
		if (!player.isClanLeader())
		{
			hb.append(Html.fontColor("LEVEL", "You do not have enough privileges!"));
			return hb.toString();
		}
		
		L2Clan clan = player.getClan();
		
		// proceso de actualizacion para el clan --------------------------
		// this also updates the database
		clan.removeClanMember(member.getName(), System.currentTimeMillis() + (Config.ALT_CLAN_JOIN_DAYS * 86400000L));
		clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + (Config.ALT_CLAN_JOIN_DAYS * 86400000L));
		clan.updateClanInDB();
		
		clan.broadcastClanStatus(); // refresh clan tab
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(member.getName()));
		
		clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(member.getName()));
		player.sendPacket(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_EXPELLING_CLAN_MEMBER);
		player.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
		
		if (member.isOnline())
		{
			member.getPlayerInstance().sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
		}
		
		hb.append("The player ", Html.fontColor("LEVEL", member.getName()), " was successfully cast!<br>");
		hb.append("<button value=Back action=\"bypass _bbsclan myClan\" width=93 height=22 back=", L2UI_CH3.bigbutton_down, " fore=", L2UI_CH3.bigbutton, "></td>");
		
		return hb.toString();
	}
	
	// XXX MISC ---------------------------------------------------------------------------------------
	private static String bbsHead(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(marcButton(bypass));
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append(newMenu("LIST OF CLANS", "listClans"));
		hb.append(newMenu("MY CLAN", "myClan"));
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(marcButton(bypass));
		
		hb.append("<br>");
		return hb.toString();
	}
	
	private static String newMenu(String butonName, String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append("<td><button value=\"", butonName, "\" action=\"bypass _bbsclan ", bypass, "\" width=100 height=32 back=", L2UI_CH3.br_party1_back2, " fore=", L2UI_CH3.br_party1_back2, "></td>");
		return hb.toString();
	}
	
	private static String marcButton(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td height=2>", Html.image(bypass.equals("listClans") ? L2UI_CH3.br_bar2_mp : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
		hb.append("<td height=2>", Html.image(bypass.equals("myClan") ? L2UI_CH3.br_bar2_mp : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		return hb.toString();
	}
	
	private static String getIconStatus(boolean status)
	{
		if (status)
		{
			return Html.image(L2UI_CH3.QuestWndInfoIcon_5, 16, 16);
		}
		return "X";
	}
	
	/**
	 * Assign a color according to the level of the character
	 * @param  lvl
	 * @return
	 */
	private static String getColorLevel(int lvl)
	{
		HtmlBuilder hb = new HtmlBuilder();
		
		if ((lvl >= 20) && (lvl < 40))
		{
			hb.append(Html.fontColor("LEVEL", lvl)); // yellow
		}
		else if ((lvl >= 40) && (lvl < 76))
		{
			hb.append(Html.fontColor("9A5C00", lvl)); // dark orange
		}
		else if (lvl >= 76)
		{
			hb.append(Html.fontColor("FF0000", lvl));// red
		}
		else
		{
			hb.append(lvl);
		}
		
		return hb.toString();
	}
	
	/**
	 * A color is assigned according to the level of the clan
	 * @param  lvl
	 * @return
	 */
	private static String getClanColorLevel(int lvl)
	{
		HtmlBuilder hb = new HtmlBuilder();
		
		if ((lvl >= 2) && (lvl < 4))
		{
			hb.append(Html.fontColor("LEVEL", lvl)); // yellow
		}
		else if ((lvl >= 5) && (lvl < 7))
		{
			hb.append(Html.fontColor("9A5C00", lvl)); // dark orange
		}
		else if (lvl >= 7)
		{
			hb.append(Html.fontColor("FF0000", lvl));// red
		}
		else
		{
			hb.append(lvl);
		}
		
		return hb.toString();
	}
	
	private static boolean increaseClanLevel(L2PcInstance player)
	{
		L2Clan clan = player.getClan();
		
		boolean increaseClanLevel = false;
		
		switch (clan.getLevel())
		{
			case 0:
				// upgrade to 1
				if ((player.getSp() >= 30000) && (player.getInventory().getAdena() >= 650000))
				{
					player.getInventory().reduceAdena("ClanLvl", 650000, null, null);
					player.getStat().setSp(player.getSp() - 30000);
					increaseClanLevel = true;
				}
				else
				{
					player.sendMessage("Upgrade Clan lvl 1 need:");
					player.sendMessage("Skill Point: 30000");
					player.sendMessage("Adena: 650000");
				}
				break;
			case 1:
				// upgrade to 2
				if ((player.getSp() >= 150000) && (player.getInventory().getAdena() >= 2500000))
				{
					player.getInventory().reduceAdena("ClanLvl", 2500000, null, null);
					player.getStat().setSp(player.getSp() - 150000);
					increaseClanLevel = true;
				}
				else
				{
					player.sendMessage("Upgrade Clan lvl 2 need:");
					player.sendMessage("Skill Point: 150000");
					player.sendMessage("Adena: 2500000");
				}
				break;
			case 2:
				// upgrade to 3
				if ((player.getSp() >= 500000) && (player.getInventory().getItemByItemId(1419) != null))
				{
					// itemid 1419 == proof of blood
					player.getInventory().destroyItemByItemId("ClanLvl", 1419, 1, player, null);
					player.getStat().setSp(player.getSp() - 500000);
					increaseClanLevel = true;
				}
				else
				{
					player.sendMessage("Upgrade Clan lvl 3 need:");
					player.sendMessage("Skill Point: 500000");
					player.sendMessage("Proof of Blood: 1");
				}
				break;
			case 3:
				// upgrade to 4
				if ((player.getSp() >= 1400000) && (player.getInventory().getItemByItemId(3874) != null))
				{
					// itemid 3874 == proof of alliance
					player.getInventory().destroyItemByItemId("ClanLvl", 3874, 1, player, null);
					player.getStat().setSp(player.getSp() - 1400000);
					increaseClanLevel = true;
				}
				else
				{
					player.sendMessage("Upgrade Clan lvl 4 need:");
					player.sendMessage("Skill Point: 1400000");
					player.sendMessage("Proof of Alliance: 1");
				}
				break;
			case 4:
				// upgrade to 5
				if ((player.getSp() >= 3500000) && (player.getInventory().getItemByItemId(3870) != null))
				{
					// itemid 3870 == proof of aspiration
					player.getInventory().destroyItemByItemId("ClanLvl", 3870, 1, player, null);
					player.getStat().setSp(player.getSp() - 3500000);
					increaseClanLevel = true;
				}
				// TODO tiene mas levels interlude no??
				else
				{
					player.sendMessage("Upgrade Clan lvl 5 need:");
					player.sendMessage("Skill Point: 3500000");
					player.sendMessage("Proof of Aspiration: 1");
				}
				break;
		}
		
		if (increaseClanLevel)
		{
			// the player should know that he has less sp now :p
			StatusUpdate su = new StatusUpdate(player);
			su.addAttribute(StatusUpdate.SP, player.getSp());
			player.sendPacket(su);
			player.sendPacket(new ItemList(player, false));
			clan.changeLevel(clan.getLevel() + 1);
		}
		else
		{
			player.sendPacket(new SystemMessage(275)); // TODO missing system message
		}
		
		return increaseClanLevel;
	}
}
