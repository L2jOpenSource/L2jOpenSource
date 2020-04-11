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
package l2r.gameserver.model.actor.events;

import java.util.logging.Level;

import l2r.gameserver.instancemanager.AntiFeedManager;
import l2r.gameserver.model.actor.events.annotations.Message;
import l2r.gameserver.model.actor.events.annotations.UseAntiFeed;
import l2r.gameserver.model.actor.events.listeners.IDlgAnswerEventListener;
import l2r.gameserver.model.actor.events.listeners.IFamePointsChangeEventListener;
import l2r.gameserver.model.actor.events.listeners.IKarmaChangeEventListener;
import l2r.gameserver.model.actor.events.listeners.IPKPointsChangeEventListener;
import l2r.gameserver.model.actor.events.listeners.IPlayerLoginEventListener;
import l2r.gameserver.model.actor.events.listeners.IPlayerLogoutEventListener;
import l2r.gameserver.model.actor.events.listeners.IPvPKillEventListener;
import l2r.gameserver.model.actor.events.listeners.IPvPPointsEventChange;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class PlayerEvents extends PlayableEvents
{
	public PlayerEvents(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	@Override
	public L2PcInstance getActingPlayer()
	{
		return (L2PcInstance) super.getActingPlayer();
	}
	
	/**
	 * Fired whenever player's karma points has change.
	 * @param oldKarma
	 * @param newKarma
	 * @return {@code true} if karma change is possible, {@code false} otherwise.
	 */
	public boolean onKarmaChange(int oldKarma, int newKarma)
	{
		if (hasListeners())
		{
			for (IKarmaChangeEventListener listener : getEventListeners(IKarmaChangeEventListener.class))
			{
				try
				{
					if (!listener.onKarmaChange(getActingPlayer(), oldKarma, newKarma))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * Fired whenever player's pk points has change.
	 * @param oldPKPoints
	 * @param newPKPoints
	 * @return {@code true} if pk points change is possible, {@code false} otherwise.
	 */
	public boolean onPKChange(int oldPKPoints, int newPKPoints)
	{
		if (hasListeners())
		{
			for (IPKPointsChangeEventListener listener : getEventListeners(IPKPointsChangeEventListener.class))
			{
				try
				{
					if (!listener.onPKPointsChange(getActingPlayer(), oldPKPoints, newPKPoints))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * Fired whenever player's pvp points has change.
	 * @param oldPvPPoints
	 * @param newPvPPoints
	 * @return {@code true} if pvp points change is possible, {@code false} otherwise.
	 */
	public boolean onPvPChange(int oldPvPPoints, int newPvPPoints)
	{
		if (hasListeners())
		{
			for (IPvPPointsEventChange listener : getEventListeners(IPvPPointsEventChange.class))
			{
				try
				{
					if (!listener.onPvPPointsChange(getActingPlayer(), oldPvPPoints, newPvPPoints))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * Fired whenever player's fame points has change.
	 * @param oldFamePoints
	 * @param newFamePoints
	 * @return {@code true} if fame points change is possible, {@code false} otherwise.
	 */
	public boolean onFameChange(int oldFamePoints, int newFamePoints)
	{
		if (hasListeners())
		{
			for (IFamePointsChangeEventListener listener : getEventListeners(IFamePointsChangeEventListener.class))
			{
				try
				{
					if (!listener.onFamePointsChange(getActingPlayer(), oldFamePoints, newFamePoints))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * Fired whenever player answer on a dialog yes/no.<br>
	 * Supported annotations:<br>
	 * <ul>
	 * <li>{@link Message}</li>
	 * </ul>
	 * @param messageId
	 * @param answer
	 * @param requesterId
	 * @return {@code true} if current answer is possible, {@code false} otherwise.
	 */
	public boolean onDlgAnswer(int messageId, int answer, int requesterId)
	{
		if (hasListeners())
		{
			for (IDlgAnswerEventListener listener : getEventListeners(IDlgAnswerEventListener.class))
			{
				try
				{
					final Message messageA = listener.getClass().getAnnotation(Message.class);
					if ((messageA != null) && !Util.contains(messageA.value(), messageId))
					{
						continue;
					}
					
					if (!listener.onDlgAnswer(getActingPlayer(), messageId, answer, requesterId))
					{
						return false;
					}
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * Fired whenever current player login.
	 */
	public void onPlayerLogin()
	{
		if (hasListeners())
		{
			for (IPlayerLoginEventListener listener : getEventListeners(IPlayerLoginEventListener.class))
			{
				try
				{
					listener.onPlayerLogin(getActingPlayer());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
	
	/**
	 * Fired whenever current player is logged out.
	 */
	public void onPlayerLogout()
	{
		if (hasListeners())
		{
			for (IPlayerLogoutEventListener listener : getEventListeners(IPlayerLogoutEventListener.class))
			{
				try
				{
					listener.onPlayerLogout(getActingPlayer());
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
	
	/**
	 * Fired whenever player kills another player.<br>
	 * Supported annotations:<br>
	 * <ul>
	 * <li>{@link UseAntiFeed}</li>
	 * </ul>
	 * @param target
	 */
	public void onPvPKill(L2PcInstance target)
	{
		if (hasListeners())
		{
			for (IPvPKillEventListener listener : getEventListeners(IPvPKillEventListener.class))
			{
				try
				{
					final UseAntiFeed useAntiFeed = listener.getClass().getAnnotation(UseAntiFeed.class);
					if ((useAntiFeed != null) && !AntiFeedManager.getInstance().check(getActingPlayer(), target))
					{
						continue;
					}
					
					listener.onPvPKill(getActingPlayer(), target);
				}
				catch (Exception e)
				{
					_log.log(Level.WARNING, getClass().getSimpleName() + ": Exception caught: ", e);
				}
			}
		}
	}
}
