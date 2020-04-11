/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.instance;

import l2r.gameserver.datatables.xml.BuyListData;
import l2r.gameserver.datatables.xml.MerchantPriceConfigData;
import l2r.gameserver.datatables.xml.MerchantPriceConfigData.MerchantPriceConfig;
import l2r.gameserver.enums.InstanceType;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.buylist.L2BuyList;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.BuyList;
import l2r.gameserver.network.serverpackets.ExBuySellList;

/**
 * This class ...
 * @version $Revision: 1.10.4.9 $ $Date: 2005/04/11 10:06:08 $
 */
public class L2MerchantInstance extends L2NpcInstance
{
	private MerchantPriceConfig _mpc;
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2MerchantInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2MerchantInstance);
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_mpc = MerchantPriceConfigData.getInstance().getMerchantPriceConfig(this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";
		
		if (val == 0)
		{
			pom = "" + npcId;
		}
		else
		{
			pom = npcId + "-" + val;
		}
		
		return "data/html/merchant/" + pom + ".htm";
	}
	
	/**
	 * @return Returns the mpc.
	 */
	public MerchantPriceConfig getMpc()
	{
		return _mpc;
	}
	
	public final void showBuyWindow(L2PcInstance player, int val)
	{
		showBuyWindow(player, val, true);
	}
	
	public final void showBuyWindow(L2PcInstance player, int val, boolean applyTax)
	{
		final L2BuyList buyList = BuyListData.getInstance().getBuyList(val);
		if (buyList == null)
		{
			_log.warn("BuyList not found! BuyListId:" + val);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!buyList.isNpcAllowed(getId()))
		{
			_log.warn("Npc not allowed in BuyList! BuyListId:" + val + " NpcId:" + getId());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final double taxRate = (applyTax) ? getMpc().getTotalTaxRate() : 0;
		
		player.tempInventoryDisable();
		
		player.sendPacket(new BuyList(buyList, player.getAdena(), taxRate));
		player.sendPacket(new ExBuySellList(player, taxRate, false));
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
}
