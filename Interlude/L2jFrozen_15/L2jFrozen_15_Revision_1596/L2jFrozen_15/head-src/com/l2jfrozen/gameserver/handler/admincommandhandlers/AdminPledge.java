package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.GMViewPledgeInfo;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * <B>Pledge Manipulation:</B><BR>
 * <LI>With target in a character without clan:<BR>
 * //pledge create clanname
 * <LI>With target in a clan leader:<BR>
 * //pledge info<BR>
 * //pledge dismiss<BR>
 * //pledge setlevel level<BR>
 * //pledge rep reputation_points<BR>
 */
public class AdminPledge implements IAdminCommandHandler
{
	private final Logger LOGGER = Logger.getLogger(AdminPledge.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pledge"
	};
	
	private enum CommandEnum
	{
		admin_pledge
	}
	
	private enum ActionEnum
	{
		create,
		dismiss,
		info,
		setlevel,
		rep
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_pledge:
			{
				
				final L2Object target = activeChar.getTarget();
				L2PcInstance player = null;
				
				if (target instanceof L2PcInstance)
				{
					player = (L2PcInstance) target;
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
					showMainPage(activeChar);
					return false;
				}
				
				final String name = player.getName();
				
				ActionEnum action = null;
				String parameter = null;
				
				if (st.hasMoreTokens())
				{
					
					action = ActionEnum.valueOf(st.nextToken()); // create|info|dismiss|setlevel|rep
					
					if (action == null)
					{
						activeChar.sendMessage("Not allowed Action on Clan");
						showMainPage(activeChar);
						return false;
					}
				}
				
				if (action != ActionEnum.create && !player.isClanLeader())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addString(name));
					showMainPage(activeChar);
					return false;
				}
				
				if (st.hasMoreTokens())
				{
					parameter = st.nextToken(); // clanname|nothing|nothing|level|rep_points
				}
				if (action != null)
				{
					switch (action)
					{
						
						case create:
						{
							
							if (parameter == null || parameter.length() == 0)
							{
								activeChar.sendMessage("Please, enter clan name.");
								showMainPage(activeChar);
								return false;
							}
							
							final long cet = player.getClanCreateExpiryTime();
							
							player.setClanCreateExpiryTime(0);
							
							final L2Clan clan = ClanTable.getInstance().createClan(player, parameter);
							
							if (clan != null)
							{
								activeChar.sendMessage("Clan " + parameter + " created. Leader: " + player.getName());
								return true;
							}
							player.setClanCreateExpiryTime(cet);
							activeChar.sendMessage("There was a problem while creating the clan.");
							showMainPage(activeChar);
							return false;
						}
						case dismiss:
						{
							
							ClanTable.getInstance().destroyClan(player.getClanId());
							
							final L2Clan clan = player.getClan();
							
							if (clan == null)
							{
								activeChar.sendMessage("Clan disbanded.");
								return true;
							}
							activeChar.sendMessage("There was a problem while destroying the clan.");
							showMainPage(activeChar);
							return false;
						}
						case info:
						{
							
							activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
							return true;
							
						}
						case rep:
						{
							
							if (parameter == null)
							{
								activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							
							int points = player.getClan().getReputationScore();
							
							try
							{
								
								points = Integer.parseInt(parameter);
								
							}
							catch (final NumberFormatException nfe)
							{
								activeChar.sendMessage("Points incorrect.");
								activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							
							L2Clan clan = player.getClan();
							
							if (clan.getLevel() < 5)
							{
								activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.");
								showMainPage(activeChar);
								return false;
							}
							
							clan.setReputationScore(clan.getReputationScore() + points, true);
							activeChar.sendMessage("You " + (points > 0 ? "add " : "remove ") + Math.abs(points) + " points " + (points > 0 ? "to " : "from ") + clan.getName() + "'s reputation. Their current score is " + clan.getReputationScore());
							clan = null;
							return true;
							
						}
						case setlevel:
						{
							
							if (parameter == null)
							{
								activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							
							int level = player.getClan().getLevel();
							
							try
							{
								
								level = Integer.parseInt(parameter);
								
							}
							catch (final NumberFormatException nfe)
							{
								activeChar.sendMessage("Level incorrect.");
								activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
								showMainPage(activeChar);
								return false;
							}
							
							if (level >= 0 && level < 9)
							{
								
								player.getClan().changeLevel(level);
								activeChar.sendMessage("You set level " + level + " for clan " + player.getClan().getName());
								return true;
							}
							
							activeChar.sendMessage("Level incorrect.");
							activeChar.sendMessage("Usage: //pledge <setlevel|rep> <number>");
							showMainPage(activeChar);
							return false;
						}
						default:
						{
							activeChar.sendMessage("Clan Action not allowed...");
							showMainPage(activeChar);
							return false;
						}
					}
				}
				if (Config.DEBUG)
				{
					LOGGER.debug("fixme:action is null");
				}
			}
			default:
			{
				
				activeChar.sendMessage("Clan command not allowed");
				showMainPage(activeChar);
				return false;
				
			}
		}
		
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void showMainPage(final L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
	}
	
}
