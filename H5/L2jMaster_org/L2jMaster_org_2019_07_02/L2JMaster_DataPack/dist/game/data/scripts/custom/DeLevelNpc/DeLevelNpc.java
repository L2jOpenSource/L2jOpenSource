/* Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.DeLevelNpc;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.data.xml.impl.ExperienceData;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Eze
 */
public class DeLevelNpc extends AbstractNpcAI
{
	private final int npcId = 1010;
	private final int itemcantidad = 5;
	private final int itemid = 23003;
	
	public DeLevelNpc(int i, String string, String string2)
	{
		super(DeLevelNpc.class.getSimpleName(), "custom");
		
		addStartNpc(npcId);
		addFirstTalkId(npcId);
		addTalkId(npcId);
		
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		
		return "main.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmlText = event;
		
		if (event.equalsIgnoreCase("DeLevel"))
		{
			try
			{
				if (player.getLevel() <= 1)
				{
					return htmlText = "You're lvl 1 You can not lose more.";
				}
				if ((player.getLevel() <= 40) && player.isSubClassActive())
				{
					return htmlText = "Lvl 40 you can not lose more being in subclass.";
				}
				if (player.getInventory().getInventoryItemCount(itemid, -1) < itemcantidad)
				{
					player.sendMessage("Incorrect item count.");
					return htmlText = null;
				}
				
				long expplayer = player.getExp();
				long exp = ExperienceData.getInstance().getExpForLevel(player.getLevel() - 1);
				
				takeItems(player, itemid, itemcantidad);
				
				player.removeExpAndSp(expplayer - exp, 0);
				
				player.sendMessage(itemcantidad + " Vote Coins They have been eliminated");
				player.sendMessage("You have downloaded a level.");
				
				htmlText = null;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return htmlText;
	}
	
	public static void main(String[] args)
	{
		new DeLevelNpc(-1, "DeLevelNpc", "custom");
		_log.info("DeLevel Manager: Enabled.");
	}
}