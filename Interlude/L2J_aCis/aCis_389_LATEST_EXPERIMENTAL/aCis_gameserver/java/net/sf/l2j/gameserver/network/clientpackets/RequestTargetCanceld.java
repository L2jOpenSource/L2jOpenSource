package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestTargetCanceld extends L2GameClientPacket
{
	private int _unselect;
	
	@Override
	protected void readImpl()
	{
		_unselect = readH();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (_unselect == 0)
		{
			if (player.getCast().isCastingNow() && player.getCast().canAbortCast())
				player.getCast().stop();
			else
				player.setTarget(null);
		}
		else
			player.setTarget(null);
	}
}