package handlers.itemhandlers;

import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.configsEngine.configs.impl.AioItemsConfigs;

/**
 * @author DoctorNo
 */
public class AioItemNpcs implements IItemHandler
{
	private static final int ITEM_IDS = AioItemsConfigs.AIO_ITEM_ID;
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		// No null pointers
		if (playable == null)
		{
			return false;
		}
		L2PcInstance player = null;
		// Just for players
		if (!(playable instanceof L2PcInstance))
		{
			return false;
		}
		
		player = (L2PcInstance) playable;
		
		int itemId = item.getId();
		if (itemId == AioItemsConfigs.AIO_ITEM_ID)
		{
			String htmFile = "data/html/sunrise/AioItemNpcs/main.htm";
			NpcHtmlMessage msg = new NpcHtmlMessage();
			msg.setFile(player, player.getHtmlPrefix(), htmFile);
			player.sendPacket(msg);
		}
		return true;
	}
	
	public int getItemIds()
	{
		return ITEM_IDS;
	}
}