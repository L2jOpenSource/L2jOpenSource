package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.L2Skill;

public class Q367_ElectrifyingRecharge extends Quest
{
	private static final String qn = "Q367_ElectrifyingRecharge";
	
	// NPCs
	private static final int LORAIN = 30673;
	
	// Item
	private static final int INITIAL_TITAN_LAMP = 5875;
	private static final int TITAN_LAMP_1 = 5876;
	private static final int TITAN_LAMP_2 = 5877;
	private static final int TITAN_LAMP_3 = 5878;
	private static final int FINAL_TITAN_LAMP = 5879;
	private static final int BROKEN_TITAN_LAMP = 5880;
	
	// Mobs
	private static final int CATHEROK = 21035;
	
	public Q367_ElectrifyingRecharge()
	{
		super(367, "Electrifying Recharge!");
		
		setItemsIds(INITIAL_TITAN_LAMP, TITAN_LAMP_1, TITAN_LAMP_2, TITAN_LAMP_3, FINAL_TITAN_LAMP, BROKEN_TITAN_LAMP);
		
		addStartNpc(LORAIN);
		addTalkId(LORAIN);
		
		addAttackId(CATHEROK);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30673-03.htm"))
		{
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
			st.giveItems(INITIAL_TITAN_LAMP, 1);
		}
		else if (event.equalsIgnoreCase("30673-08.htm"))
		{
			st.playSound(QuestState.SOUND_GIVEUP);
			st.exitQuest(true);
		}
		return htmltext;
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
				htmltext = (player.getLevel() < 37) ? "30673-02.htm" : "30673-01.htm";
				break;
			
			case STATE_STARTED:
				final int cond = st.getInt("cond");
				if (cond == 1)
				{
					if (st.hasQuestItems(BROKEN_TITAN_LAMP))
					{
						htmltext = "30673-05.htm";
						st.takeItems(BROKEN_TITAN_LAMP, -1);
						st.giveItems(INITIAL_TITAN_LAMP, 1);
						st.playSound(QuestState.SOUND_ACCEPT);
					}
					else if (st.hasAtLeastOneQuestItem(TITAN_LAMP_1, TITAN_LAMP_2, TITAN_LAMP_3))
						htmltext = "30673-04.htm";
					else
						htmltext = "30673-03.htm";
				}
				else if (cond == 2)
				{
					htmltext = "30673-06.htm";
					st.set("cond", "1");
					st.takeItems(FINAL_TITAN_LAMP, -1);
					st.giveItems(INITIAL_TITAN_LAMP, 1);
					
					// Dye reward.
					final int i0 = Rnd.get(14);
					if (i0 == 0)
						st.rewardItems(4553, 1);
					else if (i0 == 1)
						st.rewardItems(4554, 1);
					else if (i0 == 2)
						st.rewardItems(4555, 1);
					else if (i0 == 3)
						st.rewardItems(4556, 1);
					else if (i0 == 4)
						st.rewardItems(4557, 1);
					else if (i0 == 5)
						st.rewardItems(4558, 1);
					else if (i0 == 6)
						st.rewardItems(4559, 1);
					else if (i0 == 7)
						st.rewardItems(4560, 1);
					else if (i0 == 8)
						st.rewardItems(4561, 1);
					else if (i0 == 9)
						st.rewardItems(4562, 1);
					else if (i0 == 10)
						st.rewardItems(4563, 1);
					else if (i0 == 11)
						st.rewardItems(4564, 1);
					else
						st.rewardItems(4445, 1);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		final Player player = attacker.getActingPlayer();
		
		final QuestState st = getRandomPartyMember(player, npc, "1");
		if (st == null)
			return null;
		
		// TODO Npc should enforce skill use 4072 on the attacker here. The only restrictions are cooldown / MP cost.
		
		if (!st.hasQuestItems(FINAL_TITAN_LAMP))
		{
			final int i0 = Rnd.get(37);
			if (i0 == 0)
			{
				if (st.hasQuestItems(INITIAL_TITAN_LAMP))
				{
					st.takeItems(INITIAL_TITAN_LAMP, -1);
					st.giveItems(TITAN_LAMP_1, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
				else if (st.hasQuestItems(TITAN_LAMP_1))
				{
					st.takeItems(TITAN_LAMP_1, -1);
					st.giveItems(TITAN_LAMP_2, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
				else if (st.hasQuestItems(TITAN_LAMP_2))
				{
					st.takeItems(TITAN_LAMP_2, -1);
					st.giveItems(TITAN_LAMP_3, 1);
					st.playSound(QuestState.SOUND_ITEMGET);
				}
				else if (st.hasQuestItems(TITAN_LAMP_3))
				{
					st.set("cond", "2");
					st.playSound(QuestState.SOUND_MIDDLE);
					st.takeItems(TITAN_LAMP_3, -1);
					st.giveItems(FINAL_TITAN_LAMP, 1);
				}
			}
			else if (i0 == 1 && !st.hasQuestItems(BROKEN_TITAN_LAMP))
			{
				st.takeItems(INITIAL_TITAN_LAMP, -1);
				st.takeItems(TITAN_LAMP_1, -1);
				st.takeItems(TITAN_LAMP_2, -1);
				st.takeItems(TITAN_LAMP_3, -1);
				st.giveItems(BROKEN_TITAN_LAMP, 1);
				st.playSound(QuestState.SOUND_ITEMGET);
			}
		}
		return null;
	}
}