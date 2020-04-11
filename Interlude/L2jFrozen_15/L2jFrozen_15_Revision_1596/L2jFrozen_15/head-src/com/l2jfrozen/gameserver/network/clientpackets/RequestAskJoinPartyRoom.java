package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) S
 * @author -Wooden-
 */
public class RequestAskJoinPartyRoom extends L2GameClientPacket
{
	private static String name;
	
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
		
		// Send PartyRoom invite request (with activeChar) name to the target
		final L2PcInstance target = L2World.getInstance().getPlayer(name);
		if (target != null)
		{
			if (!target.isProcessingRequest())
			{
				activeChar.onTransactionRequest(target);
				target.sendPacket(new ExAskJoinPartyRoom(activeChar.getName()));
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(target.getName()));
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:14 RequestAskJoinPartyRoom";
	}
	
}
