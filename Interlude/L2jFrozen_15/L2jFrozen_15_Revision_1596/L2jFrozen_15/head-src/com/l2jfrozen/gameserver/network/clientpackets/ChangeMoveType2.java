package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class ChangeMoveType2 extends L2GameClientPacket
{
	private boolean typeRun;
	
	@Override
	protected void readImpl()
	{
		typeRun = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (typeRun)
		{
			player.setRunning();
		}
		else
		{
			player.setWalking();
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 1C ChangeMoveType2";
	}
}