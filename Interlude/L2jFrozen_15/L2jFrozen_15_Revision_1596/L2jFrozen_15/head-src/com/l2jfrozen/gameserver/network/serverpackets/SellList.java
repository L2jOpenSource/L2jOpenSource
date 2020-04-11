package com.l2jfrozen.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.3.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class SellList extends L2GameServerPacket
{
	private static Logger LOGGER = Logger.getLogger(SellList.class);
	private final L2PcInstance activeChar;
	private final L2MerchantInstance lease;
	private final int money;
	private final List<L2ItemInstance> selllist = new ArrayList<>();
	
	public SellList(final L2PcInstance player)
	{
		activeChar = player;
		lease = null;
		money = activeChar.getAdena();
		doLease();
	}
	
	public SellList(final L2PcInstance player, final L2MerchantInstance lease)
	{
		activeChar = player;
		this.lease = lease;
		money = activeChar.getAdena();
		doLease();
	}
	
	private void doLease()
	{
		if (lease == null)
		{
			for (final L2ItemInstance item : activeChar.getInventory().getItems())
			{
				if (item != null && !item.isEquipped() && // Not equipped
					item.getItem().isSellable() && // Item is sellable
					item.getItem().getItemId() != 57 && // Adena is not sellable
					(activeChar.getPet() == null || // Pet not summoned or
						item.getObjectId() != activeChar.getPet().getControlItemId())) // Pet is summoned and not the item that summoned the pet
				{
					selllist.add(item);
					if (Config.DEBUG)
					{
						LOGGER.debug("item added to selllist: " + item.getItem().getName());
					}
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x10);
		writeD(money);
		writeD(lease == null ? 0x00 : 1000000 + lease.getTemplate().npcId);
		
		writeH(selllist.size());
		
		for (final L2ItemInstance item : selllist)
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
			
			if (lease == null)
			{
				writeD(item.getItem().getReferencePrice() / 2); // wtf??? there is no conditional part in SellList!! this d should allways be here 0.o! fortunately the lease stuff are never ever use so the if allways exectues
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 10 SellList";
	}
}