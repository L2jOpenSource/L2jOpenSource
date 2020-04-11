package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharDeleteFail extends L2GameServerPacket
{
	public static final int REASON_DELETION_FAILED = 0x01;
	public static final int REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER = 0x02;
	public static final int REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED = 0x03;
	
	private final int error;
	
	public CharDeleteFail(final int errorCode)
	{
		error = errorCode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x24);
		writeD(error);
	}
	
	@Override
	public String getType()
	{
		return "[S] 24 CharDeleteFail";
	}
}
