package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.multisell.MultiSellEntry;
import com.l2jfrozen.gameserver.model.multisell.MultiSellIngredient;
import com.l2jfrozen.gameserver.model.multisell.MultiSellListContainer;

/**
 * @author luisantonioa
 */
public class MultiSellList extends L2GameServerPacket
{
	protected int listId, page, finished;
	protected MultiSellListContainer list;
	
	public MultiSellList(final MultiSellListContainer list, final int page, final int finished)
	{
		this.list = list;
		listId = list.getListId();
		this.page = page;
		this.finished = finished;
	}
	
	@Override
	protected void writeImpl()
	{
		// [ddddd] [dchh] [hdhdh] [hhdh]
		
		writeC(0xd0);
		writeD(listId); // list id
		writeD(page); // page
		writeD(finished); // finished
		writeD(0x28); // size of pages
		writeD(list == null ? 0 : list.getEntries().size()); // list lenght
		
		if (list != null)
		{
			for (final MultiSellEntry ent : list.getEntries())
			{
				writeD(ent.getEntryId());
				writeD(0x00); // C6
				writeD(0x00); // C6
				writeC(1);
				writeH(ent.getProducts().size());
				writeH(ent.getIngredients().size());
				
				for (final MultiSellIngredient i : ent.getProducts())
				{
					writeH(i.getItemId());
					writeD(ItemTable.getInstance().getTemplate(i.getItemId()).getBodyPart());
					writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
					writeD(i.getItemCount());
					writeH(i.getEnchantmentLevel()); // enchtant lvl
					writeD(0x00); // C6
					writeD(0x00); // C6
				}
				
				for (final MultiSellIngredient i : ent.getIngredients())
				{
					final int items = i.getItemId();
					int typeE = 65335;
					if (items != 65336 && items != 65436)
					{
						typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
					}
					writeH(items); // ID
					writeH(typeE);
					writeD(i.getItemCount()); // Count
					writeH(i.getEnchantmentLevel()); // Enchant Level
					writeD(0x00); // C6
					writeD(0x00); // C6
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] D0 MultiSellList";
	}
	
}
