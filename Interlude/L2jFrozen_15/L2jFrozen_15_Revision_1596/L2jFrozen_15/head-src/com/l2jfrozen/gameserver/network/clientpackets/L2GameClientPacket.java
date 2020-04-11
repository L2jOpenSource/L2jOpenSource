package com.l2jfrozen.gameserver.network.clientpackets;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.netcore.ReceivablePacket;

/**
 * Packets received by the game server from clients
 * @author KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	private static final Logger LOGGER = Logger.getLogger(L2GameClientPacket.class);
	
	@Override
	protected boolean read()
	{
		try
		{
			readImpl();
			return true;
		}
		catch (final BufferOverflowException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			if (getClient() != null)
			{
				getClient().closeNow();
			}
			
			LOGGER.warn("Client: " + getClient().toString() + " - Buffer overflow and has been kicked");
		}
		catch (final BufferUnderflowException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			getClient().onBufferUnderflow();
		}
		catch (final Throwable t)
		{
			LOGGER.error("Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + t.getMessage(), t);
			t.printStackTrace();
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
			if (this instanceof MoveBackwardToLocation || this instanceof AttackRequest || this instanceof RequestMagicSkillUse)
			{
				if (getClient().getActiveChar() != null)
				{
					getClient().getActiveChar().onActionRequest(); // Removes onSpawn Protection
				}
			}
		}
		catch (final Throwable t)
		{
			LOGGER.error("Client: " + getClient().toString() + " - Failed reading: " + getType() + " ; " + t.getMessage(), t);
			t.printStackTrace();
			
			if (this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}
	
	protected abstract void runImpl();
	
	protected final void sendPacket(final L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}