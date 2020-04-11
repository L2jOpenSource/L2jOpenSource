package l2r.gameserver.communitybbs.SunriseBoards;

import gr.sr.configsEngine.configs.impl.SmartCommunityConfigs;
import gr.sr.dataHolder.PlayersTopData;
import gr.sr.main.TopListsLoader;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class TopPkPlayers extends AbstractSunriseBoards
{
	private int _counter = 1;
	private final StringBuilder _list = new StringBuilder();
	
	@Override
	public void load()
	{
		_list.setLength(0);
		_counter = 1;
		
		for (PlayersTopData playerData : TopListsLoader.getInstance().getTopPk())
		{
			if (_counter <= SmartCommunityConfigs.TOP_PLAYER_RESULTS)
			{
				addChar(playerData.getCharName(), playerData.getClanName(), playerData.getPk());
				_counter++;
			}
		}
	}
	
	private void addChar(String name, String cname, int pk)
	{
		_list.append("<table width=680 bgcolor=111111  height=16 border=0 cellspacing=0 cellpadding=0>");
		_list.append("<tr>");
		_list.append("<td FIXWIDTH=40>" + _counter + "</td");
		_list.append("<td fixwidth=160>" + name + "</td");
		_list.append("<td fixwidth=160>" + cname + "</td>");
		_list.append("<td align=center fixwidth=80>" + pk + "</td>");
		_list.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"680\" height=\"1\">");
	}
	
	@Override
	public String getList()
	{
		return _list.toString();
	}
	
	public static TopPkPlayers getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final TopPkPlayers _instance = new TopPkPlayers();
	}
}