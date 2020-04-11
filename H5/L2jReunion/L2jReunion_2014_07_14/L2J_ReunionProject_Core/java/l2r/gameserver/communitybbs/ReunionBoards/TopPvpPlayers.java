package l2r.gameserver.communitybbs.ReunionBoards;

import javolution.text.TextBuilder;
import gr.reunion.configsEngine.SmartCommunityConfigs;
import gr.reunion.dataHolder.PlayersTopData;
import gr.reunion.datatables.CustomTable;


public class TopPvpPlayers
{
	private int _counter = 1;
	private final TextBuilder _topPvp = new TextBuilder();
	
	public TopPvpPlayers(String file)
	{
		loadDB(file);
	}
	
	private void loadDB(String file)
	{
		for (PlayersTopData playerData : CustomTable.getInstance().getTopPvp())
		{
			if (getCounter() <= SmartCommunityConfigs.TOP_PLAYER_RESULTS)
			{
				String name = playerData.getCharName();
				String cName = playerData.getClanName();
				int pvp = playerData.getPvp();
				
				addChar(name, cName, pvp);
				setCounter(getCounter() + 1);
			}
		}
	}
	
	public String loadTopList()
	{
		return _topPvp.toString();
	}
	
	private void addChar(String name, String cname, int pvp)
	{
		_topPvp.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=111111 width=750>");
		_topPvp.append("<tr>");
		_topPvp.append("<td FIXWIDTH=40>" + getCounter() + "</td");
		_topPvp.append("<td fixwidth=160>" + name + "</td");
		_topPvp.append("<td fixwidth=160>" + cname + "</td>");
		_topPvp.append("<td fixwidth=80>" + pvp + "</td>");
		_topPvp.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"735\" height=\"1\">");
	}
	
	public int getCounter()
	{
		return _counter;
	}
	
	public void setCounter(int counter)
	{
		_counter = counter;
	}
}