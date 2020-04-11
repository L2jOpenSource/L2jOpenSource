package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author  godson
 */
public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private final int side;
	private final L2PcInstance activeChar;
	
	/**
	 * @param player
	 * @param side   (1 = right, 2 = left)
	 */
	public ExOlympiadUserInfo(final L2PcInstance player, final int side)
	{
		activeChar = player;
		this.side = side;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (activeChar == null)
		{
			return;
		}
		writeC(0xfe);
		writeH(0x29);
		writeC(side);
		writeD(activeChar.getObjectId());
		writeS(activeChar.getName());
		writeD(activeChar.getClassId().getId());
		writeD((int) activeChar.getCurrentHp());
		writeD(activeChar.getMaxHp());
		writeD((int) activeChar.getCurrentCp());
		writeD(activeChar.getMaxCp());
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:29 ExOlympiadUserInfo";
	}
}
