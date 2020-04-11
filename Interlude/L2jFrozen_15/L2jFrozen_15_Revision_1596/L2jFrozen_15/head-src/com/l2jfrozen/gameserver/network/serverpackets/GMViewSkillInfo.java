
package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	private L2Skill[] skills;
	
	public GMViewSkillInfo(final L2PcInstance cha)
	{
		activeChar = cha;
		skills = activeChar.getAllSkills();
		if (skills.length == 0)
		{
			skills = new L2Skill[0];
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeS(activeChar.getName());
		writeD(skills.length);
		
		for (final L2Skill skill : skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getId());
			writeC(0x00); // c5
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 91 GMViewSkillInfo";
	}
}
