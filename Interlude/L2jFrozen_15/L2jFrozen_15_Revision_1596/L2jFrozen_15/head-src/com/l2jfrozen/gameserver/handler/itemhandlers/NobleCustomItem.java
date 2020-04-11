// Noble Custom Item , Created By Stefoulis15
// Added From Stefoulis15 Into The Core.
// Visit www.MaxCheaters.com For Support 
// Source File Name:   NobleCustomItem.java
// Modded by programmos, sword dev
package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;

public class NobleCustomItem implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		Config.NOBLE_CUSTOM_ITEM_ID
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (activeChar.isNoble())
		{
			activeChar.sendMessage("You are already Noble!");
		}
		else
		{
			activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 16));
			activeChar.setNoble(true);
			activeChar.sendMessage("You Are Now a Noble,You Are Granted With Noblesse Status , And Noblesse Skills.");
			activeChar.broadcastUserInfo();
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
			activeChar.getInventory().addItem("Tiara", 7694, 1, activeChar, null);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
