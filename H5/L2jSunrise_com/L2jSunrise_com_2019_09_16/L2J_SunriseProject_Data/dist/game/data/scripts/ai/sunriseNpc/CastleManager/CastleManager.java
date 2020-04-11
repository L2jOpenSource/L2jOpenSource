package ai.sunriseNpc.CastleManager;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SiegeInfo;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class CastleManager extends AbstractNpcAI
{
	private final int NpcId = CustomNpcsConfigs.CASTLE_NPC_ID;
	
	public CastleManager()
	{
		super(CastleManager.class.getSimpleName(), "ai/sunriseNpc");
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_CASTLE_MANAGER)
		{
			player.sendMessage("Castle manager npc is disabled by admin");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (player.getLevel() <= CustomNpcsConfigs.CASTLE_REQUIRED_LEVEL)
		{
			player.sendMessage("Your level is too low to use this function, you must be at least " + String.valueOf(CustomNpcsConfigs.CASTLE_REQUIRED_LEVEL + 1) + " level.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		Castle castle = l2r.gameserver.instancemanager.CastleManager.getInstance().getCastleById(Integer.parseInt(event));
		if (castle != null)
		{
			sendMainHtmlWindow(player, npc);
			player.sendPacket(new SiegeInfo(castle));
		}
		
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		sendMainHtmlWindow(player, npc);
		return "";
	}
	
	private void sendMainHtmlWindow(L2PcInstance player, L2Npc npc)
	{
		final NpcHtmlMessage html = getHtmlPacket(player, npc, "main.htm");
		html.replace("%player%", player.getName());
		
		player.sendPacket(html);
	}
	
	private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
		return packet;
	}
}