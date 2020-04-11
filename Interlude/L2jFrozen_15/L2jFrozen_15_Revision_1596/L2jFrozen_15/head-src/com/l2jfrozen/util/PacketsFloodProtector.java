package com.l2jfrozen.util;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.controllers.GameTimeController;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.loginserver.L2LoginClient;
import com.l2jfrozen.loginserver.LoginController;
import com.l2jfrozen.loginserver.network.serverpackets.LoginFail.LoginFailReason;
import com.l2jfrozen.netcore.MMOClient;
import com.l2jfrozen.netcore.NetcoreConfig;

/**
 * @author Shyla
 */
public class PacketsFloodProtector
{
	private final static int MAX_CONCURRENT_ACTIONS_PER_PLAYER = 10;
	
	private static Hashtable<String, AtomicInteger> clients_concurrent_actions = new Hashtable<>();
	
	private static final Logger LOGGER = Logger.getLogger(PacketsFloodProtector.class);
	
	private static Hashtable<String, Hashtable<Integer, AtomicInteger>> clients_actions = new Hashtable<>();
	
	private static Hashtable<String, Hashtable<Integer, Integer>> clients_nextGameTick = new Hashtable<>();
	
	private static Hashtable<String, Boolean> punishes_in_progress = new Hashtable<>();
	
	/**
	 * Checks whether the request is flood protected or not.
	 * @param  opcode
	 * @param  opcode2
	 * @param  client
	 * @return         true if action is allowed, otherwise false
	 */
	public static boolean tryPerformAction(final int opcode, final int opcode2, final MMOClient<?> client)
	{
		if (NetcoreConfig.getInstance().DISABLE_FULL_PACKETS_FLOOD_PROTECTOR)
		{
			return true;
		}
		
		// filter on opcodes
		if (!isOpCodeToBeTested(opcode, opcode2, client instanceof L2LoginClient))
		{
			return true;
		}
		
		String account = "";
		
		if (client instanceof L2LoginClient)
		{
			final L2LoginClient login_cl = (L2LoginClient) client;
			account = login_cl.getAccount();
		}
		else if (client instanceof L2GameClient)
		{
			final L2GameClient game_cl = (L2GameClient) client;
			account = game_cl.accountName;
		}
		
		if (account == null)
		{
			return true;
		}
		
		final L2GameClient clientGame = (L2GameClient) client;
		
		// Ignore flood protector for GM char
		if (clientGame != null && clientGame.getActiveChar() != null && clientGame.getActiveChar().isGM())
		{
			return true;
		}
		
		// get actual concurrent actions number for account
		AtomicInteger actions_per_account = clients_concurrent_actions.get(account);
		if (actions_per_account == null)
		{
			actions_per_account = new AtomicInteger(0);
		}
		if (actions_per_account.get() < MAX_CONCURRENT_ACTIONS_PER_PLAYER)
		{
			final int actions = actions_per_account.incrementAndGet();
			
			if (NetcoreConfig.getInstance().ENABLE_MMOCORE_DEBUG)
			{
				LOGGER.info(" -- account " + account + " has performed " + actions + " concurrent actions until now");
			}
			
			clients_concurrent_actions.put(account, actions_per_account);
		}
		else
		{
			return false;
		}
		
		final int curTick = GameTimeController.getGameTicks();
		
		Hashtable<Integer, Integer> account_nextGameTicks = clients_nextGameTick.get(account);
		if (account_nextGameTicks == null)
		{
			account_nextGameTicks = new Hashtable<>();
		}
		Integer nextGameTick = account_nextGameTicks.get(opcode);
		if (nextGameTick == null)
		{
			nextGameTick = curTick;
			account_nextGameTicks.put(opcode, nextGameTick);
		}
		clients_nextGameTick.put(account, account_nextGameTicks);
		
		Boolean punishmentInProgress = punishes_in_progress.get(account);
		if (punishmentInProgress == null)
		{
			punishmentInProgress = false;
		}
		else if (punishmentInProgress)
		{
			final AtomicInteger actions = clients_concurrent_actions.get(account);
			actions.decrementAndGet();
			clients_concurrent_actions.put(account, actions);
			return false;
		}
		punishes_in_progress.put(account, punishmentInProgress);
		
		Hashtable<Integer, AtomicInteger> received_commands_actions = clients_actions.get(account);
		if (received_commands_actions == null)
		{
			received_commands_actions = new Hashtable<>();
		}
		AtomicInteger command_count = null;
		if ((command_count = received_commands_actions.get(opcode)) == null)
		{
			command_count = new AtomicInteger(0);
			received_commands_actions.put(opcode, command_count);
		}
		clients_actions.put(account, received_commands_actions);
		
		if (curTick <= nextGameTick && !punishmentInProgress) // time to check operations
		{
			command_count.incrementAndGet();
			clients_actions.get(account).put(opcode, command_count);
			
			if (NetcoreConfig.getInstance().ENABLE_MMOCORE_DEBUG)
			{
				LOGGER.info("-- called OpCode " + Integer.toHexString(opcode) + " ~" + String.valueOf((NetcoreConfig.getInstance().FLOOD_PACKET_PROTECTION_INTERVAL - (nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK) + " ms after first command...");
				LOGGER.info("   total received packets with OpCode " + Integer.toHexString(opcode) + " into the Interval: " + command_count.get());
			}
			
			if (NetcoreConfig.getInstance().PACKET_FLOODING_PUNISHMENT_LIMIT > 0 && command_count.get() >= NetcoreConfig.getInstance().PACKET_FLOODING_PUNISHMENT_LIMIT && NetcoreConfig.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE != null)
			{
				punishes_in_progress.put(account, true);
				
				if (!isOpCodeToBeTested(opcode, opcode2, client instanceof L2LoginClient))
				{
					if (NetcoreConfig.getInstance().LOG_PACKET_FLOODING)
					{
						LOGGER.warn("ATTENTION: Account " + account + " is flooding the server...");
					}
					
					if ("kick".equals(NetcoreConfig.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE))
					{
						if (NetcoreConfig.getInstance().LOG_PACKET_FLOODING)
						{
							LOGGER.warn(" ------- kicking account " + account);
						}
						kickPlayer(client, opcode);
					}
					else if ("ban".equals(NetcoreConfig.getInstance().PACKET_FLOODING_PUNISHMENT_TYPE))
					{
						if (NetcoreConfig.getInstance().LOG_PACKET_FLOODING)
						{
							LOGGER.warn(" ------- banning account " + account);
						}
						banAccount(client, opcode);
					}
				}
				// clear already punished account
				punishes_in_progress.remove(account);
				clients_nextGameTick.remove(account);
				clients_actions.remove(account);
				clients_concurrent_actions.remove(account);
				
				return false;
			}
			
			if (curTick == nextGameTick)
			{ // if is the first time, just calculate the next game tick
				nextGameTick = curTick + NetcoreConfig.getInstance().FLOOD_PACKET_PROTECTION_INTERVAL;
				clients_nextGameTick.get(account).put(opcode, nextGameTick);
			}
			
			final AtomicInteger actions = clients_concurrent_actions.get(account);
			actions.decrementAndGet();
			clients_concurrent_actions.put(account, actions);
			
			return true;
		}
		punishes_in_progress.put(account, false);
		clients_nextGameTick.get(account).remove(opcode);
		clients_actions.get(account).remove(opcode);
		
		final AtomicInteger actions = clients_concurrent_actions.get(account);
		actions.decrementAndGet();
		clients_concurrent_actions.put(account, actions);
		
		return true;
	}
	
	private static boolean isOpCodeToBeTested(final int opcode, final int opcode2, final boolean loginclient)
	{
		if (loginclient)
		{
			return !NetcoreConfig.getInstance().LS_LIST_PROTECTED_OPCODES.contains(opcode);
		}
		
		if (opcode == 0xd0)
		{
			if (NetcoreConfig.getInstance().GS_LIST_PROTECTED_OPCODES.contains(opcode))
			{
				return !NetcoreConfig.getInstance().GS_LIST_PROTECTED_OPCODES2.contains(opcode2);
			}
			return true;
			
		}
		return !NetcoreConfig.getInstance().GS_LIST_PROTECTED_OPCODES.contains(opcode);
	}
	
	/**
	 * Kick player from game (close network connection).
	 * @param client
	 * @param opcode
	 */
	private static void kickPlayer(final MMOClient<?> client, final int opcode)
	{
		if (client instanceof L2LoginClient)
		{
			final L2LoginClient login_cl = (L2LoginClient) client;
			login_cl.close(LoginFailReason.REASON_SYSTEM_ERROR);
			
			LOGGER.warn("Player with account " + login_cl.getAccount() + " kicked for flooding with packet " + Integer.toHexString(opcode));
		}
		else if (client instanceof L2GameClient)
		{
			final L2GameClient game_cl = (L2GameClient) client;
			game_cl.closeNow();
			
			LOGGER.warn("Player with account " + game_cl.accountName + " kicked for flooding with packet " + Integer.toHexString(opcode));
		}
	}
	
	/**
	 * Bans char account and logs out the char.
	 * @param client
	 * @param opcode
	 */
	private static void banAccount(final MMOClient<?> client, final int opcode)
	{
		if (client instanceof L2LoginClient)
		{
			final L2LoginClient login_cl = (L2LoginClient) client;
			LoginController.getInstance().setAccountAccessLevel(login_cl.getAccount(), -100);
			login_cl.close(LoginFailReason.REASON_SYSTEM_ERROR);
			
			LOGGER.warn("Player with account " + login_cl.getAccount() + " banned for flooding forever with packet " + Integer.toHexString(opcode));
		}
		else if (client instanceof L2GameClient)
		{
			final L2GameClient game_cl = (L2GameClient) client;
			
			if (game_cl.getActiveChar() != null)
			{
				game_cl.getActiveChar().setPunishLevel(L2PcInstance.PunishLevel.ACC, 0);
				LOGGER.warn("Player " + game_cl.getActiveChar() + " of account " + game_cl.accountName + " banned forever for flooding with packet " + Integer.toHexString(opcode));
				game_cl.getActiveChar().logout();
			}
			
			game_cl.closeNow();
			LOGGER.warn("Player with account " + game_cl.accountName + " kicked for flooding with packet " + Integer.toHexString(opcode));
		}
	}
}