package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * CDSDDSS -> (0xd5)(objId)(name)(0x00)(type)(speaker)(name)
 */

public class Snoop extends L2GameServerPacket
{
	private final L2PcInstance snooped;
	private final int type;
	private final String speaker;
	private final String msg;
	
	public Snoop(final L2PcInstance snooped, final int type, final String speaker, final String msg)
	{
		this.snooped = snooped;
		this.type = type;
		this.speaker = speaker;
		this.msg = msg;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xd5);
		writeD(snooped.getObjectId());
		writeS(snooped.getName());
		writeD(0); // ??
		writeD(type);
		writeS(speaker);
		writeS(msg);
	}
	
	@Override
	public String getType()
	{
		return "[S] D5 Snoop";
	}
}