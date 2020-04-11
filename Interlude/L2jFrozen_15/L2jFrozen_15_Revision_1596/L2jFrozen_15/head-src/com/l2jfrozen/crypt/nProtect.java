package com.l2jfrozen.crypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.L2GameClient;
import com.l2jfrozen.gameserver.network.serverpackets.GameGuardQuery;

/**
 * The main "engine" of protection ...
 * @author Nick
 */
public class nProtect
{
	private static Logger LOGGER = Logger.getLogger(nProtect.class);
	
	public static enum RestrictionType
	{
		RESTRICT_ENTER,
		RESTRICT_EVENT,
		RESTRICT_OLYMPIAD,
		RESTRICT_SIEGE
	}
	
	public class nProtectAccessor
	{
		public nProtectAccessor()
		{
		}
		
		public void setCheckGameGuardQuery(final Method m)
		{
			checkGameGuardQuery = m;
		}
		
		public void setStartTask(final Method m)
		{
			startTask = m;
		}
		
		public void setCheckRestriction(final Method m)
		{
			checkRestriction = m;
		}
		
		public void setSendRequest(final Method m)
		{
			sendRequest = m;
		}
		
		public void setCloseSession(final Method m)
		{
			closeSession = m;
		}
		
		public void setSendGGQuery(final Method m)
		{
			sendGGQuery = m;
		}
		
	}
	
	protected Method checkGameGuardQuery = null;
	protected Method startTask = null;
	protected Method checkRestriction = null;
	protected Method sendRequest = null;
	protected Method closeSession = null;
	protected Method sendGGQuery = null;
	private static nProtect instance = null;
	
	private static boolean enabled = false;
	
	public static nProtect getInstance()
	{
		if (instance == null)
		{
			instance = new nProtect();
		}
		return instance;
	}
	
	private nProtect()
	{
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName("com.l2jfrozen.protection.main");
			
			if (clazz != null)
			{
				final Method m = clazz.getMethod("init", nProtectAccessor.class);
				if (m != null)
				{
					m.invoke(null, new nProtectAccessor());
					enabled = true;
				}
			}
		}
		catch (final ClassNotFoundException e)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("nProtect System will be not loaded due to ClassNotFoundException of 'com.l2jfrozen.protection.main' class");
			}
		}
		catch (SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void sendGameGuardQuery(final GameGuardQuery pkt)
	{
		try
		{
			if (sendGGQuery != null)
			{
				sendGGQuery.invoke(pkt);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkGameGuardRepy(final L2GameClient cl, final int[] reply)
	{
		try
		{
			if (checkGameGuardQuery != null)
			{
				return (Boolean) checkGameGuardQuery.invoke(null, cl, reply);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	public ScheduledFuture<?> startTask(final L2GameClient client)
	{
		try
		{
			if (startTask != null)
			{
				return (ScheduledFuture<?>) startTask.invoke(null, client);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void sendRequest(final L2GameClient cl)
	{
		if (sendRequest != null)
		{
			try
			{
				sendRequest.invoke(null, cl);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void closeSession(final L2GameClient cl)
	{
		if (closeSession != null)
		{
			try
			{
				closeSession.invoke(null, cl);
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean checkRestriction(final L2PcInstance player, final RestrictionType type, final Object... params)
	{
		try
		{
			if (checkRestriction != null)
			{
				return (Boolean) checkRestriction.invoke(null, player, type, params);
			}
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * @return the enabled
	 */
	public static boolean isEnabled()
	{
		return enabled;
	}
	
}
