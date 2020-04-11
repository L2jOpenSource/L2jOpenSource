package l2r.gameserver.communitybbs.ReunionBoards;

import javolution.text.TextBuilder;
import gr.reunion.configsEngine.SmartCommunityConfigs;
import gr.reunion.dataHolder.PlayersTopData;
import gr.reunion.datatables.CustomTable;


public class TopPkPlayers
{
	private int _counter = 1;
	private final TextBuilder _topPk = new TextBuilder();
	
	public TopPkPlayers(String file)
	{
		loadDB(file);
	}
	
	private void loadDB(String file)
	{
		for (PlayersTopData playerData : CustomTable.getInstance().getTopPk())
		{
			if (getCounter() <= SmartCommunityConfigs.TOP_PLAYER_RESULTS)
			{
				String name = playerData.getCharName();
				String cName = playerData.getClanName();
				int pk = playerData.getPk();
				
				addChar(name, cName, pk);
				setCounter(getCounter() + 1);
			}
		}
	}
	
	public String loadTopList()
	{
		return _topPk.toString();
	}
	
	private void addChar(String name, String cname, int pk)
	{
		_topPk.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=111111 width=750>");
		_topPk.append("<tr>");
		_topPk.append("<td FIXWIDTH=40>" + getCounter() + "</td");
		_topPk.append("<td fixwidth=160>" + name + "</td");
		_topPk.append("<td fixwidth=160>" + cname + "</td>");
		_topPk.append("<td fixwidth=80>" + pk + "</td>");
		_topPk.append("</tr></table><img src=\"L2UI.Squaregray\" width=\"735\" height=\"1\">");
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