package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SiegeInfo;

public final class RequestJoinSiege extends L2GameClientPacket
{
	private int _castleId;
	private int _isAttacker;
	private int _isJoining;
	
	@Override
	protected void readImpl()
	{
		_castleId = readD();
		_isAttacker = readD();
		_isJoining = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (!player.isClanLeader())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		final Castle castle = CastleManager.getInstance().getCastleById(_castleId);
		if (castle == null)
			return;
		
		if (_isJoining == 1)
		{
			if (System.currentTimeMillis() < player.getClan().getDissolvingExpiryTime())
			{
				player.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
				return;
			}
			
			if (_isAttacker == 1)
				castle.getSiege().registerAttacker(player);
			else
				castle.getSiege().registerDefender(player);
		}
		else
			castle.getSiege().unregisterClan(player.getClan());
		
		player.sendPacket(new SiegeInfo(castle));
	}
}