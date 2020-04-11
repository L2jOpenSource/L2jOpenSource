package l2r.gameserver.communitybbs.SunriseBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import l2r.L2DatabaseFactory;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class CastleStatus extends AbstractSunriseBoards
{
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load()
	{
		_list.setLength(0);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			for (int i = 1; i < 9; i++)
			{
				PreparedStatement statement = con.prepareStatement("SELECT clan_name FROM clan_data WHERE hasCastle=" + i + ";");
				ResultSet result = statement.executeQuery();
				
				PreparedStatement statement2 = con.prepareStatement("SELECT name, siegeDate, taxPercent FROM castle WHERE id=" + i + ";");
				ResultSet result2 = statement2.executeQuery();
				
				while (result.next())
				{
					String owner = result.getString("clan_name");
					
					while (result2.next())
					{
						String name = result2.getString("name");
						long someLong = result2.getLong("siegeDate");
						int tax = result2.getInt("taxPercent");
						Date anotherDate = new Date(someLong);
						String DATE_FORMAT = "dd-MMM-yyyy HH:mm";
						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
						
						addCastleToList(name, owner, tax, sdf.format(anotherDate));
					}
				}
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addCastleToList(String name, String owner, int tax, String siegeDate)
	{
		_list.append("<table border=0 cellspacing=0 cellpadding=2 width=480>");
		_list.append("<tr>");
		_list.append("<td align=center FIXWIDTH=120>" + name + "</td>");
		_list.append("<td align=center FIXWIDTH=60>" + tax + "</td>");
		_list.append("<td align=center FIXWIDTH=120>" + owner + "</td>");
		_list.append("<td align=center FIXWIDTH=155>" + siegeDate + "</td>");
		_list.append("</tr>");
		_list.append("</table>");
		_list.append("<img src=\"L2UI.Squaregray\" width=\"480\" height=\"1\">");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static CastleStatus getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final CastleStatus _instance = new CastleStatus();
	}
}
