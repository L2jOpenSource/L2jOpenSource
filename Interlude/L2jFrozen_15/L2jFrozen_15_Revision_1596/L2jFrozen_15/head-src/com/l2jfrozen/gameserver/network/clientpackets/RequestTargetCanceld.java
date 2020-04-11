package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Character;

public final class RequestTargetCanceld extends L2GameClientPacket
{
	private int unselect;
	
	@Override
	protected void readImpl()
	{
		unselect = readH();
	}
	
	@Override
	protected void runImpl()
	{
		final L2Character activeChar = getClient().getActiveChar();
		if (activeChar != null)
		{
			if (unselect == 0)
			{
				if (activeChar.isCastingNow() && activeChar.canAbortCast())
				{
					activeChar.abortCast();
				}
				else if (activeChar.getTarget() != null)
				{
					activeChar.setTarget(null);
				}
			}
			else if (activeChar.getTarget() != null)
			{
				activeChar.setTarget(null);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 37 RequestTargetCanceld";
	}
}