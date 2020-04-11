package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.PackageSendableList;

/**
 * Format: (c)d d: char object id (?)
 * @author -Wooden-
 */
public final class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectID;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}
	
	@Override
	public void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		final ItemInstance[] items = player.getInventory().getAvailableItems(true, false);
		if (items == null)
			return;
		
		sendPacket(new PackageSendableList(items, _objectID));
	}
}