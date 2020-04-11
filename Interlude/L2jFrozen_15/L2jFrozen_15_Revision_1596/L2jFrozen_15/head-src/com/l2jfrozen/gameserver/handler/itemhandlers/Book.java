package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.RadarControl;
import com.l2jfrozen.gameserver.network.serverpackets.ShowMiniMap;

public class Book implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5588,
		6317,
		7561,
		7064,
		7082,
		7083,
		7084,
		7085,
		7086,
		7087,
		7088,
		7089,
		7090,
		7091,
		7092,
		7093,
		7094,
		7095,
		7096,
		7097,
		7098,
		7099,
		7100,
		7101,
		7102,
		7103,
		7104,
		7105,
		7106,
		7107,
		7108,
		7109,
		7110,
		7111,
		7112
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
		
		// Quest item: Lidia's diary
		if (itemId == 7064)
		{
			activeChar.sendPacket(new ShowMiniMap(1665));
			activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/help/" + itemId + ".htm");
		activeChar.sendPacket(html);
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
