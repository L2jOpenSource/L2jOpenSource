package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.network.serverpackets.AllyInfo;

public final class RequestAllyInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final AllyInfo ai = new AllyInfo(getClient().getActiveChar());
		sendPacket(ai);
	}
	
	@Override
	public String getType()
	{
		return "[C] 8E RequestAllyInfo";
	}
}
