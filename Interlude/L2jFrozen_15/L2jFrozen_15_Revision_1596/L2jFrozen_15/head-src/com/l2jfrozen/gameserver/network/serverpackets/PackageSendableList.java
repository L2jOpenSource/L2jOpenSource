package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;

/**
 * @author -Wooden-
 */
public class PackageSendableList extends L2GameServerPacket
{
	private final L2ItemInstance[] items;
	private final int playerObjId;
	
	public PackageSendableList(final L2ItemInstance[] items, final int playerObjId)
	{
		this.items = items;
		this.playerObjId = playerObjId;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xC3);
		
		writeD(playerObjId);
		writeD(getClient().getActiveChar().getAdena());
		writeD(items.length);
		for (final L2ItemInstance item : items) // format inside the for taken from SellList part use should be about the same
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(0x00);
			writeD(item.getObjectId()); // some item identifier later used by client to answer (see RequestPackageSend) not item id nor object id maybe some freight system id??
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[S] C3 PackageSendableList";
	}
}
