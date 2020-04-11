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
package l2r.gameserver.util;

import java.util.concurrent.atomic.AtomicInteger;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.enums.PcCondOverride;
import l2r.gameserver.instancemanager.PunishmentManager;
import l2r.gameserver.model.punishment.PunishmentAffect;
import l2r.gameserver.model.punishment.PunishmentTask;
import l2r.gameserver.model.punishment.PunishmentType;
import l2r.gameserver.network.L2GameClient;
import l2r.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flood protector implementation.
 * @author fordfrog
 */
public final class FloodProtectorAction
{
	/**
	 * Logger
	 */
	private static final Logger _log = LoggerFactory.getLogger(FloodProtectorAction.class);
	/**
	 * Client for this instance of flood protector.
	 */
	private final L2GameClient _client;
	/**
	 * Configuration of this instance of flood protector.
	 */
	private final FloodProtectorConfig _config;
	/**
	 * Next game tick when new request is allowed.
	 */
	private volatile int _nextGameTick = GameTimeController.getInstance().getGameTicks();
	/**
	 * Request counter.
	 */
	private final AtomicInteger _count = new AtomicInteger(0);
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	private boolean _logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply punisment multiple times (flooding).
	 */
	private volatile boolean _punishmentInProgress;
	
	/**
	 * Creates new instance of FloodProtectorAction.
	 * @param client the game client for which flood protection is being created
	 * @param config flood protector configuration
	 */
	public FloodProtectorAction(final L2GameClient client, final FloodProtectorConfig config)
	{
		super();
		_client = client;
		_config = config;
	}
	
	/**
	 * Checks whether the request is flood protected or not.
	 * @param command command issued or short command description
	 * @return true if action is allowed, otherwise false
	 */
	public boolean tryPerformAction(final String command)
	{
		final int curTick = GameTimeController.getInstance().getGameTicks();
		
		if ((_client.getActiveChar() != null) && _client.getActiveChar().canOverrideCond(PcCondOverride.FLOOD_CONDITIONS))
		{
			return true;
		}
		
		if ((curTick < _nextGameTick) || _punishmentInProgress)
		{
			if (_config.LOG_FLOODING && !_logged)
			{
				log(" called command ", command, " ~", String.valueOf((_config.FLOOD_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK), " ms after previous command");
				_logged = true;
			}
			
			_count.incrementAndGet();
			
			if (!_punishmentInProgress && (_config.PUNISHMENT_LIMIT > 0) && (_count.get() >= _config.PUNISHMENT_LIMIT) && (_config.PUNISHMENT_TYPE != null))
			{
				_punishmentInProgress = true;
				
				if ("kick".equals(_config.PUNISHMENT_TYPE))
				{
					kickPlayer();
				}
				else if ("ban".equals(_config.PUNISHMENT_TYPE))
				{
					banAccount();
				}
				else if ("jail".equals(_config.PUNISHMENT_TYPE))
				{
					jailChar();
				}
				
				_punishmentInProgress = false;
			}
			return false;
		}
		
		if (_count.get() > 0)
		{
			if (_config.LOG_FLOODING)
			{
				log(" issued ", String.valueOf(_count), " extra requests within ~", String.valueOf(_config.FLOOD_PROTECTION_INTERVAL * GameTimeController.MILLIS_IN_TICK), " ms");
			}
		}
		
		_nextGameTick = curTick + _config.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		_count.set(0);
		return true;
	}
	
	/**
	 * Kick player from game (close network connection).
	 */
	private void kickPlayer()
	{
		if (_client.getActiveChar() != null)
		{
			_client.getActiveChar().logout(false);
		}
		else
		{
			_client.closeNow();
		}
		
		log("kicked for flooding");
	}
	
	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount()
	{
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(_client.getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.BAN, System.currentTimeMillis() + _config.PUNISHMENT_TIME, "", getClass().getSimpleName()));
		log(" banned for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
	}
	
	/**
	 * Jails char.
	 */
	private void jailChar()
	{
		if (_client.getActiveChar() != null)
		{
			int charId = _client.getActiveChar().getObjectId();
			if (charId > 0)
			{
				PunishmentManager.getInstance().startPunishment(new PunishmentTask(charId, PunishmentAffect.CHARACTER, PunishmentType.JAIL, System.currentTimeMillis() + _config.PUNISHMENT_TIME, "", getClass().getSimpleName()));
			}
			
			log(" jailed for flooding ", _config.PUNISHMENT_TIME <= 0 ? "forever" : "for " + _config.PUNISHMENT_TIME + " mins");
		}
	}
	
	private void log(String... lines)
	{
		final StringBuilder output = StringUtil.startAppend(100, _config.FLOOD_PROTECTOR_TYPE, ": ");
		String address = null;
		try
		{
			if (!_client.isDetached())
			{
				address = _client.getConnection().getInetAddress().getHostAddress();
			}
		}
		catch (Exception e)
		{
		}
		
		switch (_client.getState())
		{
			case IN_GAME:
				if (_client.getActiveChar() != null)
				{
					StringUtil.append(output, _client.getActiveChar().getName());
					StringUtil.append(output, "(", String.valueOf(_client.getActiveChar().getObjectId()), ") ");
				}
				break;
			case AUTHED:
				if (_client.getAccountName() != null)
				{
					StringUtil.append(output, _client.getAccountName(), " ");
				}
				break;
			case CONNECTED:
				if (address != null)
				{
					StringUtil.append(output, address);
				}
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}
		
		StringUtil.append(output, lines);
		_log.warn(output.toString());
	}
}