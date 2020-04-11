package com.l2jfrozen.gameserver.network.clientpackets;

/**
 * Format:(ch) just a trigger
 * @author -Wooden-
 */
public final class RequestDuelSurrender extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}
	
	@Override
	protected void runImpl()
	{
		// TODO: Track Duel bug to use Snipe or Ultimate Defense and move
		// DuelManager.getInstance().doSurrender(getClient().getActiveChar());
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:30 RequestDuelSurrender";
	}
}
