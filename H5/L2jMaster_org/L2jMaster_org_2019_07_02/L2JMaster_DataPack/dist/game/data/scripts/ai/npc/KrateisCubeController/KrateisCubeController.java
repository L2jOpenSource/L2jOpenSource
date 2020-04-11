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
package ai.npc.KrateisCubeController;

import com.l2jserver.gameserver.instancemanager.KrateisCubeManager;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.KrateisCubeEngine;
import com.l2jserver.gameserver.model.olympiad.OlympiadManager;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExPVPMatchCCRetire;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

import ai.npc.AbstractNpcAI;

/**
 * Krateis Cube Controller.
 * @author U3Games
 */
public class KrateisCubeController extends AbstractNpcAI
{
	// Npc Manager
	private static final int MATCH_MANAGER = 32503;
	
	// Npc Instance
	private static final int ENTRANCE_MANAGER_LVL_70 = 32504;
	private static final int ENTRANCE_MANAGER_LVL_76 = 32505;
	private static final int ENTRANCE_MANAGER_LVL_80 = 32506;
	
	private KrateisCubeController()
	{
		super(KrateisCubeController.class.getSimpleName(), "ai/npc");
		addStartNpc(MATCH_MANAGER, ENTRANCE_MANAGER_LVL_70, ENTRANCE_MANAGER_LVL_76, ENTRANCE_MANAGER_LVL_80);
		addTalkId(MATCH_MANAGER, ENTRANCE_MANAGER_LVL_70, ENTRANCE_MANAGER_LVL_76, ENTRANCE_MANAGER_LVL_80);
		addFirstTalkId(MATCH_MANAGER, ENTRANCE_MANAGER_LVL_70, ENTRANCE_MANAGER_LVL_76, ENTRANCE_MANAGER_LVL_80);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (npc.getId() == MATCH_MANAGER)
		{
			switch (event)
			{
				case "checkState":
				{
					if (KrateisCubeManager.getInstance().checkIsRegistered(player))
					{
						htmltext = "32503-04.htm";
					}
					else
					{
						if (KrateisCubeManager.getInstance().isTimeToRegister())
						{
							if (checkConditions(player))
							{
								int arena = checkArena(player);
								KrateisCubeManager.getInstance().registerPlayer(player, arena);
								htmltext = "32503-03.htm";
							}
						}
						else
						{
							htmltext = "32503-02.htm";
						}
					}
					
					break;
				}
				case "register":
				{
					if (checkConditions(player))
					{
						int arena = checkArena(player);
						KrateisCubeManager.getInstance().registerPlayer(player, arena);
						htmltext = "32503-03.htm";
					}
					
					break;
				}
				case "remove":
				{
					if (KrateisCubeManager.getInstance().checkIsRegistered(player))
					{
						KrateisCubeManager.getInstance().unregisterPlayer(player);
						htmltext = "32503-05.htm";
					}
					else
					{
						player.sendPacket(SystemMessageId.NAME_NOT_REGISTERED_ON_CONTACT_LIST);
					}
					
					break;
				}
			}
		}
		
		if ((npc.getId() == ENTRANCE_MANAGER_LVL_70) || (npc.getId() == ENTRANCE_MANAGER_LVL_76) || (npc.getId() == ENTRANCE_MANAGER_LVL_80))
		{
			switch (event)
			{
				case "exit":
				{
					if (KrateisCubeManager.checkIsInsided(player))
					{
						// Delete player
						KrateisCubeManager.getInstance().unregisterPlayer(player);
						
						// Effect
						player.getEffectList().stopAllEffects();
						player.stopAllEffects();
						
						// Teleport
						player.setInstanceId(0);
						player.teleToLocation(-70381, -70937, -1428, 0, 0);
						
						// Screen
						player.sendPacket(new ExPVPMatchCCRetire());
						
						// Update
						player.broadcastStatusUpdate();
						player.broadcastUserInfo();
					}
					else
					{
						player.teleToLocation(-70381, -70937, -1428, 0, 0);
					}
					
					break;
				}
				case "teleport":
				{
					if (KrateisCubeManager.checkIsInsided(player))
					{
						if (KrateisCubeEngine.getInstance().isActive())
						{
							KrateisCubeManager.getInstance().teleportToInstance(player);
						}
						else
						{
							return null;
						}
					}
					else
					{
						player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
						player.teleToLocation(-70381, -70937, -1428, 0, 0);
					}
					
					break;
				}
			}
		}
		
		return htmltext;
	}
	
	/**
	 * Check Arena for player level.
	 * @param player
	 * @return
	 */
	private int checkArena(L2PcInstance player)
	{
		int arena = 0;
		if ((player.getLevel() >= 70) && (player.getLevel() <= 75))
		{
			arena = 1;
		}
		else if ((player.getLevel() >= 76) && (player.getLevel() <= 79))
		{
			arena = 2;
		}
		else if (player.getLevel() >= 80)
		{
			arena = 3;
		}
		
		return arena;
	}
	
	/**
	 * Conditions to register in event.
	 * @param player
	 * @return
	 */
	private boolean checkConditions(L2PcInstance player)
	{
		if (KrateisCubeManager.getInstance().checkIsRegistered(player))
		{
			player.sendPacket(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST);
			return false;
		}
		
		if (player.getLevel() < 70)
		{
			player.sendPacket(SystemMessageId.C1_S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED);
			return false;
		}
		
		if (player.isDead() || player.isAlikeDead())
		{
			player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
			return false;
		}
		
		if (player.isInCombat())
		{
			player.sendPacket(SystemMessageId.UNABLE_COMBAT_PLEASE_GO_RESTART);
			return false;
		}
		
		if (player.getPvpFlag() > 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
			return false;
		}
		
		if (player.getKarma() > 0)
		{
			player.sendPacket(SystemMessageId.CANNOT_ENTER_CAUSE_DONT_MATCH_REQUIREMENTS);
			return false;
		}
		
		if ((player.getInventoryLimit() * 0.8) <= player.getInventory().getSize())
		{
			player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
			return false;
		}
		
		if (player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMessageId.CANNOT_REGISTER_PROCESSING_CURSED_WEAPON);
			return false;
		}
		
		if (player.isOnEvent() || player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.CAN_BE_USED_DURING_QUEST_EVENT_PERIOD);
			return false;
		}
		
		if (OlympiadManager.getInstance().isRegistered(player))
		{
			OlympiadManager.getInstance().unRegisterNoble(player);
			player.sendPacket(SystemMessageId.COLISEUM_OLYMPIAD_KRATEIS_APPLICANTS_CANNOT_PARTICIPATE);
			return false;
		}
		
		if (KrateisCubeManager.getInstance().checkIsRegistered(player))
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_REGISTERED_ON_THE_MATCH_WAITING_LIST);
			msg.addCharName(player);
			player.sendPacket(msg);
			return false;
		}
		
		int arena = checkArena(player);
		if (!KrateisCubeManager.checkMaxPlayersArena(arena))
		{
			player.sendPacket(SystemMessageId.CANNOT_REGISTER_CAUSE_QUEUE_FULL);
			return false;
		}
		
		return true;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new KrateisCubeController();
	}
}