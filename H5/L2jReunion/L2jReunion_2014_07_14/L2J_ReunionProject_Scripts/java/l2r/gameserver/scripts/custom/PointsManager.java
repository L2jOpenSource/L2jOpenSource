package l2r.gameserver.scripts.custom;

import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.UserInfo;

/**
 * @author -=DoctorNo=-
 */
public final class PointsManager extends Quest
{
	private static final int NpcId = 554;
	private static final int REP_ITEM_ID = 40002; // item id used as payment for clan points
	private static final int REP_PRICE = 500; // payment price for clan reputation
	private static final int REP_SCORE = 500; // amount of clan reputation points
	private static final int FAME_ITEM_ID = 40002; // item id used as payment for fame points
	private static final int FAME_PRICE = 500; // payment price for fame points
	private static final int FAME_SCORE = 500; // amount of fame points
	
	public PointsManager()
	{
		super(-1, "PointsManager", "custom");
		
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	/**
	 * Method to manage all player bypasses
	 * @param player
	 */
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		// Add clan reputation
		if (event.startsWith("clanRep"))
		{
			if ((player.getClan() == null) || (!player.isClanLeader()))
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return "";
			}
			if (((player.getInventory().getItemByItemId(REP_ITEM_ID) == null) || (player.getInventory().getItemByItemId(REP_ITEM_ID).getCount() < REP_PRICE)))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return "";
			}
			player.getClan().addReputationScore(REP_SCORE, true);
			player.destroyItemByItemId("clan", REP_ITEM_ID, REP_PRICE, player, true);
			player.sendMessage("You have successfully add " + REP_SCORE + " reputation point(s) to your clan.");
		}
		
		// Add fame points
		else if (event.startsWith("addFame"))
		{
			if ((player.getInventory().getItemByItemId(FAME_ITEM_ID) == null) || (player.getInventory().getItemByItemId(FAME_ITEM_ID).getCount() < FAME_PRICE))
			{
				player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				return "";
			}
			player.destroyItemByItemId("fame", FAME_ITEM_ID, FAME_PRICE, player, true);
			player.setFame(player.getFame() + FAME_SCORE);
			player.broadcastUserInfo();
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));
			player.sendMessage("You have successfully add " + FAME_SCORE + " fame point(s).");
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int npcId = npc.getId();
		
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		
		if (npcId == NpcId)
		{
			String html = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/scripts/custom/PointsManager/1.htm");
			html = html.replaceAll("%player%", player.getName());
			html = html.replaceAll("%REP_PRICE%", String.valueOf(REP_PRICE));
			html = html.replaceAll("%FAME_PRICE%", String.valueOf(FAME_PRICE));
			html = html.replaceAll("%REP_ITEM_ID%", ItemData.getInstance().getTemplate(REP_ITEM_ID).getName());
			html = html.replaceAll("%FAME_ITEM_ID%", ItemData.getInstance().getTemplate(FAME_ITEM_ID).getName());
			html = html.replaceAll("%REP_SCORE%", String.valueOf(REP_SCORE));
			html = html.replaceAll("%FAME_SCORE%", String.valueOf(FAME_SCORE));
			
			NpcHtmlMessage npcHtml = new NpcHtmlMessage(0);
			npcHtml.setHtml(html);
			player.sendPacket(npcHtml);
		}
		return "";
	}
	
	public static void main(String[] args)
	{
		new PointsManager();
	}
}