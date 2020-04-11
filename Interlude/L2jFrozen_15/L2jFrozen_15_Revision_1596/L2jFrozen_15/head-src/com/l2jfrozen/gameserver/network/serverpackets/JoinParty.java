package com.l2jfrozen.gameserver.network.serverpackets;

/**
 * sample
 * <p>
 * 4c 01 00 00 00
 * <p>
 * format cd.
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public final class JoinParty extends L2GameServerPacket
{
	private final int response;
	
	public JoinParty(final int response)
	{
		this.response = response;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x3a);
		
		writeD(response);
	}
	
	@Override
	public String getType()
	{
		return "[S] 3a JoinParty";
	}
	
}
