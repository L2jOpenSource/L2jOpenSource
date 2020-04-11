package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestChangeWaitType extends L2GameClientPacket
{
	private boolean _typeStand;
	
	@Override
	protected void readImpl()
	{
		_typeStand = (readD() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final WorldObject target = player.getTarget();
		
		if (_typeStand)
			player.getAI().tryTo(IntentionType.STAND, null, null);
		else
			player.getAI().tryTo(IntentionType.SIT, target, null);
	}
}