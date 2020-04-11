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
package com.l2jserver.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.TeleportWhereType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2OlympiadManagerInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.olympiad.OlympiadGameTask;
import com.l2jserver.gameserver.model.zone.AbstractZoneSettings;
import com.l2jserver.gameserver.model.zone.L2ZoneRespawn;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExOlympiadMatchEnd;
import com.l2jserver.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * An olympiad stadium
 * @author durgus, DS
 */
public class L2OlympiadStadiumZone extends L2ZoneRespawn
{
	private List<Location> _spectatorLocations;
	
	public L2OlympiadStadiumZone(int id)
	{
		super(id);
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new Settings();
		}
		setSettings(settings);
	}
	
	public final class Settings extends AbstractZoneSettings
	{
		private OlympiadGameTask _task = null;
		
		protected Settings()
		{
		}
		
		public OlympiadGameTask getOlympiadTask()
		{
			return _task;
		}
		
		protected void setTask(OlympiadGameTask task)
		{
			_task = task;
		}
		
		@Override
		public void clear()
		{
			_task = null;
		}
	}
	
	@Override
	public Settings getSettings()
	{
		return (Settings) super.getSettings();
	}
	
	public final void registerTask(OlympiadGameTask task)
	{
		getSettings().setTask(task);
	}
	
	public final void openDoors()
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(getInstanceId()).getDoors())
		{
			if ((door != null) && !door.getOpen())
			{
				door.openMe();
			}
		}
	}
	
	public final void closeDoors()
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(getInstanceId()).getDoors())
		{
			if ((door != null) && door.getOpen())
			{
				door.closeMe();
			}
		}
	}
	
	public final void spawnBuffers()
	{
		for (L2Npc buffer : InstanceManager.getInstance().getInstance(getInstanceId()).getNpcs())
		{
			if ((buffer instanceof L2OlympiadManagerInstance) && !buffer.isVisible())
			{
				buffer.spawnMe();
			}
		}
	}
	
	public final void deleteBuffers()
	{
		for (L2Npc buffer : InstanceManager.getInstance().getInstance(getInstanceId()).getNpcs())
		{
			if ((buffer instanceof L2OlympiadManagerInstance) && buffer.isVisible())
			{
				buffer.decayMe();
			}
		}
	}
	
	public final void broadcastStatusUpdate(L2PcInstance player)
	{
		final ExOlympiadUserInfo packet = new ExOlympiadUserInfo(player);
		for (L2PcInstance target : getPlayersInside())
		{
			if ((target != null) && (target.inObserverMode() || (target.getOlympiadSide() != player.getOlympiadSide())))
			{
				target.sendPacket(packet);
			}
		}
	}
	
	public final void broadcastPacketToObservers(L2GameServerPacket packet)
	{
		for (L2Character character : getCharactersInside())
		{
			if ((character != null) && character.isPlayer() && character.getActingPlayer().inObserverMode())
			{
				character.sendPacket(packet);
			}
		}
	}
	
	@Override
	protected final void onEnter(L2Character character)
	{
		if (getSettings().getOlympiadTask() != null)
		{
			if (getSettings().getOlympiadTask().isBattleStarted())
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character.isPlayer())
				{
					character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
					getSettings().getOlympiadTask().getGame().sendOlympiadInfo(character);
				}
			}
		}
		
		if (character.isPlayable())
		{
			final L2PcInstance player = character.getActingPlayer();
			if (player != null)
			{
				// only participants, observers and GMs allowed
				if (!player.canOverrideCond(PcCondOverride.ZONE_CONDITIONS) && !player.isInOlympiadMode() && !player.inObserverMode())
				{
					ThreadPoolManager.getInstance().executeGeneral(new KickPlayer(player));
				}
				else
				{
					// check for pet
					if (player.hasPet())
					{
						player.getSummon().unSummon(player);
					}
				}
			}
		}
	}
	
	@Override
	protected final void onExit(L2Character character)
	{
		if (getSettings().getOlympiadTask() != null)
		{
			if (getSettings().getOlympiadTask().isBattleStarted())
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character.isPlayer())
				{
					character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}
	
	public final void updateZoneStatusForCharactersInside()
	{
		if (getSettings().getOlympiadTask() == null)
		{
			return;
		}
		
		final boolean battleStarted = getSettings().getOlympiadTask().isBattleStarted();
		final SystemMessage sm;
		if (battleStarted)
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.LEFT_COMBAT_ZONE);
		}
		
		for (L2Character character : getCharactersInside())
		{
			if (character == null)
			{
				continue;
			}
			
			if (battleStarted)
			{
				character.setInsideZone(ZoneId.PVP, true);
				if (character.isPlayer())
				{
					character.sendPacket(sm);
				}
			}
			else
			{
				character.setInsideZone(ZoneId.PVP, false);
				if (character.isPlayer())
				{
					character.sendPacket(sm);
					character.sendPacket(ExOlympiadMatchEnd.STATIC_PACKET);
				}
			}
		}
	}
	
	private static final class KickPlayer implements Runnable
	{
		private L2PcInstance _player;
		
		public KickPlayer(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player != null)
			{
				if (_player.hasSummon())
				{
					_player.getSummon().unSummon(_player);
				}
				
				_player.teleToLocation(TeleportWhereType.TOWN);
				_player.setInstanceId(0);
				_player = null;
			}
		}
	}
	
	@Override
	public void parseLoc(int x, int y, int z, String type)
	{
		if ((type != null) && type.equals("spectatorSpawn"))
		{
			if (_spectatorLocations == null)
			{
				_spectatorLocations = new ArrayList<>();
			}
			_spectatorLocations.add(new Location(x, y, z));
		}
		else
		{
			super.parseLoc(x, y, z, type);
		}
	}
	
	public List<Location> getSpectatorSpawns()
	{
		return _spectatorLocations;
	}
}