package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

/**
 * sample 0000: 6d 0c 00 00 00 00 00 00 00 03 00 00 00 f3 03 00 m............... 0010: 00 00 00 00 00 01 00 00 00 f4 03 00 00 00 00 00 ................ 0020: 00 01 00 00 00 10 04 00 00 00 00 00 00 01 00 00 ................ 0030: 00 2c 04 00 00 00 00 00 00 03 00 00 00 99 04 00 .,.............. 0040:
 * 00 00 00 00 00 02 00 00 00 a0 04 00 00 00 00 00 ................ 0050: 00 01 00 00 00 c0 04 00 00 01 00 00 00 01 00 00 ................ 0060: 00 76 00 00 00 01 00 00 00 01 00 00 00 a3 00 00 .v.............. 0070: 00 01 00 00 00 01 00 00 00 c2 00 00 00 01 00 00 ................ 0080: 00 01 00 00
 * 00 d6 00 00 00 01 00 00 00 01 00 00 ................ 0090: 00 f4 00 00 00 format d (ddd)
 * @version $Revision: 1.3.2.1.2.5 $ $Date: 2005/03/27 15:29:39 $
 */
public class SkillList extends L2GameServerPacket
{
	private final List<Skill> skills;
	
	static class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		public boolean disabled;
		
		Skill(int pId, int pLevel, boolean pPassive)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = false;
		}
		
		Skill(int pId, int pLevel, boolean pPassive, boolean pDisable)
		{
			id = pId;
			level = pLevel;
			passive = pPassive;
			disabled = pDisable;
		}
	}
	
	public SkillList()
	{
		skills = new ArrayList<>();
	}
	
	public void addSkill(int id, int level, boolean passive)
	{
		skills.add(new Skill(id, level, passive, false));
	}
	
	public void addSkill(int id, int level, boolean passive, boolean disabled)
	{
		skills.add(new Skill(id, level, passive, disabled));
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x58);
		writeD(skills.size());
		
		for (Skill temp : skills)
		{
			writeD(temp.passive ? 1 : 0);
			writeD(temp.level);
			writeD(temp.id);
			writeC(temp.disabled ? 1 : 0);
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 58 SkillList";
	}
}
