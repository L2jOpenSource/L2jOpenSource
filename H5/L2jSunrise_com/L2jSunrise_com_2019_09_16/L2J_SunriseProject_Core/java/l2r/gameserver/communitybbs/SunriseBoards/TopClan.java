package l2r.gameserver.communitybbs.SunriseBoards;

import l2r.gameserver.data.sql.ClanTable;

import gr.sr.configsEngine.configs.impl.SmartCommunityConfigs;
import gr.sr.dataHolder.PlayersTopData;
import gr.sr.main.TopListsLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class TopClan extends AbstractSunriseBoards
{
	private int _counter = 1;
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load()
	{
		_list.setLength(0);
		_counter = 1;
		
		for (PlayersTopData playerData : TopListsLoader.getInstance().getTopClan())
		{
			if (_counter <= SmartCommunityConfigs.TOP_PLAYER_RESULTS)
			{
				String leaderName = ClanTable.getInstance().getClanByName(playerData.getClanName()).getLeader().getName();
				int clanReputation = ClanTable.getInstance().getClanByName(playerData.getClanName()).getReputationScore();
				addClanToList(playerData.getClanName(), leaderName, playerData.getClanLevel(), clanReputation);
				_counter++;
			}
		}
	}
	
	private void addClanToList(String clan, String leadername, int clanlevel, int reputation)
	{
		_list.append("<table width=680 height=16 bgcolor=111111 border=0 cellspacing=0 cellpadding=0>");
		_list.append("<tr>");
		_list.append("<td FIXWIDTH=40>" + _counter + "</td");
		_list.append("<td fixwidth=90>" + clan + "</td");
		_list.append("<td fixwidth=85>" + leadername + "</td>");
		_list.append("<td fixwidth=45>" + clanlevel + "</td>");
		_list.append("<td  align=center FIXWIDTH=70>" + reputation + "</td>");
		_list.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"680\" height=\"1\">");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static TopClan getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TopClan _instance = new TopClan();
	}
}
