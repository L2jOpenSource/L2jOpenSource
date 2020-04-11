package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.SSQStatus;

public final class RequestSSQStatus extends L2GameClientPacket
{
	private int _page;
	
	@Override
	protected void readImpl()
	{
		_page = readC();
	}
	
	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		if ((SevenSignsManager.getInstance().isSealValidationPeriod() || SevenSignsManager.getInstance().isCompResultsPeriod()) && _page == 4)
			return;
		
		activeChar.sendPacket(new SSQStatus(activeChar.getObjectId(), _page));
	}
}