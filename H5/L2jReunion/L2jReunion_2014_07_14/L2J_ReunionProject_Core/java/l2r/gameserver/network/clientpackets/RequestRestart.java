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
package l2r.gameserver.network.clientpackets;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javolution.util.FastList;
import l2r.Config;
import l2r.gameserver.SevenSignsFestival;
import l2r.gameserver.enums.PrivateStoreType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.AntiFeedManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.L2GameClient.GameClientState;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CharSelectionInfo;
import l2r.gameserver.network.serverpackets.RestartResponse;
import l2r.gameserver.scripting.scriptengine.listeners.player.PlayerDespawnListener;
import l2r.gameserver.taskmanager.AttackStanceTaskManager;
import gr.reunion.configsEngine.AntibotConfigs;
import gr.reunion.interf.ReunionEvents;
import gr.reunion.protection.Protection;
import gr.reunion.protection.network.ProtectionManager;

/**
 * This class ...
 * @version $Revision: 1.11.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestart extends L2GameClientPacket
{
	private static final String _C__57_REQUESTRESTART = "[C] 57 RequestRestart";
	protected static final Logger _logAccounting = Logger.getLogger("accounting");
	private static List<PlayerDespawnListener> despawnListeners = new FastList<>();
	
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if ((player.getActiveEnchantItemId() != L2PcInstance.ID_NONE) || (player.getActiveEnchantAttrItemId() != L2PcInstance.ID_NONE))
		{
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isLocked())
		{
			_log.warn("Player " + player.getName() + " tried to restart during class change.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.getPrivateStoreType() != PrivateStoreType.NONE)
		{
			player.sendMessage("Cannot restart while trading");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Chaotic Zone
		if (player.isInsideZone(ZoneIdType.ZONE_CHAOTIC))
		{
			player.sendMessage("Cannot restart while inside chaotic zone.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Flag Zone
		if (player.isInsideZone(ZoneIdType.FLAG))
		{
			player.sendMessage("Cannot restart while inside flag zone.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Antibot farm system
		if (AntibotConfigs.ENABLE_ANTIBOT_FARM_SYSTEM && player.isFarmBot())
		{
			player.sendMessage("Cannot restart cause of antibot protection.");
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if (Config.DEBUG)
			{
				_logger.fine("Player " + player.getName() + " tried to logout while fighting.");
			}
			
			player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
			sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (ReunionEvents.isInEvent(player) && !player.isGM())
		{
			player.sendMessage("A superior power doesn't allow you to leave the event");
			player.sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendMessage("You cannot restart while in Olympiad.");
			player.sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		if (player.inObserverMode())
		{
			player.sendMessage("You cannot restart while in Olympiad.");
			player.sendPacket(RestartResponse.valueOf(false));
			return;
		}
		
		// Prevent player from restarting if they are a festival participant
		// and it is in progress, otherwise notify party members that the player
		// is not longer a participant.
		if (player.isFestivalParticipant())
		{
			if (SevenSignsFestival.getInstance().isFestivalInitialized())
			{
				player.sendMessage("You cannot restart while you are a participant in a festival.");
				sendPacket(RestartResponse.valueOf(false));
				return;
			}
			
			final L2Party playerParty = player.getParty();
			
			if (playerParty != null)
			{
				player.getParty().broadcastString(player.getName() + " has been removed from the upcoming festival.");
			}
		}
		
		// Remove player from Boss Zone
		player.removeFromBossZone();
		
		final L2GameClient client = getClient();
		
		LogRecord record = new LogRecord(Level.INFO, "Logged out");
		record.setParameters(new Object[]
		{
			client
		});
		_logAccounting.log(record);
		
		// detach the client from the char so that the connection isnt closed in the deleteMe
		player.setClient(null);
		
		player.deleteMe();
		
		client.setActiveChar(null);
		AntiFeedManager.getInstance().onDisconnect(client);
		
		// return the client to the authed status
		client.setState(GameClientState.AUTHED);
		
		if (Protection.isProtectionOn())
		{
			ProtectionManager.scheduleSendPacketToClient(0L, player);
		}
		Protection.doDisconection(getClient());
		
		sendPacket(RestartResponse.valueOf(true));
		
		// send char list
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
	
	@Override
	public String getType()
	{
		return _C__57_REQUESTRESTART;
	}
	
	// Listeners
	/**
	 * Adds a despawn listener which will get triggered when a player despawns
	 * @param listener
	 */
	public static void addDespawnListener(PlayerDespawnListener listener)
	{
		if (!despawnListeners.contains(listener))
		{
			despawnListeners.add(listener);
		}
	}
	
	/**
	 * Removes a despawn listener
	 * @param listener
	 */
	public static void removeDespawnListener(PlayerDespawnListener listener)
	{
		despawnListeners.remove(listener);
	}
}