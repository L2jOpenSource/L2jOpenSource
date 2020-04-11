/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import l2r.gameserver.SevenSigns;
import l2r.gameserver.datatables.sql.TeleportLocationTable;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.L2TeleportLocation;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

public class L2DungeonGatekeeperInstance extends L2Npc
{
	public L2DungeonGatekeeperInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2DungeonGatekeeperInstance);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		String filename = SevenSigns.SEVEN_SIGNS_HTML_PATH;
		int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_AVARICE);
		int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_GNOSIS);
		int playerCabal = SevenSigns.getInstance().getPlayerCabal(player.getObjectId());
		boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
		int compWinner = SevenSigns.getInstance().getCabalHighestScore();
		
		if (actualCommand.startsWith("necro"))
		{
			boolean canPort = true;
			if (isSealValidationPeriod)
			{
				if ((compWinner == SevenSigns.CABAL_DAWN) && ((playerCabal != SevenSigns.CABAL_DAWN) || (sealAvariceOwner != SevenSigns.CABAL_DAWN)))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
					canPort = false;
				}
				else if ((compWinner == SevenSigns.CABAL_DUSK) && ((playerCabal != SevenSigns.CABAL_DUSK) || (sealAvariceOwner != SevenSigns.CABAL_DUSK)))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
					canPort = false;
				}
				else if ((compWinner == SevenSigns.CABAL_NULL) && (playerCabal != SevenSigns.CABAL_NULL))
				{
					canPort = true;
				}
				else if (playerCabal == SevenSigns.CABAL_NULL)
				{
					canPort = false;
				}
			}
			else
			{
				if (playerCabal == SevenSigns.CABAL_NULL)
				{
					canPort = false;
				}
			}
			
			if (!canPort)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				filename += "necro_no.htm";
				html.setFile(player.getHtmlPrefix(), filename);
				player.sendPacket(html);
			}
			else
			{
				doTeleport(player, Integer.parseInt(st.nextToken()));
				player.setIsIn7sDungeon(true);
			}
		}
		else if (actualCommand.startsWith("cata"))
		{
			boolean canPort = true;
			if (isSealValidationPeriod)
			{
				if ((compWinner == SevenSigns.CABAL_DAWN) && ((playerCabal != SevenSigns.CABAL_DAWN) || (sealGnosisOwner != SevenSigns.CABAL_DAWN)))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN);
					canPort = false;
				}
				else if ((compWinner == SevenSigns.CABAL_DUSK) && ((playerCabal != SevenSigns.CABAL_DUSK) || (sealGnosisOwner != SevenSigns.CABAL_DUSK)))
				{
					player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK);
					canPort = false;
				}
				else if ((compWinner == SevenSigns.CABAL_NULL) && (playerCabal != SevenSigns.CABAL_NULL))
				{
					canPort = true;
				}
				else if (playerCabal == SevenSigns.CABAL_NULL)
				{
					canPort = false;
				}
			}
			else
			{
				if (playerCabal == SevenSigns.CABAL_NULL)
				{
					canPort = false;
				}
			}
			
			if (!canPort)
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				filename += "cata_no.htm";
				html.setFile(player.getHtmlPrefix(), filename);
				player.sendPacket(html);
			}
			else
			{
				doTeleport(player, Integer.parseInt(st.nextToken()));
				player.setIsIn7sDungeon(true);
			}
		}
		else if (actualCommand.startsWith("exit"))
		{
			doTeleport(player, Integer.parseInt(st.nextToken()));
			player.setIsIn7sDungeon(false);
		}
		else if (actualCommand.startsWith("goto"))
		{
			doTeleport(player, Integer.parseInt(st.nextToken()));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	private void doTeleport(L2PcInstance player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			if (player.isAlikeDead())
			{
				return;
			}
			
			player.teleToLocation(list.getX(), list.getY(), list.getZ(), true);
		}
		else
		{
			_log.warn("No teleport destination with id:" + val);
		}
		
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
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
		
		return "data/html/teleporter/" + pom + ".htm";
	}
}