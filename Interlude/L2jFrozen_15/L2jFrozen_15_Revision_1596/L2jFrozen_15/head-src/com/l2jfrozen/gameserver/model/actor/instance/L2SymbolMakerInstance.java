package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.datatables.sql.HennaTreeTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.network.serverpackets.HennaEquipList;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;

public class L2SymbolMakerInstance extends L2FolkInstance
{
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (command.equals("Draw"))
		{
			final L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId());
			final HennaEquipList hel = new HennaEquipList(player, henna);
			player.sendPacket(hel);
			
			player.sendPacket(new ItemList(player, false));
		}
		else if (command.equals("RemoveList"))
		{
			showRemoveChat(player);
		}
		else if (command.startsWith("Remove "))
		{
			if (!player.getClient().getFloodProtectors().getTransaction().tryPerformAction("HennaRemove"))
			{
				return;
			}
			
			final int slot = Integer.parseInt(command.substring(7));
			player.removeHenna(slot);
			
			player.sendPacket(new ItemList(player, false));
			
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void showRemoveChat(final L2PcInstance player)
	{
		TextBuilder html1 = new TextBuilder("<html><body>");
		html1.append("Select symbol you would like to remove:<br><br>");
		boolean hasHennas = false;
		
		for (int i = 1; i <= 3; i++)
		{
			final L2HennaInstance henna = player.getHennas(i);
			
			if (henna != null)
			{
				hasHennas = true;
				html1.append("<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>");
			}
		}
		if (!hasHennas)
		{
			html1.append("You don't have any symbol to remove!");
		}
		
		html1.append("</body></html>");
		insertObjectIdAndShowChatWindow(player, html1.toString());
		html1 = null;
	}
	
	public L2SymbolMakerInstance(final int objectID, final L2NpcTemplate template)
	{
		super(objectID, template);
	}
	
	@Override
	public String getHtmlPath(final int npcId, final int val)
	{
		return "data/html/symbolmaker/SymbolMaker.htm";
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
}
