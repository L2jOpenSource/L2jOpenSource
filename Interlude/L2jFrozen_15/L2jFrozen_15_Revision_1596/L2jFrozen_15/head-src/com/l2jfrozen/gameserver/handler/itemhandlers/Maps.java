package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.network.serverpackets.ShowMiniMap;

/**
 * This class provides handling for items that should display a map when double clicked.
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:30:07 $
 */

public class Maps implements IItemHandler
{
	// all the items ids that this handler knowns
	private static final int[] ITEM_IDS =
	{
		1665,
		1863,
		7063
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		final int itemId = item.getItemId();
		if (itemId == 7063)
		{
			activeChar.sendPacket(new ShowMiniMap(1665));
			activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
		}
		else
		{
			activeChar.sendPacket(new ShowMiniMap(itemId));
		}
		
		return;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
