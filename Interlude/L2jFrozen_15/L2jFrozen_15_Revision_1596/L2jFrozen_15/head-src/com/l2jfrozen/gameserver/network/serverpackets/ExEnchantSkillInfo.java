package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
	private final List<Req> reqs;
	private final int skillId;
	private final int level;
	private final int spCost;
	private final int xpCost;
	private final int rate;
	
	class Req
	{
		public int id;
		public int count;
		public int type;
		public int unk;
		
		Req(final int pType, final int pId, final int pCount, final int pUnk)
		{
			id = pId;
			type = pType;
			count = pCount;
			unk = pUnk;
		}
	}
	
	public ExEnchantSkillInfo(final int id, final int level, final int spCost, final int xpCost, final int rate)
	{
		reqs = new ArrayList<>();
		skillId = id;
		this.level = level;
		this.spCost = spCost;
		this.xpCost = xpCost;
		this.rate = rate;
	}
	
	public void addRequirement(final int type, final int id, final int count, final int unk)
	{
		reqs.add(new Req(type, id, count, unk));
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x18);
		
		writeD(skillId);
		writeD(level);
		writeD(spCost);
		writeQ(xpCost);
		writeD(rate);
		
		writeD(reqs.size());
		
		for (final Req temp : reqs)
		{
			writeD(temp.type);
			writeD(temp.id);
			writeD(temp.count);
			writeD(temp.unk);
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:18 ExEnchantSkillInfo";
	}
	
}
