package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * Format: (ch) dd
 * @author -Wooden-
 */
public class PledgeSkillListAdd extends L2GameServerPacket
{
	private final int id;
	private final int lvl;
	
	public PledgeSkillListAdd(final int id, final int lvl)
	{
		this.id = id;
		this.lvl = lvl;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3a);
		
		writeD(id);
		writeD(lvl);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:3A PledgeSkillListAdd";
	}
}
