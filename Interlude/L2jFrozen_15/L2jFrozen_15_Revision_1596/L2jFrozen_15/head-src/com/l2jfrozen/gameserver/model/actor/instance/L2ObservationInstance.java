package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * @author NightMarez
 */
public class L2ObservationInstance extends L2FolkInstance
{
	public L2ObservationInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You already participated in Olympiad!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.inEventTvT || player.inEventDM || player.inEventCTF)
		{
			player.sendMessage("You already participated in Event!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInCombat() || player.getPvpFlag() > 0)
		{
			player.sendMessage("You are in combat now!");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (command.startsWith("observeSiege"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken(); // Command
			
			int x = Integer.parseInt(st.nextToken()); // X location
			int y = Integer.parseInt(st.nextToken()); // Y location
			int z = Integer.parseInt(st.nextToken()); // Z location
			
			if (SiegeManager.getInstance().getSiege(x, y, z) != null)
			{
				doObserve(player, command);
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ONLY_VIEW_SIEGE));
			}
		}
		else if (command.startsWith("observe"))
		{
			doObserve(player, command);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public String getHtmlPath(final int npcId, final int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/observation/" + pom + ".htm";
	}
	
	private void doObserve(L2PcInstance player, String val)
	{
		StringTokenizer st = new StringTokenizer(val);
		st.nextToken(); // Command
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		int cost = Integer.parseInt(st.nextToken());
		
		if (player.reduceAdena("Broadcast", cost, this, true))
		{
			// enter mode
			player.enterObserverMode(x, y, z);
			final ItemList il = new ItemList(player, false);
			player.sendPacket(il);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
