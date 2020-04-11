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
package custom.UpLevelNpc;

import ai.npc.AbstractNpcAI;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Eze
 */
public class UpLevelNpc extends AbstractNpcAI
{
	private final int npcId = 1011;
	private final int itemcantidad = 50;
	private final int itemid = 23003;
	
	public UpLevelNpc(int i, String string, String string2)
	{
		super(UpLevelNpc.class.getSimpleName(), "custom");
		
		addStartNpc(npcId);
		addFirstTalkId(npcId);
		addTalkId(npcId);
		
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		
		return "upmain.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("UpLevel"))
		{
			try
			{
				if (player.getLevel() >= 85)
				{
					return "You're lvl 85 You can not win more.";
				}
				if ((player.getLevel() >= 80) && player.isSubClassActive())
				{
					return "Lvl 80 you can not win more being in subclass.";
				}
				if (player.getInventory().getInventoryItemCount(itemid, -1) < itemcantidad)
				{
					player.sendMessage("You not have correct item.");
					return null;
				}
				
				takeItems(player, itemid, itemcantidad);
				
				player.getStat().addLevel((byte) (1));
				
				player.sendMessage(itemcantidad + " Vote Coins They have been eliminated");
				player.sendMessage("You have win a level.");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return "upmain.htm";
	}
	
	public static void main(String[] args)
	{
		new UpLevelNpc(-1, "UpLevelNpc", "custom");
		_log.info("UpLevelNpc Manager: Enabled.");
	}
}