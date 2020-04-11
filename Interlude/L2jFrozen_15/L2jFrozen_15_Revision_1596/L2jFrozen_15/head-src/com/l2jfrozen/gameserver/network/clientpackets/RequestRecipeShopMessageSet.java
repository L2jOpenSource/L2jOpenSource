package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	private String name;
	
	@Override
	protected void readImpl()
	{
		name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		/*
		 * if (player.getCreateList() == null) { player.setCreateList(new L2ManufactureList()); }
		 */
		
		if (player.getCreateList() != null && name.length() < 30)
		{
			player.getCreateList().setStoreName(name);
		}
		
	}
	
	@Override
	public String getType()
	{
		return "[C] b1 RequestRecipeShopMessageSet";
	}
}
