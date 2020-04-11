package l2r.gameserver.communitybbs.SunriseBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2r.L2DatabaseFactory;

import gr.sr.main.ClassNamesHolder;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class HeroeList extends AbstractSunriseBoards
{
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load()
	{
		_list.setLength(0);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT h.played, ch.char_name, ch.base_class, ch.online, cl.clan_name, cl.ally_name FROM heroes h LEFT JOIN characters ch ON ch.charId=h.charId LEFT OUTER JOIN clan_data cl ON cl.clan_id=ch.clanid ORDER BY h.count DESC, ch.char_name ASC LIMIT 20");
			
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				boolean status = false;
				
				if (result.getInt("online") == 1)
				{
					status = true;
				}
				
				addPlayerToList(result.getInt("played"), result.getString("char_name"), result.getInt("base_class"), result.getString("clan_name"), result.getString("ally_name"), status);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addPlayerToList(int played, String name, int ChrClass, String clan, String ally, boolean isOnline)
	{
		_list.append("<table border=0 cellspacing=0 cellpadding=2 width=480>");
		_list.append("<tr>");
		_list.append("<td align=center FIXWIDTH=130>" + name + "</td>");
		_list.append("<td align=center FIXWIDTH=160>" + ClassNamesHolder.getClassName(ChrClass) + "</td>");
		_list.append("<td align=center FIXWIDTH=80>" + played + "</td>");
		_list.append("<td align=center FIXWIDTH=70>" + ((isOnline) ? "<font color=99FF00>Online</font>" : "<font color=CC0000>Offline</font>") + "</td>");
		_list.append("</tr>");
		_list.append("</table>");
		_list.append("<img src=\"L2UI.Squaregray\" width=\"480\" height=\"1\">");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static HeroeList getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final HeroeList _instance = new HeroeList();
	}
}
