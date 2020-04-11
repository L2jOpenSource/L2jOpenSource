package net.sf.l2j.gameserver.scripting.quests;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.SiegableHall;
import net.sf.l2j.gameserver.model.entity.ClanHallSiege;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public final class Q655_AGrandPlanForTamingWildBeasts extends Quest
{
	public static final String qn = "Q655_AGrandPlanForTamingWildBeasts";
	
	// NPCs
	private static final int MESSENGER = 35627;
	
	// Items
	private static final int CRYSTAL_OF_PURITY = 8084;
	private static final int TRAINER_LICENSE = 8293;
	
	// Misc
	private static final int REQUIRED_CRYSTAL_COUNT = 10;
	private static final int REQUIRED_CLAN_LEVEL = 4;
	private static final int MINUTES_TO_SIEGE = 3600;
	
	private static final String PATH_TO_HTML = "data/html/scripts/conquerablehalls/flagwar/WildBeastReserve/messenger_initial.htm";
	
	public Q655_AGrandPlanForTamingWildBeasts()
	{
		super(655, "A Grand Plan for Taming Wild Beasts");
		
		setItemsIds(CRYSTAL_OF_PURITY, TRAINER_LICENSE);
		
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		final long minutesToSiege = getMinutesToSiege();
		
		if (event.equalsIgnoreCase("35627-06.htm"))
		{
			final Clan clan = player.getClan();
			if (clan != null && clan.getLevel() >= REQUIRED_CLAN_LEVEL && clan.getClanHallId() == 0 && player.isClanLeader() && minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
			{
				st.setState(STATE_STARTED);
				st.set("cond", "1");
				st.playSound(QuestState.SOUND_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("35627-11.htm"))
		{
			if (minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
				htmltext = HtmCache.getInstance().getHtm(PATH_TO_HTML);
			else
				htmltext = htmltext.replace("%next_siege%", getSiegeDate());
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		final long minutesToSiege = getMinutesToSiege();
		
		switch (st.getState())
		{
			case STATE_CREATED:
				final Clan clan = player.getClan();
				if (clan == null)
					return htmltext;
				
				if (minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
				{
					if (player.isClanLeader())
					{
						if (clan.getClanHallId() == 0)
							htmltext = (clan.getLevel() >= REQUIRED_CLAN_LEVEL) ? "35627-01.htm" : "35627-03.htm";
						else
							htmltext = "35627-04.htm";
					}
					else
					{
						if (clan.getClanHallId() == ClanHallSiege.BEAST_FARM && minutesToSiege > 0 && minutesToSiege < MINUTES_TO_SIEGE)
							htmltext = HtmCache.getInstance().getHtm(PATH_TO_HTML);
						else
							htmltext = "35627-05.htm";
					}
				}
				else
					htmltext = getHtmlText("35627-02.htm").replace("%next_siege%", getSiegeDate());
				break;
			
			case STATE_STARTED:
				// Time out ; quest aborts.
				if (minutesToSiege < 0 || minutesToSiege > MINUTES_TO_SIEGE)
				{
					htmltext = "35627-07.htm";
					st.exitQuest(true);
				}
				else
				{
					int cond = st.getInt("cond");
					if (cond == 1)
						htmltext = "35627-08.htm";
					else if (cond == 2)
					{
						htmltext = "35627-10.htm";
						st.set("cond", "3");
						st.playSound(QuestState.SOUND_MIDDLE);
						st.takeItems(CRYSTAL_OF_PURITY, -1);
						st.giveItems(TRAINER_LICENSE, 1);
					}
					else if (cond == 3)
						htmltext = "35627-09.htm";
				}
				break;
		}
		return htmltext;
	}
	
	/**
	 * @return the next siege formatted date.
	 */
	private static String getSiegeDate()
	{
		final SiegableHall hall = ClanHallManager.getInstance().getSiegableHall(ClanHallSiege.BEAST_FARM);
		if (hall != null)
		{
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.format(hall.getSiegeDate());
		}
		return "Error in date.";
	}
	
	/**
	 * @return the amount of minutes before the next siege.
	 */
	private static long getMinutesToSiege()
	{
		final SiegableHall hall = ClanHallManager.getInstance().getSiegableHall(ClanHallSiege.BEAST_FARM);
		if (hall != null)
			return (hall.getNextSiegeTime() - Calendar.getInstance().getTimeInMillis()) / 3600;
		
		return -1;
	}
	
	/**
	 * Rewards the {@link Player}'s clan leader with a Crystal of Purity after Player tame a wild beast.<br>
	 * <br>
	 * If 10 crystals are gathered, trigger cond 2 for the clan leader.
	 * @param player : The player used as reference.
	 * @param npc : The npc used as reference.
	 */
	public void reward(Player player, Npc npc)
	{
		final QuestState leaderQs = checkClanLeaderCondition(player, npc, "cond", "1");
		if (leaderQs == null)
			return;
		
		if (leaderQs.dropItemsAlways(CRYSTAL_OF_PURITY, 1, REQUIRED_CRYSTAL_COUNT))
			leaderQs.set("cond", "2");
	}
}