
package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * @author L2JFrozen
 */
public class RequestSiegeInfo extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		// TODO this
	}
	
	@Override
	public String getType()
	{
		return "[C] 0x47 RequestSiegeInfo";
	}
}
