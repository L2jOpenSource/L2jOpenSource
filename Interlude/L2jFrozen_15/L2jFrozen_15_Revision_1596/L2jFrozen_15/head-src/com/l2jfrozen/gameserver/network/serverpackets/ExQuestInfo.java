package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: $ $Date: $
 * @author  Luca Baldi
 */
public class ExQuestInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x19);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:19 EXQUESTINFO";
	}
}
