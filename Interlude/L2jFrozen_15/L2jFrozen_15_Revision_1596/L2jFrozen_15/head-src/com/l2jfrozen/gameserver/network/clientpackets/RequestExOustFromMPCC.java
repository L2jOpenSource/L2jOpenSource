package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden- D0 0F 00 5A 00 77 00 65 00 72 00 67 00 00 00
 */
public final class RequestExOustFromMPCC extends L2GameClientPacket
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
		final L2PcInstance target = L2World.getInstance().getPlayer(name);
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (target != null && target.isInParty() && activeChar.isInParty() && activeChar.getParty().isInCommandChannel() && target.getParty().isInCommandChannel() && activeChar.getParty().getCommandChannel().getChannelLeader().equals(activeChar))
		{
			target.getParty().getCommandChannel().removeParty(target.getParty());
			
			SystemMessage sm = SystemMessage.sendString("Your party was dismissed from the CommandChannel.");
			target.getParty().broadcastToPartyMembers(sm);
			
			sm = SystemMessage.sendString(target.getParty().getPartyMembers().get(0).getName() + "'s party was dismissed from the CommandChannel.");
		}
		else
		{
			activeChar.sendMessage("Incorrect Target");
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0F RequestExOustFromMPCC";
	}
	
}
