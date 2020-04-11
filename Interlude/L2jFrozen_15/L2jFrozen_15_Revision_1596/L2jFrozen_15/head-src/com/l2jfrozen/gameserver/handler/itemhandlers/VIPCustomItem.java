// Hero Custom Item , Created By Stefoulis15
// Added From Stefoulis15 Into The Core.
// Visit www.MaxCheaters.com For Support 
// Source File Name:   HeroCustomItem.java
// Modded by programmos, sword dev
package com.l2jfrozen.gameserver.handler.itemhandlers;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;

/**
 * Thanks to Piotr
 */
public class VIPCustomItem implements IItemHandler
{
	protected static final Logger LOGGER = Logger.getLogger(VIPCustomItem.class);
	private static final long dayInMiliseconds = 86400000;
	
	private static final int ITEM_IDS[] =
	{
		Config.VIP_ITEM_ID
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!playable.isPlayer())
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("This item cannot be used on Olympiad Games.");
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.isVIP())
		{
			activeChar.sendMessage("You are already VIP!");
		}
		else
		{
			activeChar.setVIP(true);
			
			long vipEndDate = 0;
			
			if (Config.VIP_X_DAYS == 0)
			{
				activeChar.setVIPEndDate(vipEndDate);
			}
			else if (Config.VIP_X_DAYS > 0)
			{
				vipEndDate = (Config.VIP_X_DAYS * dayInMiliseconds) + System.currentTimeMillis();
				activeChar.setVIPEndDate(vipEndDate);
			}
			
			activeChar.sendMessage("You are now VIP");
			activeChar.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			
			activeChar.setVariable(L2PcInstance.VIP_END, vipEndDate, true);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}