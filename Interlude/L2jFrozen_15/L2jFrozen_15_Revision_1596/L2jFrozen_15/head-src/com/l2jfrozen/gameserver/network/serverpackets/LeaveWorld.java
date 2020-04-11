package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * This class ...
 * @version $Revision: 1.3.3 $ $Date: 2009/05/12 19:06:39 $
 */
public class LeaveWorld extends L2GameServerPacket
{
	public static final LeaveWorld STATIC_PACKET = new LeaveWorld();
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7e);
	}
	
	@Override
	public String getType()
	{
		return "[S] 7e LeaveWorld";
	}
	
}
