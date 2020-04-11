package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2Skill;

/**
 * Format: (ch) d [dd].
 * @author -Wooden-
 */
public class PledgeSkillList extends L2GameServerPacket
{
	private final L2Clan clan;
	
	public PledgeSkillList(final L2Clan clan)
	{
		this.clan = clan;
	}
	
	@Override
	protected void writeImpl()
	{
		final L2Skill[] skills = clan.getAllSkills();
		
		writeC(0xfe);
		writeH(0x39);
		writeD(skills.length);
		for (final L2Skill sk : skills)
		{
			writeD(sk.getId());
			writeD(sk.getLevel());
		}
	}
	
	/**
	 * Gets the type.
	 * @return the type
	 */
	@Override
	public String getType()
	{
		return "[S] FE:39 PledgeSkillList";
	}
}
