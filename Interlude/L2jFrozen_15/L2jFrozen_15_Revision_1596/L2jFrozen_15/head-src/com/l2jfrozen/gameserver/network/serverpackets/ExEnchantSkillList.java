package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillList extends L2GameServerPacket
{
	private final List<Skill> skills;
	
	class Skill
	{
		public int id;
		public int nextLevel;
		public int sp;
		public int exp;
		
		Skill(final int pId, final int pNextLevel, final int pSp, final int pExp)
		{
			id = pId;
			nextLevel = pNextLevel;
			sp = pSp;
			exp = pExp;
		}
	}
	
	public void addSkill(final int id, final int level, final int sp, final int exp)
	{
		skills.add(new Skill(id, level, sp, exp));
	}
	
	public ExEnchantSkillList()
	{
		skills = new ArrayList<>();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x17);
		
		writeD(skills.size());
		for (final Skill sk : skills)
		{
			writeD(sk.id);
			writeD(sk.nextLevel);
			writeD(sk.sp);
			writeQ(sk.exp);
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:17 ExEnchantSkillList";
	}
	
}
