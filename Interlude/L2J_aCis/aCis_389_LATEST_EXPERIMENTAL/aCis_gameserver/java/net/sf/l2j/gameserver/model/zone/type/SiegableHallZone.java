package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.SiegableHall;

public final class SiegableHallZone extends ClanHallZone
{
	public SiegableHallZone(int id)
	{
		super(id);
	}
	
	public void banishNonSiegeParticipants()
	{
		final SiegableHall ch = ClanHallManager.getInstance().getSiegableHall(getResidenceId());
		if (ch == null)
			return;
		
		for (Player player : getKnownTypeInside(Player.class))
		{
			if (player.isInSieagableHallSiege())
				player.teleportTo(ch.getRndSpawn(SpawnType.BANISH), 0);
		}
	}
}