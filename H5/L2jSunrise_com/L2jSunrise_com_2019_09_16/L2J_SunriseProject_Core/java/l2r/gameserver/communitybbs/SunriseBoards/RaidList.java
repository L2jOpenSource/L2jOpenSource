package l2r.gameserver.communitybbs.SunriseBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2r.L2DatabaseFactory;

import gr.sr.configsEngine.configs.impl.SmartCommunityConfigs;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class RaidList extends AbstractSunriseBoards
{
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load(String rfid)
	{
		_list.setLength(0);
		int type = Integer.parseInt(rfid);
		int stpoint = 0;
		int pos = 0;
		String sort = "";
		if (SmartCommunityConfigs.RAID_LIST_SORT_ASC)
		{
			sort = "ASC";
		}
		else
		{
			sort = "DESC";
		}
		for (int count = 1; count != type; count++)
		{
			stpoint += SmartCommunityConfigs.RAID_LIST_RESULTS;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT id, name, level FROM npc WHERE type='L2RaidBoss' AND EXISTS (SELECT * FROM raidboss_spawnlist WHERE raidboss_spawnlist.boss_id = npc.id) ORDER BY `level` " + sort + " Limit " + stpoint + ", " + SmartCommunityConfigs.RAID_LIST_RESULTS);
			ResultSet result = statement.executeQuery();
			pos = stpoint;
			
			while (result.next())
			{
				int npcid = result.getInt("id");
				String npcname = result.getString("name");
				int rlevel = result.getInt("level");
				PreparedStatement statement2 = con.prepareStatement("SELECT respawn_time, respawn_delay, respawn_random FROM raidboss_spawnlist WHERE boss_id=" + npcid);
				ResultSet result2 = statement2.executeQuery();
				
				while (result2.next())
				{
					pos++;
					boolean rstatus = false;
					long respawn = result2.getLong("respawn_time");
					if (respawn == 0)
					{
						rstatus = true;
					}
					int mindelay = result2.getInt("respawn_delay");
					int maxdelay = result2.getInt("respawn_random");
					mindelay = mindelay / 60 / 60;
					maxdelay = maxdelay / 60 / 60;
					addRaidToList(pos, npcname, rlevel, mindelay, maxdelay, rstatus);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addRaidToList(int pos, String npcname, int rlevel, int mindelay, int maxdelay, boolean rstatus)
	{
		_list.append("<table border=0 cellspacing=0 cellpadding=0  bgcolor=111111 width=680 height=" + SmartCommunityConfigs.RAID_LIST_ROW_HEIGHT + ">");
		_list.append("<tr>");
		_list.append("<td FIXWIDTH=5></td>");
		_list.append("<td FIXWIDTH=20>" + pos + "</td>");
		_list.append("<td FIXWIDTH=270>" + npcname + "</td>");
		_list.append("<td FIXWIDTH=50>" + rlevel + "</td>");
		_list.append("<td FIXWIDTH=120 align=center>" + mindelay + " - " + maxdelay + "</td>");
		_list.append("<td FIXWIDTH=50 align=center>" + ((rstatus) ? "<font color=99FF00>Alive</font>" : "<font color=CC0000>Dead</font>") + "</td>");
		_list.append("<td FIXWIDTH=5></td>");
		_list.append("</tr>");
		_list.append("</table>");
		_list.append("<img src=\"L2UI.Squaregray\" width=\"680\" height=\"1\">");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static RaidList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidList _instance = new RaidList();
	}
}
