package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class RequestSkillList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();
		
		if (cha != null)
		{
			cha.sendSkillList();
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 3F RequestSkillList";
	}
}
