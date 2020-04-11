package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.Player;

public class RequestPledgeMemberList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Clan clan = activeChar.getClan();
		if(clan != null)
		{
			activeChar.sendPacket(clan.listAll());
		}
	}
}