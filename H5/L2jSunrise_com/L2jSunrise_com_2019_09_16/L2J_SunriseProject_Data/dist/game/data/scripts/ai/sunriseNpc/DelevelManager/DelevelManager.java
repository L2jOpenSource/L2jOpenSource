package ai.sunriseNpc.DelevelManager;

import l2r.gameserver.data.xml.impl.ExperienceData;
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
public class DelevelManager extends AbstractNpcAI
{
	private static final int NPC = CustomNpcsConfigs.DELEVEL_NPC_ID;
	private static final int ITEM_ID = CustomNpcsConfigs.DELEVEL_ITEM_ID;
	private static final int ITEM_COUNT_PER_LEVEL = CustomNpcsConfigs.DELEVEL_ITEM_AMOUNT;
	private static final boolean DYNAMIC_PRICES = CustomNpcsConfigs.DELEVEL_DYNAMIC_PRICE;
	private static final int MINLVL = CustomNpcsConfigs.DELEVEL_REQUIRED_LEVEL;
	
	private int getDelevelPrice(final L2PcInstance player)
	{
		return DYNAMIC_PRICES ? ITEM_COUNT_PER_LEVEL * player.getLevel() : ITEM_COUNT_PER_LEVEL;
	}
	
	public DelevelManager()
	{
		super(DelevelManager.class.getSimpleName(), "ai/sunriseNpc");
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_DELEVEL_MANAGER)
		{
			player.sendMessage("Delevel manager npc is disabled by admin");
			sendMainHtmlWindow(player, npc);
			return "";
		}
		
		if (event.equalsIgnoreCase("level"))
		{
			if (player.getLevel() <= MINLVL)
			{
				player.sendMessage("Your level is too low to use this function, you must be at least " + MINLVL + 1 + " level.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.isInCombat())
			{
				player.sendMessage("Cannot use while in combat.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.getKarma() > 0)
			{
				player.sendMessage("Cannot use while hava karma.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.isEnchanting())
			{
				player.sendMessage("Cannot use while Enchanting.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.isAlikeDead())
			{
				player.sendMessage("Cannot use while Dead or Fake Death.");
				sendMainHtmlWindow(player, npc);
				return "";
			}
			
			if (player.destroyItemByItemId("Delevel", ITEM_ID, getDelevelPrice(player), player, true))
			{
				player.setExp(player.getStat().getExpForLevel(player.getLevel()));
				// sets exp to 0%, if you don't like people abusing this by
				// deleveling at 99% exp, comment the previous line
				player.removeExpAndSp(player.getExp() - ExperienceData.getInstance().getExpForLevel(player.getLevel() - 1), 0);
				player.rewardSkills();
				player.sendMessage("Your level has been decreased.");
			}
			else
			{
				player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
			}
			
			sendMainHtmlWindow(player, npc);
			return "";
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		sendMainHtmlWindow(player, npc);
		return "";
	}
	
	private void sendMainHtmlWindow(L2PcInstance player, L2Npc npc)
	{
		final NpcHtmlMessage html = getHtmlPacket(player, npc, "main.htm");
		html.replace("%MINLVL%", String.valueOf(MINLVL + 1));
		html.replace("%PLAYER%", player.getName());
		html.replace("%DELEVEL_PRICE%", String.valueOf(getDelevelPrice(player)));
		html.replace("%ITEM_NAME%", ItemData.getInstance().getTemplate(ITEM_ID).getName());
		
		player.sendPacket(html);
	}
	
	private NpcHtmlMessage getHtmlPacket(L2PcInstance player, L2Npc npc, String htmlFile)
	{
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player, player.getHtmlPrefix(), htmlFile));
		return packet;
	}
}
