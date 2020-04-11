package l2r.gameserver.communitybbs.ReunionBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import javolution.text.TextBuilder;
import l2r.L2DatabaseFactory;

public class CastleStatus
{
	private final TextBuilder _playerList = new TextBuilder();
	
	public CastleStatus()
	{
		loadFromDB();
	}
	
	private void loadFromDB()
	{
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
		_playerList.append("<table border=0 cellspacing=0 cellpadding=2 width=480>");
		_playerList.append("<tr>");
		_playerList.append("<td align=center FIXWIDTH=120>" + name + "</td>");
		_playerList.append("<td align=center FIXWIDTH=60>" + tax + "</td>");
		_playerList.append("<td align=center FIXWIDTH=120>" + owner + "</td>");
		_playerList.append("<td align=center FIXWIDTH=155>" + siegeDate + "</td>");
		_playerList.append("</tr>");
		_playerList.append("</table>");
		_playerList.append("<img src=\"L2UI.Squaregray\" width=\"480\" height=\"1\">");
	}
	
	public String loadCastleList()
	{
		return _playerList.toString();
	}
}
