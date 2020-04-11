package handlers.itemhandlers;

import l2r.gameserver.handler.IItemHandler;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.configsEngine.configs.impl.BufferConfigs;

/**
 * @author DoctorNo
 */
public class AioItemBuff implements IItemHandler
{
	private static final int ITEM_IDS = BufferConfigs.DONATE_BUFF_ITEM_ID;
	
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		// Just for players
		if (!playable.isPlayer())
		{
			return false;
		}
		
		L2PcInstance player = null;
		player = playable.getActingPlayer();
		
		String htmFile = "data/html/sunrise/ItemBuffer/main.htm";
		NpcHtmlMessage msg = new NpcHtmlMessage();
		msg.setFile(player, player.getHtmlPrefix(), htmFile);
		player.sendPacket(msg);
		
		return true;
	}
	
	public int getItemIds()
	{
		return ITEM_IDS;
	}
}