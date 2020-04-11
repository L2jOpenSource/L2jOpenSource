package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.PackageSendableList;

/**
 * Format: (c)d d: char object id (?)
 * @author -Wooden-
 */
public final class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int objectID;
	
	@Override
	protected void readImpl()
	{
		objectID = readD();
	}
	
	@Override
	public void runImpl()
	{
		
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (player.getObjectId() == objectID)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		/*
		 * L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_objectID); if(target == null) return;
		 */
		final L2ItemInstance[] items = getClient().getActiveChar().getInventory().getAvailableItems(true);
		// build list...
		sendPacket(new PackageSendableList(items, objectID));
	}
	
	@Override
	public String getType()
	{
		return "[C] 9E RequestPackageSendableItemList";
	}
	
}
