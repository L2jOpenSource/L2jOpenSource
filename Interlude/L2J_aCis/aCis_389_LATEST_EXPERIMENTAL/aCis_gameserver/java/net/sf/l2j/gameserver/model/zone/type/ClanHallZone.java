package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.zone.type.subtype.ResidenceZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ClanHallDecoration;

/**
 * A zone extending {@link ResidenceZoneType} used by {@link ClanHall}s.
 */
public class ClanHallZone extends ResidenceZoneType
{
	public ClanHallZone(int id)
	{
		super(id);
	}
	
	@Override
	public void banishForeigners(int clanId)
	{
		final ClanHall ch = ClanHallManager.getInstance().getClanHall(getResidenceId());
		if (ch == null)
			return;
		
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.getClanId() == clanId)
				continue;
			
			player.teleportTo(ch.getRndSpawn(SpawnType.BANISH), 20);
		}
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
			setResidenceId(Integer.parseInt(value));
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
			
			final ClanHall ch = ClanHallManager.getInstance().getClanHall(getResidenceId());
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
}