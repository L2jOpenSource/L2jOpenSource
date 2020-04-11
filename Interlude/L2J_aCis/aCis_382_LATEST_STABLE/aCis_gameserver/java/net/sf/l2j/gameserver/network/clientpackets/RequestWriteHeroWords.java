package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.model.actor.Player;

/**
 * Format chS c (id) 0xD0 h (subid) 0x0C S the hero's words :)
 * @author -Wooden-
 */
public final class RequestWriteHeroWords extends L2GameClientPacket
{
	private String _heroWords;
	
	@Override
	protected void readImpl()
	{
		_heroWords = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null || !player.isHero())
			return;
		
		if (_heroWords == null || _heroWords.length() > 300)
			return;
		
		HeroManager.getInstance().setHeroMessage(player, _heroWords);
	}
}