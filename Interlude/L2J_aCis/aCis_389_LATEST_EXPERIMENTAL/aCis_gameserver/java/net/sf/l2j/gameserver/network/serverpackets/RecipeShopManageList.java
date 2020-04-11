package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.craft.ManufactureItem;
import net.sf.l2j.gameserver.model.craft.ManufactureList;
import net.sf.l2j.gameserver.model.item.Recipe;

public class RecipeShopManageList extends L2GameServerPacket
{
	private final Player _player;
	private final Collection<Recipe> _recipes;
	
	public RecipeShopManageList(Player player, boolean isDwarven)
	{
		_player = player;
		_recipes = (isDwarven && player.hasDwarvenCraft()) ? player.getDwarvenRecipeBook() : player.getCommonRecipeBook();
		
		final ManufactureList manufactureList = player.getManufactureList();
		manufactureList.setState(isDwarven);
		
		// Integrity check.
		final Iterator<ManufactureItem> it = manufactureList.getList().iterator();
		while (it.hasNext())
		{
			ManufactureItem item = it.next();
			if (item.isDwarven() != isDwarven || !player.hasRecipeList(item.getId()))
				it.remove();
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xd8);
		writeD(_player.getObjectId());
		writeD(_player.getAdena());
		writeD(_player.getManufactureList().isDwarven() ? 0x00 : 0x01);
		
		if (_recipes == null)
			writeD(0);
		else
		{
			writeD(_recipes.size());// number of items in recipe book
			
			int i = 0;
			for (Recipe recipe : _recipes)
			{
				writeD(recipe.getId());
				writeD(++i);
			}
		}
		
		final List<ManufactureItem> list = _player.getManufactureList().getList();
		writeD(list.size());
		
		for (ManufactureItem item : list)
		{
			writeD(item.getId());
			writeD(0x00);
			writeD(item.getValue());
		}
	}
}