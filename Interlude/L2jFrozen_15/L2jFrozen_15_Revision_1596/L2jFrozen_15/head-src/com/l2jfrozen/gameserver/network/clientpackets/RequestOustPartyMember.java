package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public final class RequestOustPartyMember extends L2GameClientPacket
{
	private String name;
	
	@Override
	protected void readImpl()
	{
		name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isInParty() && activeChar.getParty().isLeader(activeChar))
		{
			if (activeChar.getParty().isInDimensionalRift() && !activeChar.getParty().getDimensionalRift().getRevivedAtWaitingRoom().contains(activeChar))
			{
				activeChar.sendMessage("You can't dismiss party member when you are in Dimensional Rift.");
			}
			else
			{
				activeChar.getParty().removePartyMember(name);
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 2C RequestOustPartyMember";
	}
}
