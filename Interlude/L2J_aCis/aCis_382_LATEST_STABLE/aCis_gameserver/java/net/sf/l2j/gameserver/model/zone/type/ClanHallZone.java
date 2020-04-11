package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ClanHallDecoration;

/**
 * A zone extending {@link SpawnZoneType} used by {@link ClanHall}s.
 */
public class ClanHallZone extends SpawnZoneType
{
	private int _clanHallId;
	
	public ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
			_clanHallId = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (character instanceof Player)
		{
			// Set as in clan hall
			character.setInsideZone(ZoneId.CLAN_HALL, true);
			
			final ClanHall ch = ClanHallManager.getInstance().getClanHall(_clanHallId);
			if (ch == null)
				return;
			
			// Send decoration packet
			character.sendPacket(new ClanHallDecoration(ch));
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
			character.setInsideZone(ZoneId.CLAN_HALL, false);
	}
	
	/**
	 * Kick {@link Player}s who don't belong to the clan set as parameter from this zone. They are ported to town.
	 * @param clanId : The clanhall owner id. Related players aren't teleported out.
	 */
	public void banishForeigners(int clanId)
	{
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.getClanId() == clanId)
				continue;
			
			player.teleportTo(TeleportType.TOWN);
		}
	}
	
	public int getClanHallId()
	{
		return _clanHallId;
	}
}