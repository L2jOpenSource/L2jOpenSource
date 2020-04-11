package ai.sunriseNpc.CasinoManager;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;
import l2r.gameserver.network.serverpackets.SocialAction;
import l2r.util.Rnd;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;
import gr.sr.main.Conditions;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class CasinoManager extends AbstractNpcAI
{
	private final int NpcId = CustomNpcsConfigs.CASINO_NPC_ID;
	private static final int chance = CustomNpcsConfigs.CASINO_SUCCESS_CHANCE;
	private static final int itemId = CustomNpcsConfigs.CASINO_ITEM_ID;
	private static final String itemName = ItemData.getInstance().getTemplate(itemId).getName();
	private static final int bet1 = CustomNpcsConfigs.CASINO_BET1;
	private static final int bet2 = CustomNpcsConfigs.CASINO_BET2;
	private static final int bet3 = CustomNpcsConfigs.CASINO_BET3;
	
	public CasinoManager()
	{
		super(CasinoManager.class.getSimpleName(), "ai/sunriseNpc");
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_CASINO_MANAGER)
		{
			player.sendMessage("Casino manager is disabled by admin.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (player.getLevel() <= CustomNpcsConfigs.CASINO_REQUIRED_LEVEL)
		{
			player.sendMessage("Your level is too low to use this function, you must be at least " + String.valueOf(CustomNpcsConfigs.CASTLE_REQUIRED_LEVEL + 1) + " level.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (event.startsWith("bet"))
		{
			String val = event.substring(4);
			int bet = Integer.parseInt(val);
			
			if ((bet == 0) || ((bet != bet1) && (bet != bet2) && (bet != bet3)))
			{
				player.sendMessage("Somthing went wrong.Try again later.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (!Conditions.checkPlayerItemCount(player, itemId, bet))
			{
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if ((Rnd.get(100) <= chance))
			{
				player.getInventory().addItem("bet", itemId, bet, player, true);
				player.broadcastPacket(new SocialAction(player.getObjectId(), 3));
				player.broadcastPacket(new MagicSkillUse(player, player, 2024, 1, 1, 0));
				player.sendMessage("Congratulations you won!");
			}
			else
			{
				player.destroyItemByItemId("bet", itemId, bet, player, true);
				player.sendMessage("I am sorry you lost your bet. Try again you might be luckier");
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
		html.replace("%chance%", String.valueOf(chance));
		html.replace("%bet1%", bet1);
		html.replace("%bet2%", bet2);
		html.replace("%bet3%", bet3);
		html.replace("%itemName%", itemName);
		
		player.sendPacket(html);
	}
	
	private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
		return packet;
	}
}