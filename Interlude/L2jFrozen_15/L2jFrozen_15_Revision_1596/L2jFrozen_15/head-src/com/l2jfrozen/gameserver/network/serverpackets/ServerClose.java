package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * @author devScarlet & mrTJO
 */
public class ServerClose extends L2GameServerPacket
{
	public static final ServerClose STATIC_PACKET = new ServerClose();
	
	/**
	 * @see com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x26);
	}
	
	/**
	 * @see com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] 26 ServerClose";
	}
}
