package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.List;
import java.util.stream.Collectors;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.holder.TimeStamp;

public class SkillCoolTime extends L2GameServerPacket
{
	public List<TimeStamp> reuseTimeStamps;
	
	public SkillCoolTime(L2PcInstance player)
	{
		reuseTimeStamps = player.getReuseTimeStamps().stream().filter(r -> r.hasNotPassed()).collect(Collectors.toList());
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xc1);
		writeD(reuseTimeStamps.size()); // list size
		for (TimeStamp ts : reuseTimeStamps)
		{
			writeD(ts.getSkill().getId());
			writeD(ts.getSkill().getLevel());
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] c1 SkillCoolTime";
	}
}