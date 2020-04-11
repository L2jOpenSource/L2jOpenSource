package ai.sunriseNpc.BetaManager;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.TransformData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2r.gameserver.network.serverpackets.SocialAction;

import gr.sr.aioItem.runnable.TransformFinalizer;
import gr.sr.configsEngine.configs.impl.CustomNpcsConfigs;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class BetaManager extends AbstractNpcAI
{
	private final static int NPC = CustomNpcsConfigs.BETA_NPC_ID;
	
	public BetaManager()
	{
		super(BetaManager.class.getSimpleName(), "ai/sunriseNpc");
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (!CustomNpcsConfigs.ENABLE_BETA_MANAGER)
		{
			player.sendMessage("Beta manager npc is disabled by admin");
			return "main.htm";
		}
		
		String htmltext = event;
		final QuestState st = getQuestState(player, true);
		
		if (event.equalsIgnoreCase("exp-sp"))
		{
			st.addExpAndSp(99999999, 999999999);
			return "character.htm";
		}
		else if (event.equalsIgnoreCase("adena"))
		{
			st.giveItems(57, 999999999);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("ancient-adena"))
		{
			st.giveItems(5575, 999999999);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("knight"))
		{
			st.giveItems(9912, 50000);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("rsc16"))
		{
			st.giveItems(13071, 1);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("bsc16"))
		{
			st.giveItems(13072, 1);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("gsc16"))
		{
			st.giveItems(13073, 1);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("hero"))
		{
			if (!player.isHero())
			{
				player.setHero(true);
				player.broadcastPacket(new SocialAction(player.getObjectId(), 20016)); // Hero Animation
				player.broadcastUserInfo();
				return "character.htm";
			}
			else if (player.isHero())
			{
				player.setHero(false);
				player.broadcastUserInfo();
				return "character.htm";
			}
		}
		else if (event.equalsIgnoreCase("noble-stone"))
		{
			st.giveItems(14052, 10000);
			return "items.htm";
		}
		else if (event.equalsIgnoreCase("clan-reputation"))
		{
			if (player.getClan() != null)
			{
				player.getClan().addReputationScore(10000, true);
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				player.sendMessage("Your clan received 10 000 clan reputation!");
				return "clan.htm";
			}
			player.sendMessage("Sorry, but you donť have clan!");
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("earth"))
		{
			st.giveItems(9816, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("angelic"))
		{
			st.giveItems(9818, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("dragon"))
		{
			st.giveItems(9815, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("memento"))
		{
			st.giveItems(9814, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("tombstone"))
		{
			st.giveItems(8176, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("nucleus"))
		{
			st.giveItems(9817, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("cradle"))
		{
			st.giveItems(8175, 10);
			return "clan.htm";
		}
		else if (event.equalsIgnoreCase("fame"))
		{
			player.setFame(player.getFame() + 10000);
			player.sendUserInfo(true);
			player.sendMessage("You received 10 000 fame points!");
			return "character.htm";
		}
		else if (event.equalsIgnoreCase("antharas"))
		{
			st.giveItems(6656, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("valakas"))
		{
			st.giveItems(6657, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("aq"))
		{
			st.giveItems(6660, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("zaken"))
		{
			st.giveItems(6659, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("freya"))
		{
			st.giveItems(16025, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("baium"))
		{
			st.giveItems(6658, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("beleth"))
		{
			st.giveItems(10314, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("frintezza"))
		{
			st.giveItems(8191, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("core"))
		{
			st.giveItems(6662, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("orfen"))
		{
			st.giveItems(6661, 1);
			return "jews.htm";
		}
		else if (event.equalsIgnoreCase("freya-cloak"))
		{
			st.giveItems(21717, 1);
			return "cloaks.htm";
		}
		else if (event.equalsIgnoreCase("frintezza-cloak"))
		{
			st.giveItems(21718, 1);
			return "cloaks.htm";
		}
		else if (event.equalsIgnoreCase("zaken-cloak"))
		{
			st.giveItems(21716, 1);
			return "cloaks.htm";
		}
		else if (event.equalsIgnoreCase("1"))
		{
			st.getPlayer().getAppearance().setTitleColor(0x009900);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("2"))
		{
			st.getPlayer().getAppearance().setTitleColor(0xff7f00);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("3"))
		{
			st.getPlayer().getAppearance().setTitleColor(0xff00ff);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("4"))
		{
			st.getPlayer().getAppearance().setTitleColor(0x00ffff);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("5"))
		{
			st.getPlayer().getAppearance().setTitleColor(0x0000ff);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("6"))
		{
			st.getPlayer().getAppearance().setTitleColor(0x0099ff);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("7"))
		{
			st.getPlayer().getAppearance().setTitleColor(0x70db93);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("8"))
		{
			st.getPlayer().getAppearance().setTitleColor(0x9f9f9f);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("9"))
		{
			st.getPlayer().getAppearance().setTitleColor(0xffff00);
			player.sendUserInfo(true);
			player.sendMessage("Your title color has been changed!");
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("change_sex"))
		{
			player.getAppearance().setSex(player.getAppearance().getSex() ? false : true);
			player.sendMessage("Your gender has been changed.");
			player.broadcastUserInfo();
			// Transform-untransorm player quickly to force the client to reload the character textures
			TransformData.getInstance().transformPlayer(105, player);
			TransformFinalizer ef = new TransformFinalizer(player);
			player.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, 200));
			return "main.htm";
		}
		else if (event.equalsIgnoreCase("noblesse"))
		{
			if (!player.isNoble())
			{
				player.setNoble(true);
				st.giveItems(7694, 1);
				player.sendMessage("Congratulations! You are now Noblesse!");
				player.broadcastUserInfo();
				return "character.htm";
			}
			player.sendMessage("You already have Noblesse!!");
			return "character.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "main.htm";
	}
}