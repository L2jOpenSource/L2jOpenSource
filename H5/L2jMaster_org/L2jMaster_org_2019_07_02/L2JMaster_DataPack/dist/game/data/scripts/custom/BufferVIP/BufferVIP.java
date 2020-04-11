/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.BufferVIP;

import static com.l2jserver.gameserver.util.Util.formatAdena;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.instancemanager.QuestManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PetInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.actor.stat.PcStat;
import com.l2jserver.gameserver.model.actor.stat.SummonStat;
import com.l2jserver.gameserver.model.actor.status.PcStatus;
import com.l2jserver.gameserver.model.actor.status.SummonStatus;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.olympiad.OlympiadManager;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.SetSummonRemainTime;
import com.l2jserver.gameserver.network.serverpackets.SetupGauge;

public class BufferVIP extends Quest
{
	private static final boolean DEBUG = false;
	
	private static void print(Exception e)
	{
		_log.warning(">>>" + e.toString() + "<<<");
		if (DEBUG)
		{
			e.printStackTrace();
		}
	}
	
	private static final String QUEST_LOADING_INFO = "BufferVIP";
	private static final int NPC_ID = 9997;
	
	private static final String TITLE_NAME = "Scheme Buffer";
	private static final boolean SCRIPT_RELOAD = false;
	private static final boolean SMART_WINDOW = false;
	private static final boolean ENABLE_BUFF_SECTION = true;
	private static final boolean ENABLE_SCHEME_SYSTEM = true;
	private static final boolean ENABLE_HEAL = true;
	// private static final boolean ENABLE_HEAL_IN_COMBAT = true;
	private static final boolean ENABLE_BUFFS = true;
	private static final boolean ENABLE_RESIST = true;
	private static final boolean ENABLE_SONGS = true;
	private static final boolean ENABLE_DANCES = true;
	private static final boolean ENABLE_CHANTS = true;
	private static final boolean ENABLE_OTHERS = true;
	private static final boolean ENABLE_SPECIAL = true;
	private static final boolean ENABLE_CUBIC = true;
	private static final boolean ENABLE_BUFF_REMOVE = false;
	private static final boolean ENABLE_BUFF_SET = true;
	private static final boolean BUFF_WITH_KARMA = true;
	private static final boolean FREE_BUFFS = false;
	private static final boolean TIME_OUT = false;
	private static final int TIME_OUT_TIME = 3;
	private static final int MIN_LEVEL = 1;
	// private static final int BUFF_REMOVE_PRICE = 1;
	// private static final int HEAL_PRICE = 1;
	// private static final int BUFF_PRICE = 1;
	// private static final int RESIST_PRICE = 1;
	// private static final int SONG_PRICE = 1;
	// private static final int DANCE_PRICE = 1;
	// private static final int CHANT_PRICE = 1;
	// private static final int OTHERS_PRICE = 1;
	// private static final int SPECIAL_PRICE = 1;
	// private static final int CUBIC_PRICE = 1;
	// private static final int BUFF_SET_PRICE = 1;
	// private static final int SCHEME_BUFF_PRICE = 30;
	private static final int SCHEMES_PER_PLAYER = 4;
	// private static final int CONSUMABLE_ID = 23003;
	private static final int MAX_SCHEME_BUFFS = Config.BUFFS_MAX_AMOUNT;
	private static final int MAX_SCHEME_DANCES = Config.DANCES_MAX_AMOUNT;
	
	private static final String SET_FIGHTER = "Fighter";
	private static final String SET_MAGE = "Mage";
	private static final String SET_ALL = "All";
	private static final String SET_NONE = "None";
	
	private String rebuildMainHtml(QuestState st)
	{
		String MAIN_HTML_MESSAGE = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>";
		String MESSAGE = "";
		int td = 0;
		String[] TRS =
		{
			"<tr><td height=25>",
			"</td>",
			"<td height=25>",
			"</td></tr>"
		};
		
		final String bottonA, bottonB, bottonC;
		if (st.getInt("Pet-On-Off") == 1)
		{
			bottonA = "Auto Buff Pet";
			bottonB = "Heal My Pet";
			bottonC = "Remove Pet Buffs";
			MAIN_HTML_MESSAGE += "<button value=\"Player Options\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " buffpet 0 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		else
		{
			bottonA = "Auto Buff";
			bottonB = "Heal";
			bottonC = "Remove Buffs";
			MAIN_HTML_MESSAGE += "<button value=\"Pet Options\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " buffpet 1 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		
		if (ENABLE_BUFF_SECTION)
		{
			if (ENABLE_BUFFS)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_buffs 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
				
			}
			if (ENABLE_RESIST)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Resist\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_resists 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
			if (ENABLE_SONGS)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Songs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_songs 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
			if (ENABLE_DANCES)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Dances\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_dances 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
			if (ENABLE_CHANTS)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Chants\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_chants 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
			if (ENABLE_SPECIAL)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Special\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_special 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
			if (ENABLE_OTHERS)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"Others\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_others 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
		}
		
		if (ENABLE_CUBIC)
		{
			if (td > 2)
			{
				td = 0;
			}
			MESSAGE += TRS[td] + "<button value=\"Cubics\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect view_cubic 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
			td += 2;
		}
		
		if (MESSAGE.length() > 0)
		{
			MAIN_HTML_MESSAGE += "<BR1><table width=100% border=0 cellspacing=0 cellpadding=1 bgcolor=444444><tr>" + "<td><font color=00FFFF>Buffs:</font></td><td align=right>...</td></tr></table>" + "<BR1><table cellspacing=0 cellpadding=0>" + MESSAGE + "</table>";
			MESSAGE = "";
			td = 0;
		}
		
		if (ENABLE_BUFF_SET)
		{
			if (td > 2)
			{
				td = 0;
			}
			MESSAGE += TRS[td] + "<button value=\"" + bottonA + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " castBuffSet 0 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
			td += 2;
		}
		
		if (ENABLE_HEAL)
		{
			if (td > 2)
			{
				td = 0;
			}
			MESSAGE += TRS[td] + "<button value=\"" + bottonB + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " heal 0 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
			td += 2;
		}
		
		if (ENABLE_BUFF_REMOVE)
		{
			if (td > 2)
			{
				td = 0;
			}
			MESSAGE += TRS[td] + "<button value=\"" + bottonC + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " removeBuffs 0 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
			td += 2;
		}
		
		if (MESSAGE.length() > 0)
		{
			MAIN_HTML_MESSAGE += "<BR1><table width=100% border=0 cellspacing=0 cellpadding=1 bgcolor=444444><tr>" + "<td><font color=00FFFF>Preset:</font></td><td align=right><font color=LEVEL>" /* + formatAdena(BUFF_SET_PRICE) */ + "</font> </td></tr></table>"
				+ "<BR1><table cellspacing=0 cellpadding=0>" + MESSAGE + "</table>";
			MESSAGE = "";
			td = 0;
		}
		
		if (ENABLE_SCHEME_SYSTEM)
		{
			MAIN_HTML_MESSAGE += generateScheme(st);
		}
		
		if (st.getPlayer().isGM())
		{
			MAIN_HTML_MESSAGE += "<br><button value=\"GM Manage Buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect manage_buffs 0 0\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		MAIN_HTML_MESSAGE += "<br><font color=303030>" + TITLE_NAME + "</font>" + "</center></body></html>";
		return MAIN_HTML_MESSAGE;
	}
	
	private String generateScheme(QuestState st)
	{
		List<String> schemeName = new ArrayList<>();
		List<String> schemeId = new ArrayList<>();
		String HTML = "";
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement rss = con.prepareStatement("SELECT * FROM buffervip_scheme_list WHERE player_id=?");
			rss.setInt(1, st.getPlayer().getObjectId());
			ResultSet action = rss.executeQuery();
			while (action.next())
			{
				schemeName.add(action.getString("scheme_name"));
				schemeId.add(action.getString("id"));
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		HTML += "<BR1><table width=100% border=0 cellspacing=0 cellpadding=1 bgcolor=444444><tr>" + "<td><font color=00FFFF>Scheme:</font></td><td align=right><font color=LEVEL>" /* + formatAdena(SCHEME_BUFF_PRICE) */ + "</font> </TD></TR></table>" + "<BR1><table cellspacing=0 cellpadding=0>";
		if (schemeName.size() > 0)
		{
			String MESSAGE = "";
			int td = 0;
			String[] TRS =
			{
				"<tr><td>",
				"</td>",
				"<td>",
				"</td></tr>"
			};
			for (int i = 0; i < schemeName.size(); ++i)
			{
				if (td > 2)
				{
					td = 0;
				}
				MESSAGE += TRS[td] + "<button value=\"" + schemeName.get(i) + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " cast " + schemeId.get(i) + " x x\" width=130 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + TRS[td + 1];
				td += 2;
			}
			
			if (MESSAGE.length() > 0)
			{
				HTML += "<table>" + MESSAGE + "</table>";
			}
		}
		
		if (schemeName.size() < SCHEMES_PER_PLAYER)
		{
			HTML += "<BR1><table><tr><td><button value=\"Create\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " create_1 x x x\" width=85 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
		}
		else
		{
			HTML += "<BR1><table width=100><tr>";
		}
		
		if (schemeName.size() > 0)
		{
			HTML += "<td><button value=\"Edit\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_1 x x x\" width=85 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td><button value=\"Delete\" action=\"bypass -h Quest " + QUEST_LOADING_INFO
				+ " delete_1 x x x\" width=85 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>";
		}
		else
		{
			HTML += "</tr></table>";
		}
		return HTML;
	}
	
	private String reloadPanel(QuestState st)
	{
		return "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>" + "<font color=303030>" + TITLE_NAME + "</font><br>" + "<img src=\"L2UI.SquareGray\" width=250 height=1><br>" + "<table width=260 border=0 bgcolor=444444>"
			+ "<tr><td><br></td></tr>"
			+ "<tr><td align=center><font color=FFFFFF>This option can be seen by GMs only and it<br1>allow to update any changes made in the<br1>script. You can disable this option in<br1>the settings section within the Script.<br><font color=LEVEL>Do you want to update the SCRIPT?</font></font></td></tr>"
			+ "<tr><td></td></tr></table><br>" + "<img src=\"L2UI.SquareGray\" width=250 height=1><br><br>" + "<button value=\"Yes\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " reloadscript 1 0 0\" width=50 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">"
			+ "<button value=\"No\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " reloadscript 0 0 0\" width=50 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "</center></body></html>";
	}
	
	private String getItemNameHtml(QuestState st, int itemval)
	{
		return "&#" + itemval + ";";
	}
	
	private int getBuffCount(String scheme)
	{
		int count = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement rss = con.prepareStatement("SELECT buff_class FROM buffervip_scheme_contents WHERE scheme_id=?");
			rss.setString(1, scheme);
			ResultSet action = rss.executeQuery();
			while (action.next())
			{
				++count;
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		return count;
	}
	
	private String getBuffType(int id)
	{
		String val = "none";
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement act = con.prepareStatement("SELECT buffType FROM buffervip_buff_list WHERE buffId=? LIMIT 1");
			act.setInt(1, id);
			ResultSet rs = act.executeQuery();
			if (rs.next())
			{
				val = rs.getString("buffType");
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		return val;
	}
	
	private boolean isEnabled(int id, int level)
	{
		boolean val = false;
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement act = con.prepareStatement("SELECT canUse FROM buffervip_buff_list WHERE buffId=? AND buffLevel=? LIMIT 1");
			act.setInt(1, id);
			act.setInt(2, level);
			ResultSet rs = act.executeQuery();
			if (rs.next())
			{
				if ("1".equals(rs.getString("canUse")))
				{
					val = true;
				}
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		return val;
	}
	
	private boolean isUsed(String scheme, int id, int level)
	{
		boolean used = false;
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement rss = con.prepareStatement("SELECT id FROM buffervip_scheme_contents WHERE scheme_id=? AND skill_id=? AND skill_level=? LIMIT 1");
			rss.setString(1, scheme);
			rss.setInt(2, id);
			rss.setInt(3, level);
			ResultSet action = rss.executeQuery();
			if (action.next())
			{
				used = true;
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		return used;
	}
	
	private int getClassBuff(String id)
	{
		int val = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement getTipo = con.prepareStatement("SELECT buff_class FROM buffervip_buff_list WHERE buffId=?");
			getTipo.setString(1, id);
			ResultSet gt = getTipo.executeQuery();
			if (gt.next())
			{
				val = gt.getInt("buff_class");
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		return val;
	}
	
	private String showText(QuestState st, String type, String text, boolean buttonEnabled, String buttonName, String location)
	{
		String MESSAGE = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>";
		MESSAGE += "<font color=LEVEL>" + type + "</font><br>" + text + "<br>";
		if (buttonEnabled)
		{
			MESSAGE += "<button value=\"" + buttonName + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect " + location + " 0 0\" width=100 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		MESSAGE += "<font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		// st.playSound("ItemSound3.sys_shortage");
		return MESSAGE;
	}
	
	private String reloadConfig(QuestState st)
	{
		try
		{
			if (QuestManager.getInstance().reload(QUEST_LOADING_INFO))
			{
				st.getPlayer().sendMessage("The script and settings have been reloaded successfully.");
			}
			else
			{
				st.getPlayer().sendMessage("Script Reloaded Failed. you edited something wrong! :P, fix it and restart the server");
			}
		}
		catch (Exception e)
		{
			st.getPlayer().sendMessage("Script Reloaded Failed. you edited something wrong! :P, fix it and restart the server");
			print(e);
		}
		return rebuildMainHtml(st);
	}
	
	private BufferVIP(int i, String string, String string2)
	{
		super(-1, BufferVIP.class.getSimpleName(), "custom");
		addStartNpc(NPC_ID);
		addFirstTalkId(NPC_ID);
		addTalkId(NPC_ID);
	}
	
	private boolean isPetBuff(QuestState st)
	{
		return st.getInt("Pet-On-Off") != 0;
	}
	
	private String createScheme()
	{
		return "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>You MUST seprerate new words with a dot (.)<br><br>Scheme name: <edit var=\"name\" width=100><br><br>"
			+ "<button value=\"Create Scheme\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " create $name no_name x x\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO
			+ " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
	}
	
	private String deleteScheme(L2PcInstance player)
	{
		String HTML = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>Available schemes:<br><br>";
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement rss = con.prepareStatement("SELECT * FROM buffervip_scheme_list WHERE player_id=?");
			rss.setInt(1, player.getObjectId());
			ResultSet action = rss.executeQuery();
			while (action.next())
			{
				HTML += "<button value=\"" + action.getString("scheme_name") + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " delete_c " + action.getString("id") + " " + action.getString("scheme_name") + " x\" width=200 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		HTML += "<br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML;
	}
	
	private String editScheme(L2PcInstance player)
	{
		String HTML = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>Select a scheme that you would like to manage:<br><br>";
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement rss = con.prepareStatement("SELECT * FROM buffervip_scheme_list WHERE player_id=?");
			rss.setInt(1, player.getObjectId());
			ResultSet action = rss.executeQuery();
			while (action.next())
			{
				String name = action.getString("scheme_name");
				String id = action.getString("id");
				HTML += "<button value=\"" + name + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " manage_scheme_select " + id + " x x\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		HTML += "<br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML;
	}
	
	private String getOptionList(String scheme)
	{
		int bcount = getBuffCount(scheme);
		String HTML = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>There are <font color=LEVEL>" + bcount + "</font> buffs in current scheme!<br><br>";
		if (bcount < (MAX_SCHEME_BUFFS + MAX_SCHEME_DANCES))
		{
			HTML += "<button value=\"Add buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " manage_scheme_1 " + scheme + " 1 x\" width=200 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (bcount > 0)
		{
			HTML += "<button value=\"Remove buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " manage_scheme_2 " + scheme + " 1 x\" width=200 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		HTML += "<br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_1 0 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"Home\" action=\"bypass -h Quest " + QUEST_LOADING_INFO
			+ " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML;
	}
	
	private String buildHtml(String buffType)
	{
		String HTML_MESSAGE = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><br>";
		
		List<String> availableBuffs = new ArrayList<>();
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement getList = con.prepareStatement("SELECT buffId,buffLevel FROM buffervip_buff_list WHERE buffType=\"" + buffType + "\" AND canUse=1  ORDER BY Buff_Class ASC, id");
			ResultSet rs = getList.executeQuery();
			while (rs.next())
			{
				int bId = rs.getInt("buffId");
				int bLevel = rs.getInt("buffLevel");
				String bName = SkillData.getInstance().getSkill(bId, bLevel).getName();
				bName = bName.replace(" ", "+");
				availableBuffs.add(bName + "_" + bId + "_" + bLevel);
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		
		if (availableBuffs.size() == 0)
		{
			HTML_MESSAGE += "No buffs are available at this moment!";
		}
		else
		{
			if (FREE_BUFFS)
			{
				HTML_MESSAGE += "All buffs are for <font color=LEVEL>free</font>!";
			}
			else
			{
				int price = 0;
				switch (buffType)
				{
					case "buff":
						// price = BUFF_PRICE;
						break;
					case "resist":
						// price = RESIST_PRICE;
						break;
					case "song":
						// price = SONG_PRICE;
						break;
					case "dance":
						// price = DANCE_PRICE;
						break;
					case "chant":
						// price = CHANT_PRICE;
						break;
					case "others":
						// price = OTHERS_PRICE;
						break;
					case "special":
						// price = SPECIAL_PRICE;
						break;
					case "cubic":
						// price = CUBIC_PRICE;
						break;
					default:
						if (DEBUG)
						{
							throw new RuntimeException();
						}
				}
				HTML_MESSAGE += "All special buffs cost <font color=LEVEL>" + formatAdena(price) + "</font> !";
			}
			HTML_MESSAGE += "<BR1><table>";
			for (String buff : availableBuffs)
			{
				buff = buff.replace("_", " ");
				String[] buffSplit = buff.split(" ");
				String name = buffSplit[0];
				int id = Integer.parseInt(buffSplit[1]);
				int level = Integer.parseInt(buffSplit[2]);
				name = name.replace("+", " ");
				HTML_MESSAGE += "<tr><td>" + getSkillIconHtml(id, level) + "</td><td><button value=\"" + name + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " giveBuffs " + id + " " + level + " " + buffType
					+ "\" width=190 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>";
			}
			HTML_MESSAGE += "</table>";
		}
		
		HTML_MESSAGE += "<br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML_MESSAGE;
	}
	
	private String generateQuery(int case1, int case2)
	{
		StringBuilder qry = new StringBuilder();
		if (ENABLE_BUFFS)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				qry.append(",\"buff\"");
			}
		}
		if (ENABLE_RESIST)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				qry.append(",\"resist\"");
			}
		}
		if (ENABLE_SONGS)
		{
			if (case2 < MAX_SCHEME_DANCES)
			{
				qry.append(",\"song\"");
			}
		}
		if (ENABLE_DANCES)
		{
			if (case2 < MAX_SCHEME_DANCES)
			{
				qry.append(",\"dance\"");
			}
		}
		if (ENABLE_CHANTS)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				qry.append(",\"chant\"");
			}
		}
		if (ENABLE_OTHERS)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				qry.append(",\"others\"");
			}
		}
		if (ENABLE_SPECIAL)
		{
			if (case1 < MAX_SCHEME_BUFFS)
			{
				qry.append(",\"special\"");
			}
		}
		if (qry.length() > 0)
		{
			qry.deleteCharAt(0);
		}
		return qry.toString();
	}
	
	private String viewAllSchemeBuffs$getBuffCount(String scheme)
	{
		int count = 0;
		int D_S_Count = 0;
		int B_Count = 0;
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement rss = con.prepareStatement("SELECT buff_class FROM buffervip_scheme_contents WHERE scheme_id=?");
			rss.setString(1, scheme);
			ResultSet action = rss.executeQuery();
			while (action.next())
			{
				++count;
				int val = action.getInt("buff_class");
				if ((val == 1) || (val == 2))
				{
					++D_S_Count;
				}
				else
				{
					++B_Count;
				}
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		String res = count + " " + B_Count + " " + D_S_Count;
		return res;
	}
	
	private String viewAllSchemeBuffs(String scheme, String page, String action)
	{
		List<String> buffList = new ArrayList<>();
		String HTML_MESSAGE = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><br>";
		String[] eventSplit = viewAllSchemeBuffs$getBuffCount(scheme).split(" ");
		int TOTAL_BUFF = Integer.parseInt(eventSplit[0]);
		int BUFF_COUNT = Integer.parseInt(eventSplit[1]);
		int DANCE_SONG = Integer.parseInt(eventSplit[2]);
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			if (action.equals("add"))
			{
				HTML_MESSAGE += "You can add <font color=LEVEL>" + (MAX_SCHEME_BUFFS - BUFF_COUNT) + "</font> Buffs and <font color=LEVEL>" + (MAX_SCHEME_DANCES - DANCE_SONG) + "</font> Dances more!";
				String QUERY = "SELECT * FROM buffervip_buff_list WHERE buffType IN (" + generateQuery(BUFF_COUNT, DANCE_SONG) + ") AND canUse=1 ORDER BY Buff_Class ASC, id";
				PreparedStatement getBuffCount = con.prepareStatement(QUERY);
				ResultSet rss = getBuffCount.executeQuery();
				while (rss.next())
				{
					String name = SkillData.getInstance().getSkill(rss.getInt("buffId"), rss.getInt("buffLevel")).getName();
					name = name.replace(" ", "+");
					buffList.add(name + "_" + rss.getInt("buffId") + "_" + rss.getInt("buffLevel"));
				}
			}
			else if (action.equals("remove"))
			{
				HTML_MESSAGE += "You have <font color=LEVEL>" + BUFF_COUNT + "</font> Buffs and <font color=LEVEL>" + DANCE_SONG + "</font> Dances";
				String QUERY = "SELECT * FROM buffervip_scheme_contents WHERE scheme_id=? ORDER BY Buff_Class ASC, id";
				PreparedStatement getBuffCount = con.prepareStatement(QUERY);
				getBuffCount.setString(1, scheme);
				ResultSet rss = getBuffCount.executeQuery();
				while (rss.next())
				{
					String name = SkillData.getInstance().getSkill(rss.getInt("skill_id"), rss.getInt("skill_level")).getName();
					name = name.replace(" ", "+");
					buffList.add(name + "_" + rss.getInt("skill_id") + "_" + rss.getInt("skill_level"));
				}
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		
		HTML_MESSAGE += "<BR1><table border=0><tr>";
		final int buffsPerPage = 20;
		final String width, pageName;
		int pc = ((buffList.size() - 1) / buffsPerPage) + 1;
		if (pc > 5)
		{
			width = "25";
			pageName = "P";
		}
		else
		{
			width = "50";
			pageName = "Page ";
		}
		for (int ii = 1; ii <= pc; ++ii)
		{
			if (ii == Integer.parseInt(page))
			{
				HTML_MESSAGE += "<td width=" + width + " align=center><font color=LEVEL>" + pageName + ii + "</font></td>";
			}
			else if (action.equals("add"))
			{
				HTML_MESSAGE += "<td width=" + width + ">" + "<button value=\"" + pageName + ii + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " manage_scheme_1 " + scheme + " " + ii + " x\" width=" + width + " height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
			}
			else if (action.equals("remove"))
			{
				HTML_MESSAGE += "<td width=" + width + ">" + "<button value=\"" + pageName + ii + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " manage_scheme_2 " + scheme + " " + ii + " x\" width=" + width + " height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}
		}
		HTML_MESSAGE += "</tr></table>";
		
		int limit = buffsPerPage * Integer.parseInt(page);
		int start = limit - buffsPerPage;
		int end = Math.min(limit, buffList.size());
		int k = 0;
		for (int i = start; i < end; ++i)
		{
			String value = buffList.get(i);
			value = value.replace("_", " ");
			String[] extr = value.split(" ");
			String name = extr[0];
			name = name.replace("+", " ");
			int id = Integer.parseInt(extr[1]);
			int level = Integer.parseInt(extr[2]);
			if (action.equals("add"))
			{
				if (!isUsed(scheme, id, level))
				{
					if ((k % 2) != 0)
					{
						HTML_MESSAGE += "<BR1><table border=0 bgcolor=333333>";
					}
					else
					{
						HTML_MESSAGE += "<BR1><table border=0 bgcolor=292929>";
					}
					HTML_MESSAGE += "<tr><td width=35>" + getSkillIconHtml(id, level) + "</td><td fixwidth=170>" + name + "</td><td><button value=\"Add\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " add_buff " + scheme + "_" + id + "_" + level + " " + page + " " + TOTAL_BUFF
						+ "\" width=65 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "</tr></table>";
					k += 1;
				}
			}
			else if (action.equals("remove"))
			{
				if ((k % 2) != 0)
				{
					HTML_MESSAGE += "<BR1><table border=0 bgcolor=333333>";
				}
				else
				{
					HTML_MESSAGE += "<BR1><table border=0 bgcolor=292929>";
				}
				HTML_MESSAGE += "<tr><td width=35>" + getSkillIconHtml(id, level) + "</td><td fixwidth=170>" + name + "</td><td><button value=\"Remove\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " remove_buff " + scheme + "_" + id + "_" + level + " " + page + " " + TOTAL_BUFF
					+ "\" width=65 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "</table>";
				k += 1;
			}
		}
		HTML_MESSAGE += "<br><br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " manage_scheme_select " + scheme + " x x\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"Home\" action=\"bypass -h Quest "
			+ QUEST_LOADING_INFO + " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML_MESSAGE;
	}
	
	private String viewAllBuffTypes()
	{
		String HTML_MESSAGE = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>";
		HTML_MESSAGE += "<font color=LEVEL>[Buff management]</font><br>";
		if (ENABLE_BUFFS)
		{
			HTML_MESSAGE += "<button value=\"Buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list buff Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_RESIST)
		{
			HTML_MESSAGE += "<button value=\"Resist Buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list resist Resists 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_SONGS)
		{
			HTML_MESSAGE += "<button value=\"Songs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list song Songs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_DANCES)
		{
			HTML_MESSAGE += "<button value=\"Dances\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list dance Dances 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_CHANTS)
		{
			HTML_MESSAGE += "<button value=\"Chants\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list chant Chants 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_SPECIAL)
		{
			HTML_MESSAGE += "<button value=\"Special Buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list special Special_Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_OTHERS)
		{
			HTML_MESSAGE += "<button value=\"Others Buffs\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list others Others_Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_CUBIC)
		{
			HTML_MESSAGE += "<button value=\"Cubics\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list cubic cubic_Buffs 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">";
		}
		if (ENABLE_BUFF_SET)
		{
			HTML_MESSAGE += "<button value=\"Buff Sets\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list set Buff_Sets 1\" width=200 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>";
		}
		HTML_MESSAGE += "<button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML_MESSAGE;
	}
	
	private String viewAllBuffs(String type, String typeName, String page)
	{
		List<String> buffList = new ArrayList<>();
		String HTML_MESSAGE = "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>";
		typeName = typeName.replace("_", " ");
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			final PreparedStatement getBuffCount;
			if (type.equals("set"))
			{
				getBuffCount = con.prepareStatement("SELECT * FROM buffervip_buff_list WHERE buffType IN (" + generateQuery(0, 0) + ") AND canUse=1");
			}
			else
			{
				getBuffCount = con.prepareStatement("SELECT * FROM buffervip_buff_list WHERE buffType=?");
				getBuffCount.setString(1, type);
			}
			ResultSet rss = getBuffCount.executeQuery();
			while (rss.next())
			{
				String name = SkillData.getInstance().getSkill(rss.getInt("buffId"), rss.getInt("buffLevel")).getName();
				name = name.replace(" ", "+");
				String usable = rss.getString("canUse");
				String forClass = rss.getString("forClass");
				String skill_id = rss.getString("buffId");
				String skill_level = rss.getString("buffLevel");
				buffList.add(name + "_" + forClass + "_" + page + "_" + usable + "_" + skill_id + "_" + skill_level);
			}
		}
		catch (SQLException e)
		{
			print(e);
		}
		Collections.sort(buffList);
		
		HTML_MESSAGE += "<font color=LEVEL>[Buff management - " + typeName + " - Page " + page + "]</font><br><table border=0><tr>";
		final int buffsPerPage;
		if (type.equals("set"))
		{
			buffsPerPage = 12;
		}
		else
		{
			buffsPerPage = 20;
		}
		final String width, pageName;
		int pc = ((buffList.size() - 1) / buffsPerPage) + 1;
		if (pc > 5)
		{
			width = "25";
			pageName = "P";
		}
		else
		{
			width = "50";
			pageName = "Page ";
		}
		typeName = typeName.replace(" ", "_");
		for (int ii = 1; ii <= pc; ++ii)
		{
			if (ii == Integer.parseInt(page))
			{
				HTML_MESSAGE += "<td width=" + width + " align=center><font color=LEVEL>" + pageName + ii + "</font></td>";
			}
			else
			{
				HTML_MESSAGE += "<td width=" + width + "><button value=\"" + pageName + "" + ii + "\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " edit_buff_list " + type + " " + typeName + " " + ii + "\" width=" + width
					+ " height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>";
			}
		}
		HTML_MESSAGE += "</tr></table><br>";
		
		int limit = buffsPerPage * Integer.parseInt(page);
		int start = limit - buffsPerPage;
		int end = Math.min(limit, buffList.size());
		for (int i = start; i < end; ++i)
		{
			String value = buffList.get(i);
			value = value.replace("_", " ");
			String[] extr = value.split(" ");
			String name = extr[0];
			name = name.replace("+", " ");
			int forClass = Integer.parseInt(extr[1]);
			int usable = Integer.parseInt(extr[3]);
			String skillPos = extr[4] + "_" + extr[5];
			if ((i % 2) != 0)
			{
				HTML_MESSAGE += "<BR1><table border=0 bgcolor=333333>";
			}
			else
			{
				HTML_MESSAGE += "<BR1><table border=0 bgcolor=292929>";
			}
			if (type.equals("set"))
			{
				String listOrder = null;
				if (forClass == 0)
				{
					listOrder = "List=\"" + SET_FIGHTER + ";" + SET_MAGE + ";" + SET_ALL + ";" + SET_NONE + ";\"";
				}
				else if (forClass == 1)
				{
					listOrder = "List=\"" + SET_MAGE + ";" + SET_FIGHTER + ";" + SET_ALL + ";" + SET_NONE + ";\"";
				}
				else if (forClass == 2)
				{
					listOrder = "List=\"" + SET_ALL + ";" + SET_FIGHTER + ";" + SET_MAGE + ";" + SET_NONE + ";\"";
				}
				else if (forClass == 3)
				{
					listOrder = "List=\"" + SET_NONE + ";" + SET_FIGHTER + ";" + SET_MAGE + ";" + SET_ALL + ";\"";
				}
				HTML_MESSAGE += "<tr><td fixwidth=145>" + name + "</td><td width=70><combobox var=\"newSet" + i + "\" width=70 " + listOrder + "></td>" + "<td width=50><button value=\"Update\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " changeBuffSet " + skillPos + " $newSet" + i + " "
					+ page + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>";
			}
			else
			{
				HTML_MESSAGE += "<tr><td fixwidth=170>" + name + "</td><td width=80>";
				if (usable == 1)
				{
					HTML_MESSAGE += "<button value=\"Disable\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " editSelectedBuff " + skillPos + " 0-" + page + " " + type + "\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>";
				}
				else if (usable == 0)
				{
					HTML_MESSAGE += "<button value=\"Enable\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " editSelectedBuff " + skillPos + " 1-" + page + " " + type + "\" width=80 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>";
				}
			}
			HTML_MESSAGE += "</table>";
		}
		HTML_MESSAGE += "<br><br><button value=\"Back\" action=\"bypass -h Quest " + QUEST_LOADING_INFO + " redirect manage_buffs 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"Home\" action=\"bypass -h Quest " + QUEST_LOADING_INFO
			+ " redirect main 0 0\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
		return HTML_MESSAGE;
	}
	
	private void manageSelectedBuff(String buffPosId, String canUseBuff)
	{
		String[] bpid = buffPosId.split("_");
		String bId = bpid[0];
		String bLvl = bpid[1];
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement upd = con.prepareStatement("UPDATE buffervip_buff_list SET canUse=? WHERE buffId=? AND buffLevel=? LIMIT 1");
			upd.setString(1, canUseBuff);
			upd.setString(2, bId);
			upd.setString(3, bLvl);
			upd.executeUpdate();
			upd.close();
		}
		catch (SQLException e)
		{
			print(e);
		}
	}
	
	private String manageSelectedSet(String id, String newVal, String opt3)
	{
		String[] bpid = id.split("_");
		String bId = bpid[0];
		String bLvl = bpid[1];
		try (Connection con = ConnectionFactory.getInstance().getConnection())
		{
			PreparedStatement upd = con.prepareStatement("UPDATE buffervip_buff_list SET forClass=? WHERE buffId=? AND bufflevel=?");
			upd.setString(1, newVal);
			upd.setString(2, bId);
			upd.setString(3, bLvl);
			upd.executeUpdate();
			upd.close();
		}
		catch (SQLException e)
		{
			print(e);
		}
		return viewAllBuffs("set", "Buff Sets", opt3);
	}
	
	private void addTimeout(QuestState st, int gaugeColor, int amount, int offset)
	{
		int endtime = (int) ((System.currentTimeMillis() + (amount * 1000)) / 1000);
		st.set("blockUntilTime", String.valueOf(endtime));
		st.getPlayer().sendPacket(new SetupGauge(gaugeColor, (amount * 1000) + offset));
	}
	
	private void heal(L2PcInstance player, boolean isPet)
	{
		L2Summon target = player.getSummon();
		if (!isPet)
		{
			PcStatus pcStatus = player.getStatus();
			PcStat pcStat = player.getStat();
			pcStatus.setCurrentHp(pcStat.getMaxHp());
			pcStatus.setCurrentMp(pcStat.getMaxMp());
			pcStatus.setCurrentCp(pcStat.getMaxCp());
		}
		else if (target != null)
		{
			SummonStatus petStatus = target.getStatus();
			SummonStat petStat = target.getStat();
			petStatus.setCurrentHp(petStat.getMaxHp());
			petStatus.setCurrentMp(petStat.getMaxMp());
			if (target instanceof L2PetInstance)
			{
				L2PetInstance pet = (L2PetInstance) target;
				pet.setCurrentFed(pet.getMaxFed());
				player.sendPacket(new SetSummonRemainTime(pet.getMaxFed(), pet.getCurrentFed()));
			}
			else if (target instanceof L2ServitorInstance)
			{
				L2ServitorInstance summon = (L2ServitorInstance) target;
				// FIXME: summon.setLifeTimeRemaining(summon.getLifeTimeRemaining() - summon.getLifeTime());
				summon.setLifeTimeRemaining(summon.getLifeTimeRemaining() + summon.getLifeTime());
				player.sendPacket(new SetSummonRemainTime(summon.getLifeTime(), summon.getLifeTimeRemaining()));
			}
			else if (DEBUG)
			{
				throw new RuntimeException();
			}
		}
	}
	
	@SuppressWarnings(
	{
		"unused",
	})
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (DEBUG)
		{
			System.out.println(getName() + "#onAdvEvent('" + event + "'," + (npc == null ? "NULL" : npc.getId() + npc.getName()) + "," + (player == null ? "NULL" : player.getName()) + ")");
		}
		QuestState st = player.getQuestState(QUEST_LOADING_INFO);
		String[] eventSplit = event.split(" ", 4);
		if (eventSplit.length != 4)
		{
			player.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
			return null;
		}
		String eventParam0 = eventSplit[0];
		String eventParam1 = eventSplit[1];
		String eventParam2 = eventSplit[2];
		String eventParam3 = eventSplit[3];
		
		switch (eventParam0)
		{
			case "reloadscript":
				if (eventParam1.equals("1"))
				{
					return reloadConfig(st);
				}
				if (eventParam1.equals("0"))
				{
					return rebuildMainHtml(st);
				}
				if (DEBUG)
				{
					throw new RuntimeException();
				}
				
			case "redirect":
				switch (eventParam1)
				{
					case "main":
						return rebuildMainHtml(st);
					case "manage_buffs":
						return viewAllBuffTypes();
					case "view_buffs":
						return buildHtml("buff");
					case "view_resists":
						return buildHtml("resist");
					case "view_songs":
						return buildHtml("song");
					case "view_dances":
						return buildHtml("dance");
					case "view_chants":
						return buildHtml("chant");
					case "view_others":
						return buildHtml("others");
					case "view_special":
						return buildHtml("special");
					case "view_cubic":
						return buildHtml("cubic");
					default:
						if (DEBUG)
						{
							throw new RuntimeException();
						}
				}
				
			case "buffpet":
				if ((int) (System.currentTimeMillis() / 1000) > st.getInt("blockUntilTime"))
				{
					st.set("Pet-On-Off", eventParam1);
					if (TIME_OUT)
					{
						addTimeout(st, 3, TIME_OUT_TIME / 2, 600);
					}
				}
				return rebuildMainHtml(st);
			
			case "create":
			{
				String param = eventParam1.replaceAll("[ !" + "\"" + "#$%&'()*+,/:;<=>?@" + "\\[" + "\\\\" + "\\]" + "\\^" + "`{|}~]", "");
				if ((param.length() == 0) || param.equals("no_name"))
				{
					player.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
					return showText(st, "Info", "Please, enter the scheme name!", true, "Return", "main");
				}
				try (Connection con = ConnectionFactory.getInstance().getConnection())
				{
					PreparedStatement ins = con.prepareStatement("INSERT INTO buffervip_scheme_list (player_id,scheme_name) VALUES (?,?)");
					ins.setInt(1, player.getObjectId());
					ins.setString(2, param);
					ins.executeUpdate();
					ins.close();
				}
				catch (SQLException e)
				{
					print(e);
				}
				return rebuildMainHtml(st);
			}
			
			case "delete":
				try (Connection con = ConnectionFactory.getInstance().getConnection())
				{
					PreparedStatement rem;
					rem = con.prepareStatement("DELETE FROM buffervip_scheme_list WHERE id=? LIMIT 1");
					rem.setString(1, eventParam1);
					rem.executeUpdate();
					rem.close();
					rem = con.prepareStatement("DELETE FROM buffervip_scheme_contents WHERE scheme_id=?");
					rem.setString(1, eventParam1);
					rem.executeUpdate();
					rem.close();
				}
				catch (SQLException e)
				{
					print(e);
				}
				return rebuildMainHtml(st);
			
			case "delete_c":
				return "<html><head><title>" + TITLE_NAME + "</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>Do you really want to delete '" + eventParam2 + "' scheme?<br><br>" + "<button value=\"Yes\" action=\"bypass -h Quest " + QUEST_LOADING_INFO
					+ " delete " + eventParam1 + " x x\" width=50 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<button value=\"No\" action=\"bypass -h Quest " + QUEST_LOADING_INFO
					+ " delete_1 x x x\" width=50 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" + "<br><font color=303030>" + TITLE_NAME + "</font></center></body></html>";
			
			case "create_1":
				return createScheme();
			case "edit_1":
				return editScheme(player);
			case "delete_1":
				return deleteScheme(player);
			case "manage_scheme_1":
				return viewAllSchemeBuffs(eventParam1, eventParam2, "add");
			case "manage_scheme_2":
				return viewAllSchemeBuffs(eventParam1, eventParam2, "remove");
			case "manage_scheme_select":
				return getOptionList(eventParam1);
			
			case "remove_buff":
			{
				String[] split = eventParam1.split("_");
				String scheme = split[0];
				String skill = split[1];
				String level = split[2];
				try (Connection con = ConnectionFactory.getInstance().getConnection())
				{
					PreparedStatement rem = con.prepareStatement("DELETE FROM buffer_scheme_contents WHERE scheme_id=? AND skill_id=? AND skill_level=? LIMIT 1");
					rem.setString(1, scheme);
					rem.setString(2, skill);
					rem.setString(3, level);
					rem.executeUpdate();
				}
				catch (SQLException e)
				{
					print(e);
				}
				int temp = Integer.parseInt(eventParam3) - 1;
				final String HTML;
				if (temp <= 0)
				{
					HTML = getOptionList(scheme);
				}
				else
				{
					HTML = viewAllSchemeBuffs(scheme, eventParam2, "remove");
				}
				return HTML;
			}
			
			case "add_buff":
			{
				String[] split = eventParam1.split("_");
				String scheme = split[0];
				String skill = split[1];
				String level = split[2];
				int idbuffclass = getClassBuff(skill);
				try (Connection con = ConnectionFactory.getInstance().getConnection())
				{
					PreparedStatement ins = con.prepareStatement("INSERT INTO buffervip_scheme_contents (scheme_id,skill_id,skill_level,buff_class) VALUES (?,?,?,?)");
					ins.setString(1, scheme);
					ins.setString(2, skill);
					ins.setString(3, level);
					ins.setInt(4, idbuffclass);
					ins.executeUpdate();
					ins.close();
				}
				catch (SQLException e)
				{
					print(e);
				}
				int temp = Integer.parseInt(eventParam3) + 1;
				final String HTML;
				if (temp >= (MAX_SCHEME_BUFFS + MAX_SCHEME_DANCES))
				{
					HTML = getOptionList(scheme);
				}
				else
				{
					HTML = viewAllSchemeBuffs(scheme, eventParam2, "add");
				}
				return HTML;
			}
			
			case "edit_buff_list":
				return viewAllBuffs(eventParam1, eventParam2, eventParam3);
			
			case "changeBuffSet":
				if (eventParam2.equals(SET_FIGHTER))
				{
					eventParam2 = "0";
				}
				else if (eventParam2.equals(SET_MAGE))
				{
					eventParam2 = "1";
				}
				else if (eventParam2.equals(SET_ALL))
				{
					eventParam2 = "2";
				}
				else if (eventParam2.equals(SET_NONE))
				{
					eventParam2 = "3";
				}
				else if (DEBUG)
				{
					throw new RuntimeException();
				}
				return manageSelectedSet(eventParam1, eventParam2, eventParam3);
			
			case "editSelectedBuff":
			{
				eventParam2 = eventParam2.replace("-", " ");
				String[] split = eventParam2.split(" ");
				String action = split[0];
				String page = split[1];
				manageSelectedBuff(eventParam1, action);
				final String typeName;
				switch (eventParam3)
				{
					case "buff":
						typeName = "Buffs";
						break;
					case "resist":
						typeName = "Resists";
						break;
					case "song":
						typeName = "Songs";
						break;
					case "dance":
						typeName = "Dances";
						break;
					case "chant":
						typeName = "Chants";
						break;
					case "others":
						typeName = "Others_Buffs";
						break;
					case "special":
						typeName = "Special_Buffs";
						break;
					case "cubic":
						typeName = "Cubics";
						break;
					default:
						throw new RuntimeException();
				}
				return viewAllBuffs(eventParam3, typeName, page);
			}
			
			case "viewSelectedConfig":
				throw new RuntimeException();
				
			case "changeConfig":
				throw new RuntimeException();
				
			case "heal":
				if ((int) (System.currentTimeMillis() / 1000) > st.getInt("blockUntilTime"))
				{
					if (player.isInCombat())
					{
						return showText(st, "Info", "You can't use the heal function while in combat.", false, "Return", "main");
					}
					
					// if (st.getQuestItemsCount(CONSUMABLE_ID) < HEAL_PRICE)
					{
						// return showText(st, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + HEAL_PRICE + " " + getItemNameHtml(st, CONSUMABLE_ID) + "!", false, "0", "0");
					}
					final boolean getSummonbuff = isPetBuff(st);
					if (getSummonbuff)
					{
						if (player.getSummon() != null)
						{
							heal(player, getSummonbuff);
						}
						else
						{
							return showText(st, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
						}
					}
					else
					{
						heal(player, getSummonbuff);
					}
					// st.takeItems(CONSUMABLE_ID, HEAL_PRICE);
					if (TIME_OUT)
					{
						addTimeout(st, 1, TIME_OUT_TIME / 2, 600);
					}
				}
				return SMART_WINDOW ? null : rebuildMainHtml(st);
			
			case "removeBuffs":
				if ((int) (System.currentTimeMillis() / 1000) > st.getInt("blockUntilTime"))
				{
					// if (st.getQuestItemsCount(CONSUMABLE_ID) < BUFF_REMOVE_PRICE)
					{
						// return showText(st, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + BUFF_REMOVE_PRICE + " " + getItemNameHtml(st, CONSUMABLE_ID) + "!", false, "0", "0");
					}
					final boolean getSummonbuff = isPetBuff(st);
					if (getSummonbuff)
					{
						if (player.getSummon() != null)
						{
							player.getSummon().stopAllEffects();
						}
						else
						{
							return showText(st, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
						}
					}
					else
					{
						player.stopAllEffects();
						if (player.getCubics() != null)
						{
							for (L2CubicInstance cubic : player.getCubics().values())
							{
								cubic.stopAction();
								player.getCubics().remove(cubic);
							}
						}
					}
					// st.takeItems(CONSUMABLE_ID, BUFF_REMOVE_PRICE);
					if (TIME_OUT)
					{
						addTimeout(st, 2, TIME_OUT_TIME / 2, 600);
					}
				}
				return SMART_WINDOW ? null : rebuildMainHtml(st);
			
			case "cast":
				if ((int) (System.currentTimeMillis() / 1000) > st.getInt("blockUntilTime"))
				{
					List<Integer> buffs = new ArrayList<>();
					List<Integer> levels = new ArrayList<>();
					try (Connection con = ConnectionFactory.getInstance().getConnection())
					{
						PreparedStatement rss = con.prepareStatement("SELECT * FROM buffervip_scheme_contents WHERE scheme_id=? ORDER BY id");
						rss.setString(1, eventParam1);
						ResultSet action = rss.executeQuery();
						while (action.next())
						{
							int id = Integer.parseInt(action.getString("skill_id"));
							int level = Integer.parseInt(action.getString("skill_level"));
							switch (getBuffType(id))
							{
								case "buff":
									if (ENABLE_BUFFS)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								case "resist":
									if (ENABLE_RESIST)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								case "song":
									if (ENABLE_SONGS)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								case "dance":
									if (ENABLE_DANCES)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								case "chant":
									if (ENABLE_CHANTS)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								case "others":
									if (ENABLE_OTHERS)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								case "special":
									if (ENABLE_SPECIAL)
									{
										if (isEnabled(id, level))
										{
											buffs.add(id);
											levels.add(level);
										}
									}
									break;
								default:
									if (DEBUG)
									{
										throw new RuntimeException();
									}
							}
						}
					}
					catch (SQLException e)
					{
						print(e);
					}
					
					if (buffs.size() == 0)
					{
						return viewAllSchemeBuffs(eventParam1, "1", "add");
					}
					if (!FREE_BUFFS)
					{
						// if (st.getQuestItemsCount(CONSUMABLE_ID) < SCHEME_BUFF_PRICE)
						{
							// return showText(st, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + SCHEME_BUFF_PRICE + " " + getItemNameHtml(st, CONSUMABLE_ID) + "!", false, "0", "0");
						}
					}
					
					final boolean getSummonbuff = isPetBuff(st);
					for (int i = 0; i < buffs.size(); ++i)
					{
						if (!getSummonbuff)
						{
							npc.broadcastPacket(new MagicSkillUse(npc, player, buffs.get(i), 1, 1, 1));
							SkillData.getInstance().getSkill(buffs.get(i), levels.get(i)).applyEffects(player, player);
						}
						else
						{
							if (player.getSummon() != null)
							{
								npc.broadcastPacket(new MagicSkillUse(npc, player.getSummon(), buffs.get(i), 1, 1, 1));
								SkillData.getInstance().getSkill(buffs.get(i), levels.get(i)).applyEffects(player.getSummon(), player.getSummon());
							}
							else
							{
								return showText(st, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
							}
						}
					}
					// st.takeItems(CONSUMABLE_ID, SCHEME_BUFF_PRICE);
					if (TIME_OUT)
					{
						addTimeout(st, 3, TIME_OUT_TIME, 600);
					}
				}
				return SMART_WINDOW ? null : rebuildMainHtml(st);
			
			case "giveBuffs":
			{
				final int cost;
				switch (eventParam3)
				{
					case "buff":
						// cost = BUFF_PRICE;
						break;
					case "resist":
						// cost = RESIST_PRICE;
						break;
					case "song":
						// cost = SONG_PRICE;
						break;
					case "dance":
						// cost = DANCE_PRICE;
						break;
					case "chant":
						// cost = CHANT_PRICE;
						break;
					case "others":
						// cost = OTHERS_PRICE;
						break;
					case "special":
						// cost = SPECIAL_PRICE;
						break;
					case "cubic":
						// cost = CUBIC_PRICE;
						break;
					default:
						throw new RuntimeException();
				}
				
				if ((int) (System.currentTimeMillis() / 1000) > st.getInt("blockUntilTime"))
				{
					if (!FREE_BUFFS)
					{
						// if (st.getQuestItemsCount(CONSUMABLE_ID) < cost)
						{
							// return showText(st, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + cost + " " + getItemNameHtml(st, CONSUMABLE_ID) + "!", false, "0", "0");
						}
					}
					Skill skill = SkillData.getInstance().getSkill(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2));
					if (skill.hasEffectType(L2EffectType.SUMMON))
					{
						if (st.getQuestItemsCount(skill.getItemConsumeId()) < skill.getItemConsumeCount())
						{
							return showText(st, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + skill.getItemConsumeCount() + " " + getItemNameHtml(st, skill.getItemConsumeId()) + "!", false, "0", "0");
						}
					}
					final boolean getSummonbuff = isPetBuff(st);
					if (!getSummonbuff)
					{
						if (eventParam3.equals("cubic"))
						{
							if (!player.getCubics().isEmpty())
							{
								for (L2CubicInstance cubic : player.getCubics().values())
								{
									cubic.stopAction();
									player.getCubics().remove(cubic.getId(), cubic);
								}
							}
							npc.broadcastPacket(new MagicSkillUse(npc, player, Integer.parseInt(eventParam1), 1, 1, 1));
							player.useMagic(SkillData.getInstance().getSkill(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)), false, false);
						}
						else
						{
							npc.broadcastPacket(new MagicSkillUse(npc, player, Integer.parseInt(eventParam1), 1, 1, 1));
							SkillData.getInstance().getSkill(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)).applyEffects(player, player);
						}
					}
					else
					{
						if (eventParam3.equals("cubic"))
						{
							if (!player.getCubics().isEmpty())
							{
								for (L2CubicInstance cubic : player.getCubics().values())
								{
									cubic.stopAction();
									player.getCubics().remove(cubic.getId(), cubic);
								}
							}
							npc.broadcastPacket(new MagicSkillUse(npc, player, Integer.parseInt(eventParam1), 1, 1, 1));
							player.useMagic(SkillData.getInstance().getSkill(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)), false, false);
						}
						else
						{
							if (player.getSummon() != null)
							{
								npc.broadcastPacket(new MagicSkillUse(npc, player.getSummon(), Integer.parseInt(eventParam1), 1, 1, 1));
								SkillData.getInstance().getSkill(Integer.parseInt(eventParam1), Integer.parseInt(eventParam2)).applyEffects(player.getSummon(), player.getSummon());
							}
							else
							{
								return showText(st, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
							}
						}
					}
					// st.takeItems(CONSUMABLE_ID, cost);
					if (TIME_OUT)
					{
						addTimeout(st, 3, TIME_OUT_TIME / 10, 600);
					}
				}
				return SMART_WINDOW ? null : buildHtml(eventParam3);
			}
			
			case "castBuffSet":
				if ((int) (System.currentTimeMillis() / 1000) > st.getInt("blockUntilTime"))
				{
					if (!FREE_BUFFS)
					{
						// if (st.getQuestItemsCount(CONSUMABLE_ID) < BUFF_SET_PRICE)
						{
							// return showText(st, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + BUFF_SET_PRICE + " " + getItemNameHtml(st, CONSUMABLE_ID) + "!", false, "0", "0");
						}
					}
					List<int[]> buff_sets = new ArrayList<>();
					final int player_class;
					if (player.isMageClass())
					{
						player_class = 1;
					}
					else
					{
						player_class = 0;
					}
					final boolean getSummonbuff = isPetBuff(st);
					if (!getSummonbuff)
					{
						try (Connection con = ConnectionFactory.getInstance().getConnection())
						{
							PreparedStatement getSimilarNameCount = con.prepareStatement("SELECT buffId,buffLevel FROM buffervip_buff_list WHERE forClass IN (?,?) ORDER BY id ASC");
							getSimilarNameCount.setInt(1, player_class);
							getSimilarNameCount.setString(2, "2");
							ResultSet rss = getSimilarNameCount.executeQuery();
							while (rss.next())
							{
								int id = rss.getInt("buffId");
								int lvl = rss.getInt("buffLevel");
								buff_sets.add(new int[]
								{
									id,
									lvl
								});
							}
						}
						catch (SQLException e)
						{
							print(e);
						}
						for (int[] i : buff_sets)
						{
							npc.broadcastPacket(new MagicSkillUse(npc, player, i[0], 1, 1, 1));
							SkillData.getInstance().getSkill(i[0], i[1]).applyEffects(player, player);
						}
					}
					else
					{
						if (player.getSummon() != null)
						{
							try (Connection con = ConnectionFactory.getInstance().getConnection())
							{
								PreparedStatement getSimilarNameCount = con.prepareStatement("SELECT buffId,buffLevel FROM buffervip_buff_list WHERE forClass IN (?,?) ORDER BY id ASC");
								getSimilarNameCount.setString(1, "0");
								getSimilarNameCount.setString(2, "2");
								ResultSet rss = getSimilarNameCount.executeQuery();
								while (rss.next())
								{
									int id = rss.getInt("buffId");
									int lvl = rss.getInt("buffLevel");
									buff_sets.add(new int[]
									{
										id,
										lvl
									});
								}
							}
							catch (SQLException e)
							{
								print(e);
							}
							for (int[] i : buff_sets)
							{
								npc.broadcastPacket(new MagicSkillUse(npc, player.getSummon(), i[0], 1, 1, 1));
								SkillData.getInstance().getSkill(i[0], i[1]).applyEffects(player.getSummon(), player.getSummon());
							}
						}
						else
						{
							return showText(st, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
						}
					}
					// st.takeItems(CONSUMABLE_ID, BUFF_SET_PRICE);
					if (TIME_OUT)
					{
						addTimeout(st, 3, TIME_OUT_TIME, 600);
					}
				}
				return SMART_WINDOW ? null : rebuildMainHtml(st);
			
		}
		return rebuildMainHtml(st);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(QUEST_LOADING_INFO);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		st.set("Pet-On-Off", 0);
		
		if (player.isGM())
		{
			if (SCRIPT_RELOAD)
			{
				return reloadPanel(st);
			}
			return rebuildMainHtml(st);
		}
		
		if (!BUFF_WITH_KARMA)
		{
			if (player.getKarma() > 0)
			{
				return showText(st, "Info", "You have too much <font color=FF0000>karma!</font><br>Come back,<br>when you don't have any karma!", false, "Return", "main");
			}
			else if (OlympiadManager.getInstance().isRegistered(player))
			{
				return showText(st, "Info", "You can not buff while you are in <font color=FF0000>Olympiad!</font><br>Come back,<br>when you are out of the Olympiad.", false, "Return", "main");
			}
			else if (TvTEvent.isPlayerParticipant(player.getObjectId()))
			{
				return showText(st, "Info", "You can not buff while you are in <font color=\"FF0000\">TvT!</font><br>Come back,<br>when you are out of TvT!", false, "Return", "main");
			}
			else if (player.getLevel() < MIN_LEVEL)
			{
				return showText(st, "Info", "Your level is too low!<br>You have to be at least level <font color=LEVEL>" + MIN_LEVEL + "</font>,<br>to use my services!", false, "Return", "main");
			}
			else if (player.getPvpFlag() > 0)
			{
				return showText(st, "Info", "You can't buff while you are <font color=800080>flagged!</font><br>Wait some time and try again!", false, "Return", "main");
			}
			else if (player.isInCombat())
			{
				return showText(st, "Info", "You can't buff while you are attacking!<br>Stop your fight and try again!", false, "Return", "main");
			}
			else
			{
				return rebuildMainHtml(st);
			}
		}
		else if (OlympiadManager.getInstance().isRegistered(player))
		{
			return showText(st, "Info", "You can not buff while you are in <font color=FF0000>Olympiad!</font><br>Come back,<br>when you are out of the Olympiad.", false, "Return", "main");
		}
		else if (TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			return showText(st, "Info", "You can not buff while you are in <font color=\"FF0000\">TvT!</font><br>Come back,<br>when you are out of TvT!", false, "Return", "main");
		}
		else if (player.getLevel() < MIN_LEVEL)
		{
			return showText(st, "Info", "Your level is too low!<br>You have to be at least level <font color=LEVEL>" + MIN_LEVEL + "</font>,<br>to use my services!", false, "Return", "main");
		}
		else if (player.getPvpFlag() > 0)
		{
			return showText(st, "Info", "You can't buff while you are <font color=800080>flagged!</font><br>Wait some time and try again!", false, "Return", "main");
		}
		else if (player.isInCombat())
		{
			return showText(st, "Info", "You can't buff while you are attacking!<br>Stop your fight and try again!", false, "Return", "main");
		}
		else if (player.hasPremiumStatus())
		{
			return rebuildMainHtml(st);
		}
		else
		{
			return showText(st, "Info", "Only Premium can use this Manager. Contact a GM for more information!", false, "Return", "main");
		}
	}
	
	@Override
	public boolean showResult(L2PcInstance player, String res)
	{
		if (SMART_WINDOW)
		{
			if ((player != null) && (res != null) && res.startsWith("<html>"))
			{
				final NpcHtmlMessage npcReply = new NpcHtmlMessage();
				npcReply.setHtml(res);
				player.sendPacket(npcReply);
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		return super.showResult(player, res);
	}
	
	private String getSkillIconHtml(int id, int level)
	{
		String iconNumber = getSkillIconNumber(id, level);
		return "<button action=\"bypass -h Quest " + QUEST_LOADING_INFO + " description " + id + " " + level + " x\" width=32 height=32 back=\"Icon.skill" + iconNumber + "\" fore=\"Icon.skill" + iconNumber + "\">";
	}
	
	private String getSkillIconNumber(int id, int level)
	{
		String formato;
		if (id == 4)
		{
			formato = "0004";
		}
		else if ((id > 9) && (id < 100))
		{
			formato = "00" + id;
		}
		else if ((id > 99) && (id < 1000))
		{
			formato = "0" + id;
		}
		else if (id == 1517)
		{
			formato = "1536";
		}
		else if (id == 1518)
		{
			formato = "1537";
		}
		else if (id == 1547)
		{
			formato = "0065";
		}
		else if (id == 2076)
		{
			formato = "0195";
		}
		else if ((id > 4550) && (id < 4555))
		{
			formato = "5739";
		}
		else if ((id > 4698) && (id < 4701))
		{
			formato = "1331";
		}
		else if ((id > 4701) && (id < 4704))
		{
			formato = "1332";
		}
		else if (id == 6049)
		{
			formato = "0094";
		}
		else
		{
			formato = String.valueOf(id);
		}
		return formato;
	}
	
	static public void main(String[] args)
	{
		new BufferVIP(-1, BufferVIP.class.getSimpleName(), "custom");
		_log.info("Buffer Premium Manager: Enabled.");
	}
}
