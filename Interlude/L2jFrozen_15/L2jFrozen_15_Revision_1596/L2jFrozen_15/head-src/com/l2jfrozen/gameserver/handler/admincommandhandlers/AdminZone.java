package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.datatables.xml.ZoneData;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author ReynalDev
 */

public class AdminZone implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_zone_check",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command == null)
		{
			return false;
		}
		
		if (activeChar == null)
		{
			return false;
		}
		
		StringTokenizer st = new StringTokenizer(command);
		String actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("admin_zone_check"))
		{
			showHtml(activeChar);
		}
		
		return true;
	}
	
	private void showHtml(L2PcInstance player)
	{
		int x = player.getX();
		int y = player.getY();
		int z = player.getZ();
		
		L2ZoneType zone = ZoneData.getInstance().getZoneByCoordinates(x, y, z);
		
		if (zone == null)
		{
			player.sendMessage("You are not inside a Zone");
			return;
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile("data/html/admin/zone.htm");
		
		html.replace("%ZONEID%", zone.getZoneId());
		html.replace("%ZONENAME%", zone.getZoneName());
		html.replace("%ZONETYPE%", zone.getClass().getSimpleName());
		html.replace("%ZONESHAPE%", zone.getZoneShape().getName());
		html.replace("%MAPREGION%", "[x:" + MapRegionTable.getInstance().getMapRegionX(x) + " y:" + MapRegionTable.getInstance().getMapRegionX(y) + "]");
		html.replace("%CLOSESTTOWN%", MapRegionTable.getInstance().getClosestTownName(player));
		html.replace("%CURRENTLOC%", x + ", " + y + ", " + player.getZ());
		
		player.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
