/*
 * Copyright (C) 2004-2019 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.ChristmasIsHere;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.event.LongTimeEvent;

/**
 * @Reworked MaGa
 */
public class ChristmasIsHere extends LongTimeEvent
{
	// npc
	private static final int SANTA_CLAUS = 13184;
	
	public ChristmasIsHere()
	{
		super(ChristmasIsHere.class.getSimpleName(), "events");
		addStartNpc(SANTA_CLAUS);
		addFirstTalkId(SANTA_CLAUS);
		addTalkId(SANTA_CLAUS);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "13184.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if (event.equalsIgnoreCase("getprizes"))
		{
			htmltext = "prizes.htm";
		}
		else if (event.equalsIgnoreCase("info"))
		{
			htmltext = "<html><body>Santa:<br>Oh no I don't have my socks. You must go to explore the world and get my socks, then you can get one of my prizes.<br>";
			htmltext += "<br></body></html>";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new ChristmasIsHere();
	}
}