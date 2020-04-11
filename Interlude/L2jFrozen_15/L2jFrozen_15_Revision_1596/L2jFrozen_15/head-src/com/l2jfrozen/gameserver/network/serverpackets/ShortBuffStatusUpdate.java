package com.l2jfrozen.gameserver.network.serverpackets;

public class ShortBuffStatusUpdate extends L2GameServerPacket
{
	private final int skillId;
	private final int skillLvl;
	private final int duration;
	
	public ShortBuffStatusUpdate(final int skillId, final int skillLvl, final int duration)
	{
		this.skillId = skillId;
		this.skillLvl = skillLvl;
		this.duration = duration;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xF4);
		writeD(skillId);
		writeD(skillLvl);
		writeD(duration);
	}
	
	@Override
	public String getType()
	{
		return "[S] F4 ShortBuffStatusUpdate";
	}
}