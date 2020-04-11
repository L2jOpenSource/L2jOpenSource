package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q021_HiddenTruth extends Quest
{
	private static final String qn = "Q021_HiddenTruth";
	
	// NPCs
	private static final int MYSTERIOUS_WIZARD = 31522;
	private static final int TOMBSTONE = 31523;
	private static final int VON_HELLMAN_DUKE = 31524;
	private static final int VON_HELLMAN_PAGE = 31525;
	private static final int BROKEN_BOOKSHELF = 31526;
	private static final int AGRIPEL = 31348;
	private static final int DOMINIC = 31350;
	private static final int BENEDICT = 31349;
	private static final int INNOCENTIN = 31328;
	
	// Items
	private static final int CROSS_OF_EINHASAD = 7140;
	private static final int CROSS_OF_EINHASAD_NEXT_QUEST = 7141;
	
	private static final Location[] PAGE_LOCS =
	{
		new Location(51992, -54424, -3160),
		new Location(52328, -53400, -3160),
		new Location(51928, -51656, -3096)
	};
	
	// Sounds
	private static final String SOUND_HORROR = "SkillSound5.horror_02";
	private static final String SOUND_ITEM_DROP = "ItemSound.item_drop_equip_armor_cloth";
	
	private Npc _duke;
	private Npc _page;
	
	public Q021_HiddenTruth()
	{
		super(21, "Hidden Truth");
		
		setItemsIds(CROSS_OF_EINHASAD);
		
		addStartNpc(MYSTERIOUS_WIZARD);
		addTalkId(MYSTERIOUS_WIZARD, TOMBSTONE, VON_HELLMAN_DUKE, VON_HELLMAN_PAGE, BROKEN_BOOKSHELF, AGRIPEL, DOMINIC, BENEDICT, INNOCENTIN);
		addDecayId(VON_HELLMAN_DUKE, VON_HELLMAN_PAGE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31522-02.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		}
		else if (event.equalsIgnoreCase("31523-03.htm"))
		{
			if (_duke == null)
			{
				if (st.getInt("cond") == 1)
					st.set("cond", "2");
				
				_duke = addSpawn(VON_HELLMAN_DUKE, 51432, -54570, -3136, 0, false, 300000, true);
				_duke.broadcastNpcSay("Who awoke me?");
			}
			else
				htmltext = "31523-04.htm";
			
			st.playSound(SOUND_HORROR);
		}
		else if (event.equalsIgnoreCase("31524-06.htm"))
		{
			if (spawnThePage(player))
			{
				st.set("cond", "3");
				st.playSound(QuestState.SOUND_MIDDLE);
			}
			else
				htmltext = "31524-06a.htm";
		}
		else if (event.equalsIgnoreCase("31526-03.htm"))
		{
			st.playSound(SOUND_ITEM_DROP);
		}
		else if (event.equalsIgnoreCase("31526-07.htm"))
		{
			st.set("truth", "1");
		}
		else if (event.equalsIgnoreCase("31526-08.htm"))
		{
			st.set("cond", "5");
			st.unset("end_walk");
			st.unset("truth");
			
			if (_page != null)
				_page.deleteMe();
			
			st.playSound(QuestState.SOUND_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31526-14.htm"))
		{
			st.set("cond", "6");
			st.playSound(QuestState.SOUND_MIDDLE);
			st.giveItems(CROSS_OF_EINHASAD, 1);
		}
		else if (event.equalsIgnoreCase("31328-05.htm"))
		{
			if (st.hasQuestItems(CROSS_OF_EINHASAD))
			{
				st.takeItems(CROSS_OF_EINHASAD, 1);
				st.giveItems(CROSS_OF_EINHASAD_NEXT_QUEST, 1);
				st.playSound(QuestState.SOUND_FINISH);
				st.exitQuest(false);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (name.equalsIgnoreCase("1"))
		{
			npc.getAI().tryTo(IntentionType.MOVE_TO, PAGE_LOCS[0], null);
			npc.broadcastNpcSay("Follow me...");
			
			startQuestTimer("2", npc, player, 5000);
		}
		else if (name.equalsIgnoreCase("2"))
		{
			npc.getAI().tryTo(IntentionType.MOVE_TO, PAGE_LOCS[1], null);
			
			startQuestTimer("3", npc, player, 12000);
		}
		else if (name.equalsIgnoreCase("3"))
		{
			npc.getAI().tryTo(IntentionType.MOVE_TO, PAGE_LOCS[2], null);
			
			startQuestTimer("4", npc, player, 18000);
		}
		else if (name.equalsIgnoreCase("4"))
		{
			QuestState st = player.getQuestState(qn);
			if (st != null)
				st.set("end_walk", "1");
			
			npc.broadcastNpcSay("Please check this bookcase, " + player.getName() + ".");
			
			startQuestTimer("5", npc, player, 47000);
		}
		else if (name.equalsIgnoreCase("5"))
		{
			npc.broadcastNpcSay("I'm confused! Maybe it's time to go back.");
		}
		
		return null;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case STATE_CREATED:
				htmltext = (player.getLevel() < 63) ? "31522-03.htm" : "31522-01.htm";
				break;
			
			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MYSTERIOUS_WIZARD:
						htmltext = "31522-05.htm";
						break;
					
					case TOMBSTONE:
						if (cond == 1 || cond == 2 || cond == 3)
							htmltext = "31523-01.htm";
						break;
					
					case VON_HELLMAN_DUKE:
						if (cond == 2)
							htmltext = "31524-01.htm";
						else if (cond == 3)
						{
							if (st.getInt("end_walk") == 0)
							{
								if (spawnThePage(player))
								{
									st.set("end_walk", "0");
									htmltext = "31524-07.htm";
								}
								else
									htmltext = "31524-07a.htm";
							}
							else
							{
								htmltext = "31524-07b.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 4)
							htmltext = "31524-07b.htm";
						else if (cond > 4)
							htmltext = "31524-07c.htm";
						break;
					
					case VON_HELLMAN_PAGE:
						if (cond == 3)
						{
							if (st.getInt("end_walk") == 1)
							{
								htmltext = "31525-02.htm";
								st.set("cond", "4");
								st.playSound(QuestState.SOUND_MIDDLE);
							}
							else
								htmltext = "31525-01.htm";
						}
						else if (cond == 4)
							htmltext = "31525-02.htm";
						break;
					
					case BROKEN_BOOKSHELF:
						if ((cond == 3 && st.getInt("end_walk") == 1))
							htmltext = "31526-01.htm";
						else if (cond == 4)
						{
							if (st.getInt("truth") == 0)
								htmltext = "31526-01.htm";
							else
							{
								htmltext = "31526-10.htm";
								
								st.set("cond", "5");
								st.unset("end_walk");
								st.unset("truth");
								
								if (_page != null)
									_page.deleteMe();
								
								st.playSound(QuestState.SOUND_MIDDLE);
							}
						}
						else if (cond == 5)
							htmltext = "31526-11.htm";
						else if (cond == 6)
							htmltext = "31526-15.htm";
						break;
					
					case AGRIPEL:
					case BENEDICT:
					case DOMINIC:
						if ((cond == 6 || cond == 7) && st.hasQuestItems(CROSS_OF_EINHASAD))
						{
							int npcId = npc.getNpcId();
							
							// For cond 6, make checks until cond 7 is activated.
							if (cond == 6)
							{
								int npcId1 = 0, npcId2 = 0;
								if (npcId == AGRIPEL)
								{
									npcId1 = BENEDICT;
									npcId2 = DOMINIC;
								}
								else if (npcId == BENEDICT)
								{
									npcId1 = AGRIPEL;
									npcId2 = DOMINIC;
								}
								else if (npcId == DOMINIC)
								{
									npcId1 = AGRIPEL;
									npcId2 = BENEDICT;
								}
								
								if (st.getInt(String.valueOf(npcId1)) == 1 && st.getInt(String.valueOf(npcId2)) == 1)
								{
									st.set("cond", "7");
									st.playSound(QuestState.SOUND_MIDDLE);
								}
								else
									st.set(String.valueOf(npcId), "1");
							}
							
							htmltext = npcId + "-01.htm";
						}
						break;
					
					case INNOCENTIN:
						if (cond == 7 && st.hasQuestItems(CROSS_OF_EINHASAD))
							htmltext = "31328-01.htm";
						break;
				}
				break;
			
			case STATE_COMPLETED:
				if (npc.getNpcId() == INNOCENTIN)
					htmltext = "31328-06.htm";
				else
					htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	@Override
	public String onDecay(Npc npc)
	{
		if (npc == _duke)
		{
			_duke = null;
		}
		else if (npc == _page)
		{
			cancelQuestTimers(_page);
			_page = null;
		}
		
		return null;
	}
	
	private boolean spawnThePage(Player player)
	{
		if (_page == null)
		{
			_page = addSpawn(VON_HELLMAN_PAGE, 51608, -54520, -3168, 0, false, 90000, true);
			_page.broadcastNpcSay("My master has instructed me to be your guide, " + player.getName() + ".");
			
			startQuestTimer("1", _page, player, 4000);
			return true;
		}
		
		return false;
	}
}