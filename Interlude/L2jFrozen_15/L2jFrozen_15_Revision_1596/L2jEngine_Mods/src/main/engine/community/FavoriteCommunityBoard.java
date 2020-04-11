package main.engine.community;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.skills.Stats;

import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.enums.MaestriaType;
import main.enums.MathType;
import main.holders.objects.CharacterHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;
import main.util.builders.html.L2UI;
import main.util.builders.html.L2UI_CH3;

/**
 * @author fissban
 */
public class FavoriteCommunityBoard extends AbstractMod
{
	public FavoriteCommunityBoard()
	{
		registerMod(ConfigData.ENABLE_BBS_FAVORITE);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				loadValuesFromDb();
				readAllRebirths();
				break;
			
			case END:
				break;
		}
	}
	
	@Override
	public boolean onCommunityBoard(PlayerHolder ph, String command)
	{
		// bbsgetfav
		if (command.startsWith("_bbsgetfav"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			// bbsgetfav
			st.nextToken();
			// bypass
			String bypass = st.hasMoreTokens() ? st.nextToken() : "main";
			
			HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
			hb.append(Html.START);
			hb.append("<br>");
			hb.append("<center>");
			hb.append(bbsHead(bypass));
			
			switch (bypass)
			{
				case "main":
				{
					hb.append(bbsBodyMain(ph));
					break;
				}
				case "maestrias":
				{
					// hb.append(bbsBodyStats(player));
					break;
				}
				case "stats":
				{
					// If we have more tokens means that the player is adding points to the stats
					if (st.countTokens() == 2)
					{
						// Get the stat we will increase
						Stats stat = Enum.valueOf(Stats.class, st.nextToken());
						// At this point we always know that there will be another token (add or sub)
						String event = st.nextToken();
						
						switch (event)
						{
							case "add":
							{
								if (ph.getFreeStatsPoints() <= 1)
								{
									UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[REBIRTH]", "You do not have enough points.!");
									break;
								}
								
								ph.addStatsPoints(stat, 1);
								// The number of player points is decreased by 1
								ph.modifyFreeStatsPoints(MathType.SUB, 1);
								break;
							}
							case "sub":
							{
								// ObjectsData.get(PlayerHolder.class,player).addCustomStat(stat, -1);
								break;
							}
						}
						
						// The new values are saved
						// STATS
						setValueDB(ph, stat.name(), ph.getStatPoints(stat) + "");
						// POINTS
						setValueDB(ph, "stats", ph.getFreeStatsPoints() + "");
						// The client is updated with the new info.
						ph.getInstance().broadcastUserInfo();
					}
					
					hb.append(bbsBodyPanelStats(ph));
					
					break;
				}
				case "rebirth":
				{
					if (!st.hasMoreTokens())
					{
						hb.append("<br><br><br><br>", Html.fontColor("LEVEL", "Deseas renacer???"), "<br>");
						hb.append("<td><button value=\"RENACER\" action=\"bypass _bbsgetfav;rebirth;yes\" width=75 height=22 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.Btn1_normal></td>");
					}
					else
					{
						hb.append(bbsBodyRebirth(ph));
					}
					
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
	
	@Override
	public double onStats(Stats stat, CharacterHolder character, double value)
	{
		if (!Util.areObjectType(L2PlayableInstance.class, character))
		{
			return value;
		}
		
		switch (stat)
		{
			case STAT_STR:
			case STAT_CON:
			case STAT_DEX:
			case STAT_INT:
			case STAT_WIT:
			case STAT_MEN:
				value += ObjectData.get(PlayerHolder.class, character.getInstance().getActingPlayer()).getStatPoints(stat);
			default:
				break;
		}
		
		return value;
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (!Util.areObjectType(L2Attackable.class, victim) || (killer.getActingPlayer() == null))
		{
			return;
		}
		
		if (killer.getInstance().getLevel() == ExperienceData.getInstance().getMaxLevel())
		{
			UtilMessage.sendCreatureMsg(killer, Say2.TELL, "[Engine]", "Max Level!");
		}
	}
	
	// HTML ------------------------------------------------------------------------
	
	private String marcButton(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		if (bypass != null)
		{
			hb.append("<table border=0 cellspacing=0 cellpadding=0>");
			hb.append("<tr>");
			hb.append("<td height=2>", Html.image(bypass.equals("main") ? L2UI_CH3.br_bar2_mp : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
			hb.append("<td height=2>", Html.image(bypass.equals("rebirth") ? L2UI_CH3.br_bar2_mp : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
			hb.append("<td height=2>", Html.image(bypass.equals("stats") ? L2UI_CH3.br_bar2_mp : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
			hb.append("<td height=2>", Html.image(bypass.equals("maestrias") ? L2UI_CH3.br_bar2_mp : L2UI_CH3.br_bar1back_mp, 100, 1), "</td>");
			hb.append("</tr>");
			hb.append("</table>");
		}
		else
		{
			hb.append(Html.image(L2UI.SquareGray, 506, 1));
		}
		return hb.toString();
	}
	
	private String bbsHead(String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		hb.append(marcButton(bypass));
		hb.append("<table border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append(newMenu("MAIN", "main"));
		hb.append(newMenu("RENACER", "rebirth"));
		hb.append(newMenu("STATS", "stats"));
		hb.append(newMenu("MAESTRIAS", "maestrias"));
		hb.append("</tr>");
		hb.append("</table>");
		hb.append(marcButton(bypass));
		
		hb.append("<br>");
		return hb.toString();
	}
	
	private String bbsBodyRebirth(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		// Check the amount of rebirths of the character and prevent it from doing more than desired.
		if (ph.getRebirth() >= ConfigData.MAX_REBIRTH)
		{
			hb.append("<br><br><br><br>", Html.fontColor("LEVEL", "You can not be reborn any more times!"), "<br>");
			hb.append("Remember that the maximum rebirths is ", ConfigData.MAX_REBIRTH);
			return hb.toString();
		}
		// Check that the character is at the highest level.
		if (ph.getInstance().getLevel() < (ExperienceData.getInstance().getMaxLevel() - 1))
		{
			hb.append("<br><br><br><br>", Html.fontColor("LEVEL", "You have not yet reached the maximum level!"), "<br>");
			hb.append("Remember that the level to be reborn is ", ExperienceData.getInstance().getMaxLevel() - 1);
			return hb.toString();
		}
		
		// It is checked that you have the NCoins to pay for the rebirth.
		int priceForNextRebirth = (int) (ConfigData.PRICE_FOR_REBIRTH * (ph.getRebirth() + 1) * ConfigData.MUL_PRICE_PER_REBIRTH);
		
		if (ph.getInstance().getInventory().getAdena() < priceForNextRebirth)
		{
			hb.append("<br><br><br><br>", Html.fontColor("LEVEL", "You have not managed to gather enough Adena to complete the rebirth!"), "<br>");
			return hb.toString();
		}
		
		ph.getInstance().getInventory().reduceAdena("", priceForNextRebirth, null, null);
		
		// If we do not have more checks we do the rebirth xD
		ph.getInstance().removeExpAndSp(ph.getInstance().getExp() - ExperienceData.getInstance().getExpForLevel(ConfigData.LVL_REBIRTH), 0);
		
		if (ph.getRebirth() == 0)
		{
			initStatsInDB(ph);
		}
		
		// Increases the amount of character rebirths
		ph.modifyRebirth(MathType.ADD, 1);
		// modifiy memory
		ph.modifyFreeStatsPoints(MathType.ADD, ConfigData.STAT_POINT_PER_REBIRTH);
		ph.modifyMasteryPoints(MathType.ADD, ConfigData.MASTERY_POINT_PER_REBIRTH);
		// Save the value in the DB
		setValueDB(ph, "rebirth", ph.getRebirth() + ""); // Rebirth
		setValueDB(ph, "stats", ph.getFreeStatsPoints() + "");// Stats
		setValueDB(ph, "MaestriaPoints", ph.getMasteryPoints() + ""); // Maestrias points
		for (MaestriaType m : MaestriaType.values()) // Maestrias level
		{
			setValueDB(ph, "MaestriaLevel" + m, ph.getMaestriaLevel(m) + "");
		}
		
		// Send message about the new update
		hb.append("<br><br><br><br>", Html.fontColor("LEVEL", "Congratulations, you have successfully rebirth!"), "<br>");
		hb.append("Do not forget to add your points and improve your masters<br>");
		// TODO Could you show the points you won?
		return hb.toString();
	}
	
	public String bbsBodyMain(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<br>");
		hb.append("<center>");
		hb.append("Bienvenido ", Html.fontColor("LEVEL", ph.getName()), " al sistema de rebirth.<br>");
		hb.append("Si has logrado llegar al nivel ", Html.fontColor("LEVEL", ExperienceData.getInstance().getMaxLevel() - 1), ", estas listo para poder renacer<br>");
		hb.append("y convertirte en un guerrero mas poderoso....<br>");
		hb.append("quizas hasta podrias alcanzar el poder de un dios!<br>");
		hb.append("<br>");
		hb.append("Actualmente tienes ", Html.fontColor("LEVEL", ph.getRebirth()), " rebirths y podras renacer ", Html.fontColor("LEVEL", ConfigData.MAX_REBIRTH), " veces.<br>");
		hb.append("<br>");
		hb.append("Con cada rebirth ganaras:<br>");
		hb.append("* ", Html.fontColor("LEVEL", ConfigData.STAT_POINT_PER_REBIRTH), " que podras sumarlos a los stas que gustes.<br>");
		hb.append("* ", Html.fontColor("LEVEL", ConfigData.MASTERY_POINT_PER_REBIRTH), " que podras mejorar tu arbol de maestrias.<br>");
		return hb.toString();
	}
	
	private static String bbsBodyPanelStats(PlayerHolder ph)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		
		hb.append("<br>");
		
		hb.append("<table bgcolor=000000 height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 16, 22), "</td>");
		hb.append("<td width=250 align=center height=22><button value=\"EXTRA POINTS: ", ph.getFreeStatsPoints(), "\" width=250 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 16, 22), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<br>");
		
		hb.append("<table height=22 width=282 border=0 cellspacing=1 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=91 align=center>", Html.fontColor("LEVEL", "STAT"), "</td>");
		hb.append("<td width=125 align=center>", Html.fontColor("LEVEL", "POINTS"), "</td>");
		hb.append("<td width=66 align=center>", Html.fontColor("LEVEL", "ACTION"), "</td>");
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<br>");
		
		// MISC -----------------------------------------------------------------------------------------------
		String TABLE_START = "<table width=282 border=0 cellspacing=1 cellpadding=0>";
		String TABLE_END = "</table>";
		// STR ------------------------------------------------------------------------------------------------
		hb.append(tableStat("STR", ph.getInstance().getStat().getSTR(), "_bbsgetfav;stats;STAT_STR;add"));
		hb.append(TABLE_START);
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "P Atk: "), ph.getInstance().getStat().getPAtk(null), "</td></tr>");
		hb.append(TABLE_END);
		// DEX ------------------------------------------------------------------------------------------------
		hb.append(tableStat("DEX", ph.getInstance().getStat().getDEX(), "_bbsgetfav;stats;STAT_DEX;add"));
		hb.append(TABLE_START);
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "Atk Spd: "), ph.getInstance().getStat().getPAtkSpd(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "Accuracy: "), ph.getInstance().getStat().getAccuracy(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "Evasion: "), ph.getInstance().getStat().getEvasionRate(null), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "P Critical Rate: "), ph.getInstance().getStat().getCriticalHit(null, null), "</td></tr>");
		hb.append(TABLE_END);
		// CON ------------------------------------------------------------------------------------------------
		hb.append(tableStat("CON", ph.getInstance().getStat().getCON(), "_bbsgetfav;stats;STAT_CON;add"));
		hb.append(TABLE_START);
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "MaxHp: "), ph.getInstance().getStat().getMaxHp(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "MaxCp: "), ph.getInstance().getStat().getMaxCp(), "</td></tr>");
		hb.append(TABLE_END);
		// INT ------------------------------------------------------------------------------------------------
		hb.append(tableStat("INT", ph.getInstance().getStat().getINT(), "_bbsgetfav;stats;STAT_INT;add"));
		hb.append(TABLE_START);
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "M Atk: "), ph.getInstance().getStat().getMAtk(null, null), "</td></tr>");
		hb.append(TABLE_END);
		// WIT ------------------------------------------------------------------------------------------------
		hb.append(tableStat("WIT", ph.getInstance().getStat().getWIT(), "_bbsgetfav;stats;STAT_WIT;add"));
		hb.append(TABLE_START);
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "M Spd: "), ph.getInstance().getStat().getMAtkSpd(), "</td></tr>");
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "M Critical Rate: "), ph.getInstance().getStat().getMCriticalHit(null, null), "</td></tr>");
		hb.append(TABLE_END);
		
		// MEN ------------------------------------------------------------------------------------------------
		hb.append(tableStat("MEN", ph.getInstance().getStat().getMEN(), "_bbsgetfav;stats;STAT_MEN;add"));
		hb.append(TABLE_START);
		hb.append("<tr><td width=20 height=16>", Html.image(L2UI_CH3.ps_sizecontrol2_over, 16, 16), "</td><td width=230>", Html.fontColor("LEVEL", "MaxMp: "), ph.getInstance().getStat().getMaxMp(), "</td></tr>");
		hb.append(TABLE_END);
		
		return hb.toString();
	}
	
	/**
	 * Used only in <b>bbsBodyPanelStats</b> to shrink text
	 * @param  statName
	 * @param  statPoint
	 * @param  bypass
	 * @return
	 */
	private static String tableStat(String statName, int statPoint, String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append("<table height=26 width=282 border=0 cellspacing=0 cellpadding=0>");
		hb.append("<tr>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 16, 22), "</td>");
		hb.append("<td width=75 align=center height=22><button value=", statName, " width=75 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td width=125 align=center height=22><button value=", statPoint, " width=125 height=22 back=", L2UI_CH3.FrameBackMid, " fore=", L2UI_CH3.FrameBackMid, "></td>");
		hb.append("<td width=16 valign=top align=center height=22>", Html.image(L2UI_CH3.FrameBackMid, 16, 22), "</td>");
		hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass ", bypass, "\" width=32 height=26 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		return hb.toString();
	}
	
	// MISC ------------------------------------------------------------------------
	private static String newMenu(String butonName, String bypass)
	{
		HtmlBuilder hb = new HtmlBuilder();
		hb.append("<td><button value=\"", butonName, "\" action=\"bypass _bbsgetfav;", bypass, "\" width=100 height=32 back=L2UI_CH3.br_party1_back2 fore=L2UI_CH3.br_party1_back2></td>");
		return hb.toString();
	}
	
	private void readAllRebirths()
	{
		for (PlayerHolder ph : ObjectData.getAll(PlayerHolder.class))
		{
			// I get the amount of rebirths of each character in the DB...
			int rebirth = getValueDB(ph.getObjectId(), "rebirth").getInt();
			// Don't has value in db
			if (rebirth == 0)
			{
				continue;
			}
			
			// Save the amount of rebirths in memory
			ph.modifyRebirth(MathType.SET, rebirth);
			
			try
			{
				// Obtain and save the number of masters points to be distributed
				// ph.setMasteryPoints(getValueDB(ph.getObjectId(), "MaestriaPoints").getInt());
				
				// for (MaestriaType m : MaestriaType.values()) // Maestrias level
				// {
				// ph.setMaestriaLevel(m, getValueDB(ph.getObjectId(), "MaestriaLevel" + m).getInt());
				// }
				
				// Obtain and save the number of masters points to be distributed
				ph.modifyFreeStatsPoints(MathType.SET, getValueDB(ph.getObjectId(), "stats").getInt());
				
				// Get the points that were added to each stat
				int stat_str = getValueDB(ph.getObjectId(), "STAT_STR").getInt();
				int stat_con = getValueDB(ph.getObjectId(), "STAT_CON").getInt();
				int stat_dex = getValueDB(ph.getObjectId(), "STAT_DEX").getInt();
				int stat_int = getValueDB(ph.getObjectId(), "STAT_INT").getInt();
				int stat_wit = getValueDB(ph.getObjectId(), "STAT_WIT").getInt();
				int stat_men = getValueDB(ph.getObjectId(), "STAT_MEN").getInt();
				// Save the points in each stat.
				ph.addStatsPoints(Stats.STAT_STR, stat_str);
				ph.addStatsPoints(Stats.STAT_CON, stat_con);
				ph.addStatsPoints(Stats.STAT_DEX, stat_dex);
				ph.addStatsPoints(Stats.STAT_INT, stat_int);
				ph.addStatsPoints(Stats.STAT_WIT, stat_wit);
				ph.addStatsPoints(Stats.STAT_MEN, stat_men);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void initStatsInDB(PlayerHolder ph)
	{
		setValueDB(ph, "STAT_STR", 0 + "");
		setValueDB(ph, "STAT_CON", 0 + "");
		setValueDB(ph, "STAT_DEX", 0 + "");
		setValueDB(ph, "STAT_INT", 0 + "");
		setValueDB(ph, "STAT_WIT", 0 + "");
		setValueDB(ph, "STAT_MEN", 0 + "");
	}
}
