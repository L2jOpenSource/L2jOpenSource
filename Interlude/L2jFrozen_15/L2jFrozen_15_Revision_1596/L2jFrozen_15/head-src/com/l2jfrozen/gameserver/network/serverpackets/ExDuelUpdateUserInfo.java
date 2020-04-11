package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * Format: ch Sddddddddd.
 * @author KenM
 */
public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private final L2PcInstance activeChar;
	
	public ExDuelUpdateUserInfo(final L2PcInstance cha)
	{
		activeChar = cha;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(activeChar.getName());
		writeD(activeChar.getObjectId());
		writeD(activeChar.getClassId().getId());
		writeD(activeChar.getLevel());
		writeD((int) activeChar.getCurrentHp());
		writeD(activeChar.getMaxHp());
		writeD((int) activeChar.getCurrentMp());
		writeD(activeChar.getMaxMp());
		writeD((int) activeChar.getCurrentCp());
		writeD(activeChar.getMaxCp());
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:4F ExDuelUpdateUserInfo";
	}
	
}
