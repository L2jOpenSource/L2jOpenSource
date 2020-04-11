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

import java.nio.BufferUnderflowException;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.L2GameClient;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.mmocore.network.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packets received by the game server from clients
 * @author KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	protected static final Logger _log = LoggerFactory.getLogger(L2GameClientPacket.class);
	protected static final java.util.logging.Logger _logger = java.util.logging.Logger.getLogger(L2GameClientPacket.class.getName());
	
	@Override
	public boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (Exception e)
		{
			if (e instanceof BufferUnderflowException)
			{
				_log.info("Client: " + getClient().toString() + " - Failed reading: " + getType() + " Buffer Under Flow Exception.");
				getClient().onBufferUnderflow();
				return false;
			}
			
			_log.error("Client: " + getClient().toString() + " - Failed reading: " + getType() + e.getMessage(), e);
		}
		return false;
	}
	
	protected abstract void readImpl();
	
	@Override
	public void run()
	{
		try
		{
			runImpl();
			
			/*
			 * Removes onspawn protection - player has faster computer than average Since GE: True for all packets except RequestItemList and UseItem (in case the item is a Scroll of Escape (736)
			 */
			if (triggersOnActionRequest())
			{
				final L2PcInstance actor = getClient().getActiveChar();
				if ((actor != null) && (actor.isSpawnProtected() || actor.isInvul() || actor.getEventInfo().isInEvent()))
				{
					actor.onActionRequest();
					if (Config.DEBUG)
					{
						_log.info("Spawn protection for player " + actor.getName() + " removed by packet: " + getType());
					}
				}
			}
		}
		catch (Throwable t)
		{
			_log.error("Client: " + getClient().toString() + " - Failed running: " + getType() + t.getMessage(), t);
			// in case of EnterWorld error kick player from game
			if (this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}
	
	protected abstract void runImpl();
	
	/**
	 * Sends a game server packet to the client.
	 * @param gsp the game server packet
	 */
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	/**
	 * Sends a system message to the client.
	 * @param id the system message Id
	 */
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	/**
	 * @return A String with this packet name for debugging purposes
	 */
	public abstract String getType();
	
	/**
	 * Overridden with true value on some packets that should disable spawn protection (RequestItemList and UseItem only)
	 * @return
	 */
	protected boolean triggersOnActionRequest()
	{
		return true;
	}
	
	/**
	 * @return the active player if exist, otherwise null.
	 */
	protected final L2PcInstance getActiveChar()
	{
		return getClient().getActiveChar();
	}
	
	protected final void sendActionFailed()
	{
		if (getClient() != null)
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
