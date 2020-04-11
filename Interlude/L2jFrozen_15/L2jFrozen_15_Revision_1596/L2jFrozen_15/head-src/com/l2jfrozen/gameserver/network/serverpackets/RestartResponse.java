package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class RestartResponse extends L2GameServerPacket
{
	private static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);
	private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);
	private final String message;
	private final boolean result;
	
	public static final RestartResponse valueOf(final boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	public RestartResponse(final boolean result)
	{
		this.result = result;
		message = "ok merong~ khaha"; // Message like L2OFF
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x5f);
		writeD(result ? 1 : 0);
		writeS(message);
	}
	
	@Override
	public String getType()
	{
		return "[S] 74 RestartResponse";
	}
}