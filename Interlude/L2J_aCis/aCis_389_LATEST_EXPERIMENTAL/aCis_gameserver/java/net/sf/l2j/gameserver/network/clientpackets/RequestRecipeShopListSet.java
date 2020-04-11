package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.actors.OperateType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.ManufactureItem;
import net.sf.l2j.gameserver.model.craft.ManufactureList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopManageList;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;

public final class RequestRecipeShopListSet extends L2GameClientPacket
{
	private int _count;
	private int[] _items;
	
	@Override
	protected void readImpl()
	{
		_count = readD();
		if (_count < 0 || _count * 8 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
			_count = 0;
		
		_items = new int[_count * 2];
		for (int x = 0; x < _count; x++)
		{
			int recipeID = readD();
			_items[x * 2 + 0] = recipeID;
			int cost = readD();
			_items[x * 2 + 1] = cost;
		}
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		// Retrieve and clear the manufacture list.
		final ManufactureList manufactureList = player.getManufactureList();
		manufactureList.clear();
		
		// Check multiple conditions. Message is sent directly from the method.
		if (!player.canOpenPrivateStore(false))
			return;
		
		// Integrity check.
		if (_count == 0)
		{
			player.sendPacket(SystemMessageId.NO_RECIPES_REGISTERED);
			player.sendPacket(new RecipeShopManageList(player, manufactureList.isDwarven()));
			return;
		}
		
		// Feed it with packet informations.
		for (int x = 0; x < _count; x++)
		{
			int recipeID = _items[x * 2 + 0];
			int cost = _items[x * 2 + 1];
			
			manufactureList.add(new ManufactureItem(recipeID, cost));
		}
		
		player.sitDown();
		player.setOperateType(OperateType.MANUFACTURE);
		player.broadcastUserInfo();
		player.broadcastPacket(new RecipeShopMsg(player));
	}
}