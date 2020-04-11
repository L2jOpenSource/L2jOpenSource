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
package l2r.gameserver.model.zone.type;

import l2r.Config;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.enums.MountType;
import l2r.gameserver.enums.TeleportWhereType;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.CHSiegeManager;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.FortSiegeManager;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.FortSiege;
import l2r.gameserver.model.entity.Siegable;
import l2r.gameserver.model.entity.clanhall.SiegableHall;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.AbstractZoneSettings;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.SystemMessageId;

/**
 * A siege zone
 * @author durgus
 */
public class L2SiegeZone extends L2ZoneType
{
	private static final int DISMOUNT_DELAY = 5;
	
	public L2SiegeZone(int id)
	{
		super(id);
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if (settings == null)
		{
			settings = new Settings();
		}
		setSettings(settings);
	}
	
	private final class Settings extends AbstractZoneSettings
	{
		private int _siegableId = -1;
		private Siegable _siege = null;
		private boolean _isActiveSiege = false;
		
		public Settings()
		{
		}
		
		public int getSiegeableId()
		{
			return _siegableId;
		}
		
		protected void setSiegeableId(int id)
		{
			_siegableId = id;
		}
		
		public Siegable getSiege()
		{
			return _siege;
		}
		
		public void setSiege(Siegable s)
		{
			_siege = s;
		}
		
		public boolean isActiveSiege()
		{
			return _isActiveSiege;
		}
		
		public void setActiveSiege(boolean val)
		{
			_isActiveSiege = val;
		}
		
		@Override
		public void clear()
		{
			_siegableId = -1;
			_siege = null;
			_isActiveSiege = false;
		}
	}
	
	@Override
	public Settings getSettings()
	{
		return (Settings) super.getSettings();
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
		{
			if (getSettings().getSiegeableId() != -1)
			{
				throw new IllegalArgumentException("Siege object already defined!");
			}
			getSettings().setSiegeableId(Integer.parseInt(value));
		}
		else if (name.equals("fortId"))
		{
			if (getSettings().getSiegeableId() != -1)
			{
				throw new IllegalArgumentException("Siege object already defined!");
			}
			getSettings().setSiegeableId(Integer.parseInt(value));
		}
		else if (name.equals("clanHallId"))
		{
			if (getSettings().getSiegeableId() != -1)
			{
				throw new IllegalArgumentException("Siege object already defined!");
			}
			getSettings().setSiegeableId(Integer.parseInt(value));
			SiegableHall hall = CHSiegeManager.getInstance().getConquerableHalls().get(getSettings().getSiegeableId());
			if (hall == null)
			{
				_log.warn("L2SiegeZone: Siegable clan hall with id " + value + " does not exist!");
			}
			else
			{
				hall.setSiegeZone(this);
			}
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (getSettings().isActiveSiege())
		{
			character.setInsideZone(ZoneIdType.PVP, true);
			character.setInsideZone(ZoneIdType.SIEGE, true);
			character.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, true); // FIXME: Custom ?
			
			if (character.isPlayer())
			{
				L2PcInstance plyer = character.getActingPlayer();
				if (plyer.isRegisteredOnThisSiegeField(getSettings().getSiegeableId()))
				{
					plyer.setIsInSiege(true); // in siege
					if (getSettings().getSiege().giveFame() && (getSettings().getSiege().getFameFrequency() > 0))
					{
						plyer.startFameTask(getSettings().getSiege().getFameFrequency() * 1000, getSettings().getSiege().getFameAmount());
					}
				}
				
				character.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				if (!Config.ALLOW_WYVERN_DURING_SIEGE && (plyer.getMountType() == MountType.WYVERN))
				{
					plyer.sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN);
					plyer.enteredNoLanding(DISMOUNT_DELAY);
				}
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneIdType.PVP, false);
		character.setInsideZone(ZoneIdType.SIEGE, false);
		character.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, false); // FIXME: Custom ?
		if (getSettings().isActiveSiege())
		{
			if (character.isPlayer())
			{
				L2PcInstance player = character.getActingPlayer();
				character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				if (player.getMountType() == MountType.WYVERN)
				{
					player.exitedNoLanding();
				}
				// Set pvp flag
				if (player.getPvpFlag() == 0)
				{
					player.startPvPFlag();
				}
			}
		}
		if (character.isPlayer())
		{
			L2PcInstance activeChar = character.getActingPlayer();
			activeChar.stopFameTask();
			activeChar.setIsInSiege(false);
			
			if ((getSettings().getSiege() instanceof FortSiege) && (activeChar.getInventory().getItemByItemId(9819) != null))
			{
				// drop combat flag
				Fort fort = FortManager.getInstance().getFortById(getSettings().getSiegeableId());
				if (fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(activeChar, fort.getResidenceId());
				}
				else
				{
					int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
					activeChar.getInventory().unEquipItemInBodySlot(slot);
					activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
				}
			}
		}
		
		if (character instanceof L2SiegeSummonInstance)
		{
			((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
		}
	}
	
	@Override
	public void onDieInside(L2Character character)
	{
		if (getSettings().isActiveSiege())
		{
			// debuff participants only if they die inside siege zone
			if (character.isPlayer() && character.getActingPlayer().isRegisteredOnThisSiegeField(getSettings().getSiegeableId()))
			{
				int lvl = 1;
				final L2Effect e = character.getFirstEffect(5660);
				if (e != null)
				{
					lvl = Math.min(lvl + e.getLevel(), 5);
				}
				
				final L2Skill skill = SkillData.getInstance().getInfo(5660, lvl);
				if (skill != null)
				{
					skill.getEffects(character, character);
				}
			}
		}
	}
	
	@Override
	public void onReviveInside(L2Character character)
	{
	}
	
	public void updateZoneStatusForCharactersInside()
	{
		if (getSettings().isActiveSiege())
		{
			for (L2Character character : getCharactersInside())
			{
				if (character != null)
				{
					onEnter(character);
				}
			}
		}
		else
		{
			L2PcInstance player;
			for (L2Character character : getCharactersInside())
			{
				if (character == null)
				{
					continue;
				}
				
				character.setInsideZone(ZoneIdType.PVP, false);
				character.setInsideZone(ZoneIdType.SIEGE, false);
				character.setInsideZone(ZoneIdType.NO_SUMMON_FRIEND, false);
				
				if (character.isPlayer())
				{
					player = character.getActingPlayer();
					character.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					player.stopFameTask();
					if (player.getMountType() == MountType.WYVERN)
					{
						player.exitedNoLanding();
					}
				}
				if (character instanceof L2SiegeSummonInstance)
				{
					((L2SiegeSummonInstance) character).unSummon(((L2SiegeSummonInstance) character).getOwner());
				}
				
			}
		}
	}
	
	/**
	 * Sends a message to all players in this zone
	 * @param message
	 */
	public void announceToPlayers(String message)
	{
		for (L2PcInstance player : getPlayersInside())
		{
			if (player != null)
			{
				player.sendMessage(message);
			}
		}
	}
	
	public int getSiegeObjectId()
	{
		return getSettings().getSiegeableId();
	}
	
	public boolean isActive()
	{
		return getSettings().isActiveSiege();
	}
	
	public void setIsActive(boolean val)
	{
		getSettings().setActiveSiege(val);
	}
	
	public void setSiegeInstance(Siegable siege)
	{
		getSettings().setSiege(siege);
	}
	
	/**
	 * Removes all foreigners from the zone
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		TeleportWhereType type = TeleportWhereType.Town;
		for (L2PcInstance temp : getPlayersInside())
		{
			if (temp.getClanId() == owningClanId)
			{
				continue;
			}
			
			temp.teleToLocation(type);
		}
	}
}
