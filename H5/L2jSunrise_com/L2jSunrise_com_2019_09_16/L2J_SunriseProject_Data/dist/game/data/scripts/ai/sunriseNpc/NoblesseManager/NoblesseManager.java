package ai.sunriseNpc.NoblesseManager;

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
public class NoblesseManager extends AbstractNpcAI
{
	private final static int NPC = CustomNpcsConfigs.NOBLE_NPC_ID;
	private static final int ItemId = CustomNpcsConfigs.NOBLE_ITEM_ID;
	private static final int ItemAmount = CustomNpcsConfigs.NOBLE_ITEM_AMOUNT;
	private static final String itemName = ItemData.getInstance().getTemplate(ItemId).getName();
	private static final int Level = CustomNpcsConfigs.NOBLE_REQUIRED_LEVEL;
	
	public NoblesseManager()
	{
		super(NoblesseManager.class.getSimpleName(), "ai/sunriseNpc");
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_NOBLE_MANAGER)
		{
			player.sendMessage("Noblesse manager is disabled by admin.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (player.getLevel() < Level)
		{
			player.sendMessage("Your level is too low to use this function, you must be at least " + String.valueOf(CustomNpcsConfigs.NOBLE_REQUIRED_LEVEL + 1) + " level.");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (event.startsWith("noblesse"))
		{
			if (player.isNoble())
			{
				player.sendMessage("You are already noblesse.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.destroyItemByItemId("noblesse", ItemId, ItemAmount, player, true))
			{
				player.addItem("Tiara", 7694, 1, null, true);
				player.setNoble(true);
				player.sendUserInfo(true);
				player.sendMessage("Congratulations! You are now Noblesse!");
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
		html.replace("%itemAmount%", ItemAmount);
		html.replace("%itemName%", itemName);
		html.replace("%minimumLevel%", Level);
		player.sendPacket(html);
	}
	
	private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
		return packet;
	}
}