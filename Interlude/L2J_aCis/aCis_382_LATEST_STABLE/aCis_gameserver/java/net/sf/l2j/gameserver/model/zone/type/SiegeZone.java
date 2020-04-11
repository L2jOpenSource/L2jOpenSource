package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.SiegeSummon;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;

/**
 * A zone extending {@link SpawnZoneType}, used for castle on siege progress, and which handles following spawns type :
 * <ul>
 * <li>Generic spawn locs : other_restart_village_list (spawns used on siege, to respawn on second closest town.</li>
 * <li>Chaotic spawn locs : chao_restart_point_list (spawns used on siege, to respawn PKs on second closest town.</li>
 * </ul>
 */
public class SiegeZone extends SpawnZoneType
{
	private int _siegableId = -1;
	private boolean _isActiveSiege = false;
	
	public SiegeZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId") || name.equals("clanHallId"))
			_siegableId = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (_isActiveSiege)
		{
			character.setInsideZone(ZoneId.PVP, true);
			character.setInsideZone(ZoneId.SIEGE, true);
			character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
			
			if (character instanceof Player)
			{
				Player activeChar = (Player) character;
				
				activeChar.setIsInSiege(true); // in siege
				
				activeChar.sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
				activeChar.enterOnNoLandingZone();
			}
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		character.setInsideZone(ZoneId.PVP, false);
		character.setInsideZone(ZoneId.SIEGE, false);
		character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
		
		if (character instanceof Player)
		{
			final Player activeChar = (Player) character;
			
			if (_isActiveSiege)
			{
				activeChar.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
				activeChar.exitOnNoLandingZone();
				
				PvpFlagTaskManager.getInstance().add(activeChar, Config.PVP_NORMAL_TIME);
				
				// Set pvp flag
				if (activeChar.getPvpFlag() == 0)
					activeChar.updatePvPFlag(1);
			}
			
			activeChar.setIsInSiege(false);
		}
		else if (character instanceof SiegeSummon)
			((SiegeSummon) character).unSummon(((SiegeSummon) character).getOwner());
	}
	
	public void updateZoneStatusForCharactersInside()
	{
		if (_isActiveSiege)
		{
			for (Creature character : _characters.values())
				onEnter(character);
		}
		else
		{
			for (Creature character : _characters.values())
			{
				character.setInsideZone(ZoneId.PVP, false);
				character.setInsideZone(ZoneId.SIEGE, false);
				character.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
				
				if (character instanceof Player)
				{
					final Player player = ((Player) character);
					
					player.sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
					player.exitOnNoLandingZone();
				}
				else if (character instanceof SiegeSummon)
					((SiegeSummon) character).unSummon(((SiegeSummon) character).getOwner());
			}
		}
	}
	
	public int getSiegeObjectId()
	{
		return _siegableId;
	}
	
	public boolean isActive()
	{
		return _isActiveSiege;
	}
	
	public void setIsActive(boolean val)
	{
		_isActiveSiege = val;
	}
	
	/**
	 * Sends a message to all players in this zone
	 * @param message
	 */
	public void announceToPlayers(String message)
	{
		for (Player player : getKnownTypeInside(Player.class))
			player.sendMessage(message);
	}
	
	/**
	 * Kick {@link Player}s who don't belong to the clan set as parameter from this zone. They are ported to chaotic or regular spawn locations depending of their karma.
	 * @param clanId : The castle owner id. Related players aren't teleported out.
	 */
	public void banishForeigners(int clanId)
	{
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.getClanId() == clanId)
				continue;
			
			player.teleportTo((player.getKarma() > 0) ? getRandomChaoticLoc() : getRandomLoc(), 20);
		}
	}
}