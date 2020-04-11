package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{
		// this is just a trigger packet. it has no content
	}
	
	@Override
	public void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		
		if (player != null)
		{
			player.sendPacket(new SkillCoolTime(player));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 0xa6 RequestSkillCoolTime";
	}
}