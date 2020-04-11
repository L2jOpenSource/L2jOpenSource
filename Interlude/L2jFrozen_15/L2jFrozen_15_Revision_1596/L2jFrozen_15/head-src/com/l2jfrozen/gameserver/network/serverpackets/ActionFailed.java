package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class ActionFailed extends L2GameServerPacket
{
	public static final ActionFailed STATIC_PACKET = new ActionFailed();
	
	public ActionFailed()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x25);
	}
	
	@Override
	public String getType()
	{
		return "[S] 25 ActionFailed";
	}
}
