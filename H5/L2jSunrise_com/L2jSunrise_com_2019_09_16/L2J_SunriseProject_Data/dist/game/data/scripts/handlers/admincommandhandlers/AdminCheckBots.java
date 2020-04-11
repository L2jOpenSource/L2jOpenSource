package handlers.admincommandhandlers;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.xml.impl.ClassListData;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.PageResult;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.util.Comparators;
import l2r.gameserver.util.HtmlUtil;

public class AdminCheckBots implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_check_bots",
		"admin_real_onlines",
		"admin_check_farm_bots",
		"admin_check_enchant_bots"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		int farmBotsCount = 0;
		int enchantBotsCount = 0;
		for (L2PcInstance bots : L2World.getInstance().getPlayers())
		{
			if (bots.isFarmBot())
			{
				farmBotsCount++;
			}
			if (bots.isEnchantBot())
			{
				enchantBotsCount++;
			}
		}
		
		if (command.startsWith("admin_check_bots"))
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage();
			adminReply.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/botsystem.htm");
			
			adminReply.replace("%farmbots%", String.valueOf(farmBotsCount));
			adminReply.replace("%enchantbots%", String.valueOf(enchantBotsCount));
			activeChar.sendPacket(adminReply);
		}
		if (command.startsWith("admin_check_farm_bots"))
		{
			if (farmBotsCount == 0)
			{
				activeChar.sendMessage("There are no currently farm bots. Try again later.");
				return false;
			}
			showBots(activeChar, 0, "farm");
		}
		if (command.startsWith("admin_check_enchant_bots"))
		{
			if (enchantBotsCount == 0)
			{
				activeChar.sendMessage("There are no currently enchant bots. Try again later.");
				return false;
			}
			showBots(activeChar, 0, "enchant");
		}
		
		if (command.startsWith("admin_real_onlines"))
		{
			int counter = 1;
			
			for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
			{
				boolean addToList = true;
				if ((activeChar != onlinePlayer) && onlinePlayer.isOnline() && ((onlinePlayer.getClient() != null) && !onlinePlayer.getClient().isDetached()))
				{
					String[] player_Ip = new String[2];
					player_Ip[0] = onlinePlayer.getClient().getConnection().getInetAddress().getHostAddress();
					int[][] trace1 = onlinePlayer.getClient().getTrace();
					
					// Just in case could not trace
					if (trace1 != null)
					{
						for (int o = 0; o < trace1[0].length; o++)
						{
							player_Ip[1] = player_Ip[1] + trace1[0][o];
							if (o != (trace1[0].length - 1))
							{
								player_Ip[1] = player_Ip[1] + ".";
							}
						}
						
						if (getAddedIps() != null)
						{
							for (String[] listIps : getAddedIps())
							{
								if (player_Ip[0].equals(listIps[0]) && player_Ip[1].equals(listIps[1]))
								{
									addToList = false;
								}
							}
						}
						
						if (addToList)
						{
							addToList(onlinePlayer);
							counter++;
						}
					}
				}
			}
			
			if (getAddedIps() != null)
			{
				_dualboxCheck.clear();
			}
			
			activeChar.sendMessage("There are " + counter + " real players.");
		}
		return false;
	}
	
	private void showBots(L2PcInstance activeChar, int page, final String type)
	{
		final L2PcInstance[] players = L2World.getInstance().getPlayersSortedBy(Comparators.PLAYER_UPTIME_COMPARATOR);
		
		NpcHtmlMessage html = new NpcHtmlMessage();
		if (type.equals("farm"))
		{
			html.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/farmbotlist.htm");
		}
		else if (type.equals("enchant"))
		{
			html.setFile(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/enchantbotlist.htm");
		}
		
		final PageResult result = HtmlUtil.createPage(players, page, 20, i ->
		{
			String whatToReturn = null;
			if (type.equals("farm"))
			{
				whatToReturn = "<td align=center><a action=\"bypass -h admin_check_farm_bots " + i + "\">Page " + (i + 1) + "</a></td>";
			}
			else if (type.equals("enchant"))
			{
				whatToReturn = "<td align=center><a action=\"bypass -h admin_check_enchant_bots " + i + "\">Page " + (i + 1) + "</a></td>";
			}
			return whatToReturn;
		} , player ->
		{
			StringBuilder sb = new StringBuilder();
			String typeToSend = null;
			sb.append("<tr>");
			sb.append("<td width=80><a action=\"bypass -h admin_character_info " + player.getName() + "\">" + player.getName() + "</a></td>");
			
			if (type.equals("farm"))
			{
				typeToSend = ClassListData.getInstance().getClass(player.getClassId()).getClientCode();
			}
			else if (type.equals("enchant"))
			{
				typeToSend = String.valueOf(player.getEnchantChance());
			}
			
			sb.append("<tr><td width=80><a action=\"bypass -h admin_teleportto " + player.getName() + "\">" + player.getName() + "</a></td><td width=110>" + typeToSend + "</td><td width=40>" + String.valueOf(player.getLevel()) + "</td></tr>");
			sb.append("</tr>");
			return sb.toString();
		});
		
		if (result.getPages() > 0)
		{
			html.replace("%pages%", "<table width=280 cellspacing=0><tr>" + result.getPagerTemplate() + "</tr></table>");
		}
		else
		{
			html.replace("%pages%", "");
		}
		
		html.replace("%players%", result.getBodyTemplate().toString());
		activeChar.sendPacket(html);
	}
	
	protected static final Set<String[]> _dualboxCheck = ConcurrentHashMap.newKeySet();
	
	// Dual Box Check pcIp based
	protected static boolean addToList(L2PcInstance player)
	{
		String[] player_Ip = new String[2];
		player_Ip[0] = player.getClient().getConnection().getInetAddress().getHostAddress();
		int[][] trace1 = player.getClient().getTrace();
		for (int o = 0; o < trace1[0].length; o++)
		{
			player_Ip[1] = player_Ip[1] + trace1[0][o];
			if (o != (trace1[0].length - 1))
			{
				player_Ip[1] = player_Ip[1] + ".";
			}
		}
		
		_dualboxCheck.add(player_Ip);
		return true;
	}
	
	public Set<String[]> getAddedIps()
	{
		return _dualboxCheck;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}