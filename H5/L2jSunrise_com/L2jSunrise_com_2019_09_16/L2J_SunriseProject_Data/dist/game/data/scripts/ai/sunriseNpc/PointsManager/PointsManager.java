package ai.sunriseNpc.PointsManager;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class PointsManager extends AbstractNpcAI
{
	private static final int NpcId = CustomNpcsConfigs.POINTS_NPC_ID;
	private static final int REP_ITEM_ID = CustomNpcsConfigs.POINTS_ITEM_ID_FOR_REP;
	private static final int REP_PRICE = CustomNpcsConfigs.POINTS_ITEM_AMOUNT_FOR_REP;
	private static final int REP_SCORE = CustomNpcsConfigs.POINTS_AMOUNT_FOR_REP;
	private static final int FAME_ITEM_ID = CustomNpcsConfigs.POINTS_ITEM_ID_FOR_FAME;
	private static final int FAME_PRICE = CustomNpcsConfigs.POINTS_ITEM_AMOUNT_FOR_FAME;
	private static final int FAME_SCORE = CustomNpcsConfigs.POINTS_AMOUNT_FOR_FAME;
	
	public PointsManager()
	{
		super(PointsManager.class.getSimpleName(), "ai/sunriseNpc");
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_POINTS_MANAGER)
		{
			player.sendMessage("Points manager npc is disabled by admin");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		// Add clan reputation
		if (event.startsWith("clanRep"))
		{
			if ((player.getClan() == null) || (!player.isClanLeader()))
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.destroyItemByItemId("clan", REP_ITEM_ID, REP_PRICE, player, true))
			{
				player.getClan().addReputationScore(REP_SCORE, true);
				player.sendMessage("You have successfully add " + REP_SCORE + " reputation point(s) to your clan.");
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
		}
		
		// Add fame points
		if (event.startsWith("addFame"))
		{
			if (player.destroyItemByItemId("fame", FAME_ITEM_ID, FAME_PRICE, player, true))
			{
				player.setFame(player.getFame() + FAME_SCORE);
				player.sendUserInfo(true);
				player.sendMessage("You have successfully add " + FAME_SCORE + " fame point(s).");
			}
			else
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
		}
		
		sendMainHtmlWindow(player, npc);
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
		html.replace("%REP_PRICE%", String.valueOf(REP_PRICE));
		html.replace("%FAME_PRICE%", String.valueOf(FAME_PRICE));
		html.replace("%REP_ITEM_ID%", ItemData.getInstance().getTemplate(REP_ITEM_ID).getName());
		html.replace("%FAME_ITEM_ID%", ItemData.getInstance().getTemplate(FAME_ITEM_ID).getName());
		html.replace("%REP_SCORE%", String.valueOf(REP_SCORE));
		html.replace("%FAME_SCORE%", String.valueOf(FAME_SCORE));
		
		player.sendPacket(html);
	}
	
	private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
		return packet;
	}
}