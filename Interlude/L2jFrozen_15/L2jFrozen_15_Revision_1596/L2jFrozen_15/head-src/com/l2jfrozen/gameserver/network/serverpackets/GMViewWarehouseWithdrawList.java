package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.templates.L2Weapon;

/**
 * Sdh(h dddhh [dhhh] d) Sdh ddddd ddddd ddddd ddddd
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final L2ItemInstance[] items;
	private final String playerName;
	private final L2PcInstance activeChar;
	private final int money;
	
	public GMViewWarehouseWithdrawList(final L2PcInstance cha)
	{
		activeChar = cha;
		items = activeChar.getWarehouse().getItems();
		playerName = activeChar.getName();
		money = activeChar.getAdena();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x95);
		writeS(playerName);
		writeD(money);
		writeH(items.length);
		
		for (final L2ItemInstance item : items)
		{
			writeH(item.getItem().getType1());
			
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(item.getCustomType1());
			
			switch (item.getItem().getType2())
			{
				case L2Item.TYPE2_WEAPON:
				{
					writeD(item.getItem().getBodyPart());
					writeH(item.getEnchantLevel());
					writeH(((L2Weapon) item.getItem()).getSoulShotCount());
					writeH(((L2Weapon) item.getItem()).getSpiritShotCount());
					break;
				}
				
				case L2Item.TYPE2_SHIELD_ARMOR:
				case L2Item.TYPE2_ACCESSORY:
				case L2Item.TYPE2_PET_WOLF:
				case L2Item.TYPE2_PET_HATCHLING:
				case L2Item.TYPE2_PET_STRIDER:
				case L2Item.TYPE2_PET_BABY:
				{
					writeD(item.getItem().getBodyPart());
					writeH(item.getEnchantLevel());
					writeH(0x00);
					writeH(0x00);
					break;
				}
			}
			
			writeD(item.getObjectId());
			
			switch (item.getItem().getType2())
			{
				case L2Item.TYPE2_WEAPON:
				{
					if (item.isAugmented())
					{
						writeD(0x0000FFFF & item.getAugmentation().getAugmentationId());
						writeD(item.getAugmentation().getAugmentationId() >> 16);
					}
					else
					{
						writeD(0);
						writeD(0);
					}
					
					break;
				}
				
				case L2Item.TYPE2_SHIELD_ARMOR:
				case L2Item.TYPE2_ACCESSORY:
				case L2Item.TYPE2_PET_WOLF:
				case L2Item.TYPE2_PET_HATCHLING:
				case L2Item.TYPE2_PET_STRIDER:
				case L2Item.TYPE2_PET_BABY:
				{
					writeD(0);
					writeD(0);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 95 GMViewWarehouseWithdrawList";
	}
}
