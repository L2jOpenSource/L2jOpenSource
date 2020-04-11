package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.Henna;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class RequestHennaUnequip extends L2GameClientPacket
{
	private int _symbolId;
	
	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final Henna henna = player.getHennaList().getBySymbolId(_symbolId);
		if (henna == null)
			return;
		
		if (player.getAdena() < henna.getRemovePrice())
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}
		
		boolean success = player.getHennaList().remove(henna);
		if (!success)
			return;
		
		sendPacket(new HennaInfo(player));
		sendPacket(new UserInfo(player));
		
		player.reduceAdena("Henna", henna.getRemovePrice(), player, false);
		
		player.addItem("Henna", henna.getDyeId(), Henna.REMOVE_AMOUNT, player, true);
		player.sendPacket(SystemMessageId.SYMBOL_DELETED);
	}
}