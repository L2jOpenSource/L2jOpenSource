package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewItemList extends L2GameServerPacket
{
	private final L2ItemInstance[] items;
	private final L2PcInstance cha;
	private final String playerName;
	
	public GMViewItemList(final L2PcInstance cha)
	{
		items = cha.getInventory().getItems();
		playerName = cha.getName();
		this.cha = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x94);
		writeS(playerName);
		writeD(cha.getInventoryLimit()); // inventory limit
		writeH(0x01); // show window ??
		writeH(items.length);
		
		for (final L2ItemInstance temp : items)
		{
			if (temp == null || temp.getItem() == null)
			{
				continue;
			}
			
			writeH(temp.getItem().getType1());
			
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 0x01 : 0x00);
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			if (temp.isAugmented())
			{
				writeD(temp.getAugmentation().getAugmentationId());
			}
			else
			{
				writeD(0x00);
			}
			writeD(-1); // C6
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 94 GMViewItemList";
	}
}
