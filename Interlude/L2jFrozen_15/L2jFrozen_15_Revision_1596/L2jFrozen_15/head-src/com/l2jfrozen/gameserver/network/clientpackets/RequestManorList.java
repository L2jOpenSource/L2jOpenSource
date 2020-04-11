package com.l2jfrozen.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ExSendManorList;

/**
 * Format: ch c (id) 0xD0 h (subid) 0x08
 * @author l3x
 */
public class RequestManorList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		final List<String> manorsName = new ArrayList<>();
		manorsName.add("gludio");
		manorsName.add("dion");
		manorsName.add("giran");
		manorsName.add("oren");
		manorsName.add("aden");
		manorsName.add("innadril");
		manorsName.add("goddard");
		manorsName.add("rune");
		manorsName.add("schuttgart");
		final ExSendManorList manorlist = new ExSendManorList(manorsName);
		player.sendPacket(manorlist);
	}
	
	@Override
	public String getType()
	{
		return "[S] FE:08 RequestManorList";
	}
}
