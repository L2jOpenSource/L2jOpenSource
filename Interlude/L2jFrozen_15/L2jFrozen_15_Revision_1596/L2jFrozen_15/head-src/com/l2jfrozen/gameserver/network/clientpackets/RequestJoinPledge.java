package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.AskJoinPledge;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

public final class RequestJoinPledge extends L2GameClientPacket
{
	private int playerTarget;
	private int pledgeType;
	
	@Override
	protected void readImpl()
	{
		playerTarget = readD();
		pledgeType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (!(L2World.getInstance().findObject(playerTarget) instanceof L2PcInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
			return;
		}
		
		final L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(playerTarget);
		final L2Clan clan = activeChar.getClan();
		
		if (!clan.checkClanJoinCondition(activeChar, target, pledgeType))
		{
			return;
		}
		
		if (!activeChar.getRequest().setRequest(target, this))
		{
			return;
		}
		
		final AskJoinPledge ap = new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName());
		target.sendPacket(ap);
	}
	
	public int getPledgeType()
	{
		return pledgeType;
	}
	
	@Override
	public String getType()
	{
		return "[C] 24 RequestJoinPledge";
	}
}