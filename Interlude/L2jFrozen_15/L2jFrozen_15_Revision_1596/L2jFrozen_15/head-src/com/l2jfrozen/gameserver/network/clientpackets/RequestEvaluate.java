package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.network.serverpackets.UserInfo;

public final class RequestEvaluate extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int targetId;
	
	@Override
	protected void readImpl()
	{
		targetId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		SystemMessage sm;
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!(activeChar.getTarget() instanceof L2PcInstance))
		{
			sm = new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getLevel() < 10)
		{
			sm = new SystemMessage(SystemMessageId.ONLY_LEVEL_SUP_10_CAN_RECOMMEND);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getTarget() == activeChar)
		{
			sm = new SystemMessage(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (activeChar.getRecomLeft() <= 0)
		{
			sm = new SystemMessage(SystemMessageId.NO_MORE_RECOMMENDATIONS_TO_HAVE);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		final L2PcInstance target = (L2PcInstance) activeChar.getTarget();
		
		if (target.getRecomHave() >= Config.ALT_RECOMMENDATIONS_NUMBER)
		{
			sm = new SystemMessage(SystemMessageId.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		if (!activeChar.canRecom(target))
		{
			sm = new SystemMessage(SystemMessageId.THAT_CHARACTER_IS_RECOMMENDED);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		activeChar.giveRecom(target);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED);
		sm.addString(target.getName());
		sm.addNumber(activeChar.getRecomLeft());
		activeChar.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED);
		sm.addString(activeChar.getName());
		target.sendPacket(sm);
		sm = null;
		
		activeChar.sendPacket(new UserInfo(activeChar));
		target.broadcastUserInfo();
	}
	
	@Override
	public String getType()
	{
		return "[C] B9 RequestEvaluate";
	}
}
