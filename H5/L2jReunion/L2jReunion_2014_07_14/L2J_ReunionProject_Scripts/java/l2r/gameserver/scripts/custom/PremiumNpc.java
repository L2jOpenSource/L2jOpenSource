package l2r.gameserver.scripts.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import gr.reunion.configsEngine.PremiumServiceConfigs;

/**
 * @author -=DoctorNo=-
 */
public class PremiumNpc extends Quest
{
	private final int NpcId = 542; // npc id here
	private final int ConsumableItemId;
	private static String qn = "PremiumNpc";
	private static final String UPDATE_PREMIUMSERVICE = "UPDATE characters_premium SET premium_service=?,enddate=? WHERE account_name=?";
	
	public PremiumNpc()
	{
		super(-1, "PremiumNpc", "custom");
		
		ConsumableItemId = PremiumServiceConfigs.PREMIUM_COIN;
		addStartNpc(NpcId);
		addFirstTalkId(NpcId);
		addTalkId(NpcId);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		htmltext = event;
		if (event.equalsIgnoreCase("getPremium"))
		{
			htmltext = "getpremium.htm";
			return htmltext;
		}
		if (event.equalsIgnoreCase("back"))
		{
			htmltext = "start.htm";
			return htmltext;
		}
		if (event.equalsIgnoreCase("info"))
		{
			htmltext = "benefits.htm";
			return htmltext;
		}
		if (event.equalsIgnoreCase("premium1"))
		{
			if (player.isPremium())
			{
				htmltext = "alreadypremium.htm";
				return htmltext;
			}
			if (st.getQuestItemsCount(ConsumableItemId) >= 10)
			{
				st.takeItems(ConsumableItemId, 10);
				player.setPremiumService(true);
				addPremiumServices(1, player);
				htmltext = "congratulations1.htm";
				return htmltext;
			}
			player.sendMessage("Sorry, but you don't have enough funds to purchease Premium Account.");
		}
		if (event.equalsIgnoreCase("premium2"))
		{
			if (player.isPremium())
			{
				htmltext = "alreadypremium.htm";
				return htmltext;
			}
			if (st.getQuestItemsCount(ConsumableItemId) >= 17)
			{
				st.takeItems(ConsumableItemId, 17);
				player.setPremiumService(true);
				addPremiumServices(2, player);
				htmltext = "congratulations2.htm";
				return htmltext;
			}
			player.sendMessage("Sorry, but you don't have enough funds to purchease Premium Account.");
		}
		if (event.equalsIgnoreCase("premium3"))
		{
			if (player.isPremium())
			{
				htmltext = "alreadypremium.htm";
				return htmltext;
			}
			if (st.getQuestItemsCount(ConsumableItemId) >= 25)
			{
				st.takeItems(ConsumableItemId, 25);
				player.setPremiumService(true);
				addPremiumServices(3, player);
				htmltext = "congratulations3.htm";
				return htmltext;
			}
			player.sendMessage("Sorry, but you don't have enough funds to purchease Premium Account.");
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getQuestState(qn) == null)
		{
			newQuestState(player);
		}
		
		return "start.htm";
	}
	
	private void addPremiumServices(int Months, L2PcInstance player)
	{
		Calendar finishtime = Calendar.getInstance();
		finishtime.setTimeInMillis(System.currentTimeMillis());
		finishtime.set(13, 0);
		finishtime.add(2, Months);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			PreparedStatement statement = con.prepareStatement(UPDATE_PREMIUMSERVICE);
			statement.setInt(1, 1);
			statement.setLong(2, finishtime.getTimeInMillis());
			statement.setString(3, player.getAccountName());
			statement.execute();
		}
		catch (SQLException e)
		{
			_log.info("PremiumService:  Could not increase data.");
		}
	}
	
	public static void main(String args[])
	{
		new PremiumNpc();
	}
}