package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AskJoinPledge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends L2GameClientPacket
{
	private int _target;
	private int _pledgeType;
	
	@Override
	protected void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getPlayer();
		if (activeChar == null)
			return;
		
		final Clan clan = activeChar.getClan();
		if (clan == null)
			return;
		
		final Player target = World.getInstance().getPlayer(_target);
		if (target == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}
		
		if (!clan.checkClanJoinCondition(activeChar, target, _pledgeType))
			return;
		
		if (!activeChar.getRequest().setRequest(target, this))
			return;
		
		target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2).addCharName(activeChar).addString(clan.getName()));
		target.sendPacket(new AskJoinPledge(activeChar.getObjectId(), clan.getName()));
	}
	
	public int getPledgeType()
	{
		return _pledgeType;
	}
}