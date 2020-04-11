package l2r.gameserver.communitybbs.ReunionBoards;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.text.TextBuilder;
import l2r.L2DatabaseFactory;

public class TopClan
{
	private final TextBuilder _topClan = new TextBuilder();
	private int _counter = 1;
	
	public TopClan(String file)
	{
		loadFromDB(file);
	}
	
	private void loadFromDB(String file)
	{
		int results = 10;
		String leadername = "";
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT clan_name, leader_id, clan_level, reputation_score FROM clan_data ORDER BY `clan_level` desc Limit " + results);
			ResultSet result = statement.executeQuery();
			
			while (result.next())
			{
				String clan = result.getString("clan_name");
				int clanleader = result.getInt("leader_id");
				int clanlevel = result.getInt("clan_level");
				int reputation = result.getInt("reputation_score");
				
				PreparedStatement statement3 = con.prepareStatement("SELECT char_name FROM characters WHERE charId=" + clanleader);
				ResultSet result2 = statement3.executeQuery();
				
				if (result2.next())
				{
					leadername = result2.getString("char_name");
				}
				addClanToList(clan, leadername, clanlevel, reputation);
				
				setCounter(getCounter() + 1);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void addClanToList(String clan, String leadername, int clanlevel, int reputation)
	{
		_topClan.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=111111 width=762>");
		_topClan.append("<tr>");
		_topClan.append("<td FIXWIDTH=40>" + getCounter() + "</td");
		_topClan.append("<td fixwidth=90>" + clan + "</td");
		_topClan.append("<td fixwidth=85>" + leadername + "</td>");
		_topClan.append("<td fixwidth=45>" + clanlevel + "</td>");
		_topClan.append("<td FIXWIDTH=70>" + reputation + "</td>");
		_topClan.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"734\" height=\"1\">");
		
	}
	
	public int getCounter()
	{
		return _counter;
	}
	
	public void setCounter(int counter)
	{
		_counter = counter;
	}
	
	public String loadClanList()
	{
		return _topClan.toString();
	}
}
