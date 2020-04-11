package ai.sunriseNpc.PremiumManager;

import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.NpcHtmlMessage;

import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;
import gr.sr.premiumEngine.PremiumHandler;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class PremiumManager extends AbstractNpcAI
{
	private final int NpcId = CustomNpcsConfigs.PREMIUM_NPC_ID;
	private final int ItemId = CustomNpcsConfigs.PREMIUM_ITEM_ID;
	private final int ItemAmountforPremium1 = CustomNpcsConfigs.PREMIUM_ITEM_AMOUNT_1;
	private final int ItemAmountforPremium2 = CustomNpcsConfigs.PREMIUM_ITEM_AMOUNT_2;
	private final int ItemAmountforPremium3 = CustomNpcsConfigs.PREMIUM_ITEM_AMOUNT_3;
	
	public PremiumManager()
	{
		super(PremiumManager.class.getSimpleName(), "ai/sunriseNpc");
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
		addStartNpc(NpcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_PREMIUM_MANAGER)
		{
			player.sendMessage("Points manager npc is disabled by admin");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (player.getLevel() <= CustomNpcsConfigs.PREMIUM_REQUIRED_LEVEL)
		{
			player.sendMessage("Your level is too low to use this function, you must be at least " + String.valueOf(CustomNpcsConfigs.PREMIUM_REQUIRED_LEVEL + 1) + " level.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (event.startsWith("premium"))
		{
			if (player.isPremium())
			{
				player.sendMessage("You are already premium. Use .premium for more details!");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (event.equalsIgnoreCase("premium1"))
			{
				if (player.destroyItemByItemId("premium", ItemId, ItemAmountforPremium1, player, true))
				{
					PremiumHandler.addPremiumServices(1, player);
					player.setPremiumService(true);
					player.sendMessage("Cogratulations, you are a premium user!");
				}
				else
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				}
			}
			
			if (event.equalsIgnoreCase("premium2"))
			{
				if (player.destroyItemByItemId("premium", ItemId, ItemAmountforPremium2, player, true))
				{
					PremiumHandler.addPremiumServices(2, player);
					player.setPremiumService(true);
					player.sendMessage("Cogratulations, you are a premium user!");
				}
				else
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				}
			}
			
			if (event.equalsIgnoreCase("premium3"))
			{
				if (player.destroyItemByItemId("premium", ItemId, ItemAmountforPremium3, player, true))
				{
					PremiumHandler.addPremiumServices(3, player);
					player.setPremiumService(true);
					player.sendMessage("Cogratulations, you are a premium user!");
				}
				else
				{
					player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
				}
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
		html.replace("%item_name%", ItemData.getInstance().getTemplate(ItemId).getName());
		html.replace("%item_amount1%", String.valueOf(ItemAmountforPremium1));
		html.replace("%item_amount2%", String.valueOf(ItemAmountforPremium2));
		html.replace("%item_amount3%", String.valueOf(ItemAmountforPremium3));
		
		player.sendPacket(html);
	}
	
	private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
		return packet;
	}
}