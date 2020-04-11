package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

public final class L2TrainerInstance extends L2FolkInstance
{
	public L2TrainerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
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
		
		return "data/html/trainer/" + pom + ".htm";
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if (player.isAio())
		{
			player.sendMessage("AIO player can not learn skills.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		super.onBypassFeedback(player, command);
	}
}
