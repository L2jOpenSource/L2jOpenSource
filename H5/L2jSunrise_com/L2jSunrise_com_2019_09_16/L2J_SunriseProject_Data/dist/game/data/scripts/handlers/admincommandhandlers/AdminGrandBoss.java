/*
 * Copyright (C) 2004-2015 L2J DataPack
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
package handlers.admincommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.StringTokenizer;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.handler.IAdminCommandHandler;
import l2r.gameserver.instancemanager.GrandBossManager;
import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.zone.type.L2NoRestartZone;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.grandboss.VanHalter;
import ai.grandboss.Antharas.Antharas;
import ai.grandboss.Baium.Baium;
import ai.grandboss.Valakas.Valakas;

/**
 * @author St3eT<br>
 *         Updated by vGodFather 26.01.2018
 */
public class AdminGrandBoss implements IAdminCommandHandler
{
	private static final int ANTHARAS = 29068; // Antharas
	private static final int ANTHARAS_ZONE = 70050; // Antharas Nest
	private static final int VALAKAS = 29028; // Valakas
	private static final int VALAKAS_ZONE = 70052; // Valakas Nest
	private static final int BAIUM = 29020; // Baium
	private static final int BAIUM_ZONE = 70051; // Baium Nest
	private static final int QUEENANT = 29001; // Queen Ant
	private static final int ORFEN = 29014; // Orfen
	private static final int CORE = 29006; // Core
	private static final int VAN_HALTER = 29062; // Van Halter
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_grandboss",
		"admin_grandboss_skip",
		"admin_grandboss_respawn",
		"admin_grandboss_minions",
		"admin_grandboss_abort",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		switch (actualCommand.toLowerCase())
		{
			case "admin_grandboss":
			{
				if (st.hasMoreTokens())
				{
					final int grandBossId = Integer.parseInt(st.nextToken());
					manageHtml(activeChar, grandBossId);
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
					html.setHtml(HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), "data/html/admin/grandbosses/grandboss.htm"));
					activeChar.sendPacket(html);
				}
				break;
			}
			
			case "admin_grandboss_skip":
			{
				if (st.hasMoreTokens())
				{
					final int grandBossId = Integer.parseInt(st.nextToken());
					
					switch (grandBossId)
					{
						case ANTHARAS:
						{
							antharasAi().notifyEvent("SKIP_WAITING", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						case VALAKAS:
						{
							valakasAi().notifyEvent("SKIP_WAITING", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						default:
						{
							activeChar.sendMessage("Wrong ID!");
							break;
						}
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //grandboss_skip Id");
				}
				break;
			}
			case "admin_grandboss_respawn":
			{
				if (st.hasMoreTokens())
				{
					final int grandBossId = Integer.parseInt(st.nextToken());
					
					switch (grandBossId)
					{
						case ANTHARAS:
						{
							antharasAi().notifyEvent("RESPAWN_ANTHARAS", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						case VALAKAS:
						{
							valakasAi().notifyEvent("RESPAWN_VALAKAS", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						case BAIUM:
						{
							baiumAi().notifyEvent("RESPAWN_BAIUM", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						case VAN_HALTER:
						{
							vanHalterAi().notifyEvent("RESPAWN_VAN_HALTER", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						default:
						{
							activeChar.sendMessage("Wrong ID!");
						}
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //grandboss_respawn Id");
				}
				break;
			}
			case "admin_grandboss_minions":
			{
				if (st.hasMoreTokens())
				{
					final int grandBossId = Integer.parseInt(st.nextToken());
					
					switch (grandBossId)
					{
						case ANTHARAS:
						{
							antharasAi().notifyEvent("DESPAWN_MINIONS", null, activeChar);
							break;
						}
						case BAIUM:
						{
							baiumAi().notifyEvent("DESPAWN_MINIONS", null, activeChar);
							break;
						}
						default:
						{
							activeChar.sendMessage("Wrong ID!");
						}
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //grandboss_minions Id");
				}
				break;
			}
			case "admin_grandboss_abort":
			{
				if (st.hasMoreTokens())
				{
					final int grandBossId = Integer.parseInt(st.nextToken());
					
					switch (grandBossId)
					{
						case ANTHARAS:
						{
							antharasAi().notifyEvent("ABORT_FIGHT", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						case VALAKAS:
						{
							valakasAi().notifyEvent("ABORT_FIGHT", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						case BAIUM:
						{
							baiumAi().notifyEvent("ABORT_FIGHT", null, activeChar);
							manageHtml(activeChar, grandBossId);
							break;
						}
						default:
						{
							activeChar.sendMessage("Wrong ID!");
						}
					}
				}
				else
				{
					activeChar.sendMessage("Usage: //grandboss_abort Id");
				}
				break;
			}
		}
		return true;
	}
	
	private void manageHtml(L2PcInstance activeChar, int grandBossId)
	{
		if (Arrays.asList(ANTHARAS, VALAKAS, BAIUM, QUEENANT, ORFEN, CORE, VAN_HALTER).contains(grandBossId))
		{
			final int bossStatus = GrandBossManager.getInstance().getBossStatus(grandBossId);
			L2NoRestartZone bossZone = null;
			String textColor = null;
			String text = null;
			String htmlPatch = null;
			int deadStatus = 0;
			
			switch (grandBossId)
			{
				case ANTHARAS:
				{
					bossZone = ZoneManager.getInstance().getZoneById(ANTHARAS_ZONE, L2NoRestartZone.class);
					htmlPatch = "data/html/admin/grandbosses/grandboss_antharas.htm";
					break;
				}
				case VALAKAS:
				{
					bossZone = ZoneManager.getInstance().getZoneById(VALAKAS_ZONE, L2NoRestartZone.class);
					htmlPatch = "data/html/admin/grandbosses/grandboss_valakas.htm";
					break;
				}
				case BAIUM:
				{
					bossZone = ZoneManager.getInstance().getZoneById(BAIUM_ZONE, L2NoRestartZone.class);
					htmlPatch = "data/html/admin/grandbosses/grandboss_baium.htm";
					break;
				}
				case QUEENANT:
				{
					htmlPatch = "data/html/admin/grandbosses/grandboss_queenant.htm";
					break;
				}
				case ORFEN:
				{
					htmlPatch = "data/html/admin/grandbosses/grandboss_orfen.htm";
					break;
				}
				case CORE:
				{
					htmlPatch = "data/html/admin/grandbosses/grandboss_core.htm";
					break;
				}
				case VAN_HALTER:
				{
					htmlPatch = "data/html/admin/grandbosses/grandboss_vanhalter.htm";
					break;
				}
			}
			
			if (Arrays.asList(ANTHARAS, VALAKAS, BAIUM).contains(grandBossId))
			{
				deadStatus = 3;
				switch (bossStatus)
				{
					case 0:
					{
						textColor = "00FF00"; // Green
						text = "Alive";
						break;
					}
					case 1:
					{
						textColor = "FFFF00"; // Yellow
						text = "Waiting";
						break;
					}
					case 2:
					{
						textColor = "FF9900"; // Orange
						text = "In Fight";
						break;
					}
					case 3:
					{
						textColor = "FF0000"; // Red
						text = "Dead";
						break;
					}
				}
			}
			else
			{
				deadStatus = 1;
				switch (bossStatus)
				{
					case 0:
					{
						textColor = "00FF00"; // Green
						text = "Alive";
						break;
					}
					case 1:
					{
						textColor = "FF0000"; // Red
						text = "Dead";
						break;
					}
				}
			}
			
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(grandBossId);
			final String bossRespawn = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(info.getLong("respawn_time"));
			
			NpcHtmlMessage html = new NpcHtmlMessage(0, 1);
			html.setHtml(HtmCache.getInstance().getHtm(activeChar, activeChar.getHtmlPrefix(), htmlPatch));
			html.replace("%bossStatus%", text);
			html.replace("%bossColor%", textColor);
			html.replace("%respawnTime%", bossStatus == deadStatus ? bossRespawn : "Already respawned!");
			html.replace("%playersInside%", bossZone != null ? String.valueOf(bossZone.getPlayersInside().size()) : "Zone not found!");
			activeChar.sendPacket(html);
		}
		else
		{
			activeChar.sendMessage("Wrong ID!");
		}
	}
	
	private Quest antharasAi()
	{
		return QuestManager.getInstance().getQuest(Antharas.class.getSimpleName());
	}
	
	private Quest valakasAi()
	{
		return QuestManager.getInstance().getQuest(Valakas.class.getSimpleName());
	}
	
	private Quest baiumAi()
	{
		return QuestManager.getInstance().getQuest(Baium.class.getSimpleName());
	}
	
	private Quest vanHalterAi()
	{
		return QuestManager.getInstance().getQuest(VanHalter.class.getSimpleName());
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
