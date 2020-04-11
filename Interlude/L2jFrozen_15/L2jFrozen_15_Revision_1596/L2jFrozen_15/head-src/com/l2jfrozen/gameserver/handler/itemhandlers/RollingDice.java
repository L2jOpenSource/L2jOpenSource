package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.Dice;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.Broadcast;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:30:07 $
 */

public class RollingDice implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		4625,
		4626,
		4627,
		4628
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		
		if (!activeChar.getFloodProtectors().getRollDice().tryPerformAction("RollDice"))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(itemId);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			final int number = rollDice(activeChar);
			if (number == 0)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER));
				return;
			}
			
			Dice d = new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ());
			Broadcast.toSelfAndKnownPlayers(activeChar, d);
			d = null;
			
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_ROLLED_S2);
			sm.addString(activeChar.getName());
			sm.addNumber(number);
			activeChar.sendPacket(sm);
			if (activeChar.isInsideZone(L2Character.ZONE_PEACE))
			{
				Broadcast.toKnownPlayers(activeChar, sm);
			}
			else if (activeChar.isInParty())
			{
				activeChar.getParty().broadcastToPartyMembers(activeChar, sm);
			}
			sm = null;
		}
		
		activeChar = null;
	}
	
	private int rollDice(final L2PcInstance player)
	{
		// Check if the dice is ready
		return Rnd.get(1, 6);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
