package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class CharCreateFail extends L2GameServerPacket
{
	public static final int REASON_CREATION_FAILED = 0x00;
	public static final int REASON_TOO_MANY_CHARACTERS = 0x01;
	public static final int REASON_NAME_ALREADY_EXISTS = 0x02;
	public static final int REASON_16_ENG_CHARS = 0x03;
	
	private final int error;
	
	public CharCreateFail(final int errorCode)
	{
		error = errorCode;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x1a);
		writeD(error);
	}
	
	@Override
	public String getType()
	{
		return "[S] 1a CharCreateFail";
	}
	
}
