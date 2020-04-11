/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.quests;

import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;

public class Q10294_SevenSignToTheMonastery extends Quest
{
	// NPC
	private static final int Elcadia = 32784;
	private static final int Elcadia_Support = 32787;
	private static final int Odd_Globe = 32815;
	private static final int ErisEvilThoughts = 32792;
	private static final int RelicGuardian = 32803;
	private static final int[] WrongBook =
	{
		32822,
		32823,
		32824,
		32826,
		32827,
		32828,
		32830,
		32831,
		32832,
		32834,
		32835,
		32836
	};
	private static final int GoodBook1 = 32821;
	private static final int GoodBook2 = 32825; // good 2
	private static final int GoodBook3 = 32829; // good 3
	private static final int GoodBook4 = 32833; // good 4
	private static final int SolinaEvilThoughts = 32793;
	private static final int JudeVanEtins = 32797;
	private static final int RelicWatcher = 32804;
	private static final int RelicWatcher1 = 32805; // good 2
	private static final int RelicWatcher2 = 32806; // good 3
	private static final int RelicWatcher3 = 32807; // good 4
	private static final int SOUTH_DEVICE = 32818;
	private static final int EAST_DEVICE = 32817;
	private static final int NORTH_DEVICE = 32819;
	private static final int WEST_DEVICE = 32816;
	
	public Q10294_SevenSignToTheMonastery()
	{
		super(10294, Q10294_SevenSignToTheMonastery.class.getSimpleName(), "Seven Signs, To the Monastery of Silence");
		addTalkId(Odd_Globe);
		addStartNpc(Elcadia);
		addTalkId(Elcadia);
		addTalkId(GoodBook1);
		addTalkId(GoodBook2);
		addTalkId(GoodBook3);
		addTalkId(GoodBook4);
		addTalkId(RelicGuardian);
		addTalkId(ErisEvilThoughts);
		addTalkId(Elcadia_Support);
		addStartNpc(Elcadia_Support);
		addTalkId(SolinaEvilThoughts);
		addTalkId(RelicWatcher);
		addTalkId(RelicWatcher1);
		addTalkId(RelicWatcher2);
		addTalkId(RelicWatcher3);
		addTalkId(JudeVanEtins);
		addTalkId(SOUTH_DEVICE);
		addTalkId(EAST_DEVICE);
		addTalkId(NORTH_DEVICE);
		addTalkId(WEST_DEVICE);
		
		for (int i : WrongBook)
		{
			addTalkId(i);
		}
		
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		if (npc.getId() == Elcadia)
		{
			if (event.equalsIgnoreCase("32784-05.html"))
			{
				st.setState(State.STARTED);
				st.set("cond", "1");
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (npc.getId() == ErisEvilThoughts)
		{
			if (event.equalsIgnoreCase("32792-03.html"))
			{
				if (st.getState() != State.STARTED)
				{
					st.setState(State.STARTED);
				}
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
		}
		
		else if (npc.getId() == GoodBook1)
		{
			if (event.equalsIgnoreCase("good.html"))
			{
				st.set("good1", "1");
				npc.setDisplayEffect(1);
				player.showQuestMovie(25);
				addSpawn(32888, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
				addSpawn(27415, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
				addSpawn(22125, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
				addSpawn(22125, 88655, -250591, -8320, 144, false, 0, false, player.getInstanceId());
			}
		}
		else if (npc.getId() == GoodBook2)
		{
			if (event.equalsIgnoreCase("good.html"))
			{
				st.set("good2", "1");
				npc.setDisplayEffect(1);
				L2Skill skill = SkillData.getInstance().getInfo(6727, 1);
				npc.setTarget(player);
				npc.doCast(skill);
			}
		}
		else if (npc.getId() == GoodBook3)
		{
			if (event.equalsIgnoreCase("good.html"))
			{
				st.set("good3", "1");
				npc.setDisplayEffect(1);
				L2Npc support = addSpawn(JudeVanEtins, 85783, -253471, -8320, 65, false, 0, false, player.getInstanceId());
				L2Skill skill = SkillData.getInstance().getInfo(6729, 1);
				support.setTarget(player);
				support.doCast(skill);
			}
		}
		else if (npc.getId() == GoodBook4)
		{
			if (event.equalsIgnoreCase("good.html"))
			{
				st.set("good4", "1");
				npc.setDisplayEffect(1);
				L2Npc support = addSpawn(SolinaEvilThoughts, 56097, -250576, -6757, 0, false, 0, false, player.getInstanceId());
				L2Skill skill = SkillData.getInstance().getInfo(6729, 1);
				support.setTarget(player);
				support.doCast(skill);
			}
		}
		else if (npc.getId() == RelicWatcher)
		{
			if (event.equalsIgnoreCase("truexit"))
			{
				if (st.getInt("good1") == 1)
				{
					htmltext = "32804-05.html";
				}
				else
				{
					htmltext = "32804-03.html";
				}
			}
		}
		else if (npc.getId() == RelicWatcher1)
		{
			if (event.equalsIgnoreCase("truexit"))
			{
				if (st.getInt("good2") == 1)
				{
					htmltext = "32805-05.html";
				}
				else
				{
					htmltext = "32805-03.html";
				}
			}
		}
		else if (npc.getId() == RelicWatcher2)
		{
			if (event.equalsIgnoreCase("truexit"))
			{
				if (st.getInt("good3") == 1)
				{
					htmltext = "32806-05.html";
				}
				else
				{
					htmltext = "32806-03.html";
				}
			}
		}
		else if (npc.getId() == RelicWatcher3)
		{
			if (event.equalsIgnoreCase("truexit"))
			{
				if (st.getInt("good4") == 1)
				{
					htmltext = "32807-05.html";
				}
				else
				{
					htmltext = "32807-03.html";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		if (npc.getId() == WEST_DEVICE)
		{
			if ((st.getInt("good2") != 1) || (st.getInt("good3") != 1) || (st.getInt("good4") != 1))
			{
				htmltext = "passnotdone.html";
			}
			else
			{
				htmltext = "32816.html";
			}
		}
		else if (npc.getId() == EAST_DEVICE)
		{
			if (st.getInt("good2") == 1)
			{
				htmltext = "passdone.html";
			}
			else
			{
				htmltext = "32817.html";
			}
		}
		else if (npc.getId() == NORTH_DEVICE)
		{
			if (st.getInt("good4") == 1)
			{
				htmltext = "passdone.html";
			}
			else
			{
				htmltext = "32819.html";
			}
		}
		else if (npc.getId() == SOUTH_DEVICE)
		{
			if (st.getInt("good3") == 1)
			{
				htmltext = "passdone.html";
			}
			else
			{
				htmltext = "32818.html";
			}
		}
		else if (npc.getId() == Elcadia)
		{
			if (st.getState() == State.COMPLETED)
			{
				htmltext = "32784-02.html";
			}
			else if (player.getLevel() < 81)
			{
				htmltext = "32784-12.html";
			}
			else if ((player.getQuestState(Q10293_SevenSignsForbiddenBook.class.getSimpleName()) == null) || (player.getQuestState(Q10293_SevenSignsForbiddenBook.class.getSimpleName()).getState() != State.COMPLETED))
			{
				htmltext = "32784-12.html";
			}
			else if (st.getState() == State.CREATED)
			{
				htmltext = "32784-01.html";
			}
			else if (st.getInt("cond") == 1)
			{
				htmltext = "32784-06.html";
			}
		}
		else if (npc.getId() == Elcadia_Support)
		{
			if (!(player.getQuestState(getName()).isCompleted()))
			{
				if (player.isMageClass())
				{
					L2Skill buff1 = SkillData.getInstance().getInfo(6714, 1);
					if (buff1 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff1);
					}
					L2Skill buff2 = SkillData.getInstance().getInfo(6721, 1);
					if (buff2 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff2);
					}
					L2Skill buff3 = SkillData.getInstance().getInfo(6722, 1);
					if (buff3 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff3);
					}
					L2Skill buff4 = SkillData.getInstance().getInfo(6717, 1);
					if (buff4 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff4);
					}
				}
				else
				{
					L2Skill buff1 = SkillData.getInstance().getInfo(6714, 1);
					if (buff1 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff1);
					}
					L2Skill buff2 = SkillData.getInstance().getInfo(6715, 1);
					if (buff2 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff2);
					}
					L2Skill buff3 = SkillData.getInstance().getInfo(6716, 1);
					if (buff3 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff3);
					}
					L2Skill buff4 = SkillData.getInstance().getInfo(6717, 1);
					if (buff4 != null)
					{
						npc.setTarget(player);
						npc.doSimultaneousCast(buff4);
					}
				}
			}
			return null;
		}
		else if (npc.getId() == ErisEvilThoughts)
		{
			if (player.getLevel() < 81)
			{
				htmltext = "32784-12.html";
			}
			else if ((player.getQuestState(Q10293_SevenSignsForbiddenBook.class.getSimpleName()) == null) || (player.getQuestState(Q10293_SevenSignsForbiddenBook.class.getSimpleName()).getState() != State.COMPLETED))
			{
				htmltext = "32784-12.html";
			}
			else if ((st.getInt("cond") < 3) && (player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()).getState() != State.COMPLETED))
			{
				htmltext = "32792-01.html";
			}
			else if ((st.getInt("cond") == 3) && (player.getQuestState(Q10294_SevenSignToTheMonastery.class.getSimpleName()).getState() != State.COMPLETED))
			{
				if (player.isSubClassActive())
				{
					htmltext = "32792-09.html";
				}
				else
				{
					st.unset("cond");
					st.unset("good1");
					st.unset("good2");
					st.unset("good3");
					st.unset("good4");
					st.addExpAndSp(25000000, 2500000);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
					htmltext = "32792-07.html";
				}
			}
			else
			{
				htmltext = "32784-02.html";
			}
		}
		else if (npc.getId() == RelicGuardian)
		{
			if ((st.getInt("cond") == 2) && (st.getInt("good1") == 1) && (st.getInt("good2") == 1) && (st.getInt("good3") == 1) && (st.getInt("good4") == 1))
			{
				st.set("cond", "3");
				st.playSound("ItemSound.quest_middle");
				htmltext = "32803-04.html";
			}
			else if (st.getInt("cond") == 3)
			{
				htmltext = "32803-05.html";
			}
			else
			{
				htmltext = "32803-01.html";
			}
		}
		else if (npc.getId() == Odd_Globe)
		{
			if (st.getInt("cond") < 3)
			{
				htmltext = "32815-01.html";
			}
		}
		else if (npc.getId() == Elcadia_Support)
		{
			switch (st.getInt("cond"))
			{
				case 1:
					htmltext = "32787-01.html";
					break;
				case 2:
					htmltext = "32787-02.html";
					break;
			}
		}
		else if (Util.contains(WrongBook, npc.getId()))
		{
			if (st.getInt("cond") == 2)
			{
				htmltext = "wrong.html";
			}
		}
		else if (npc.getId() == GoodBook1)
		{
			if (st.getInt("good1") == 1)
			{
				htmltext = "already.html";
			}
			else
			{
				htmltext = "32821-01.html";
			}
		}
		else if (npc.getId() == GoodBook2)
		{
			if (st.getInt("good2") == 1)
			{
				htmltext = "already.html";
			}
			else
			{
				htmltext = "32825-01.html";
			}
		}
		else if (npc.getId() == GoodBook3)
		{
			if (st.getInt("good3") == 1)
			{
				htmltext = "already.html";
			}
			else
			{
				htmltext = "32829-01.html";
			}
		}
		else if (npc.getId() == GoodBook4)
		{
			if (st.getInt("good4") == 1)
			{
				htmltext = "already.html";
			}
			else
			{
				htmltext = "32833-01.html";
			}
		}
		else if (npc.getId() == SolinaEvilThoughts)
		{
			htmltext = "32793-01.html";
		}
		else if (npc.getId() == JudeVanEtins)
		{
			htmltext = "32797-01.html";
		}
		else if (npc.getId() == RelicWatcher)
		{
			if (st.getInt("cond") < 3)
			{
				htmltext = "32804-01.html";
			}
		}
		else if (npc.getId() == RelicWatcher1)
		{
			if (st.getInt("cond") < 3)
			{
				htmltext = "32805-01.html";
			}
		}
		else if (npc.getId() == RelicWatcher2)
		{
			if (st.getInt("cond") < 3)
			{
				htmltext = "32806-01.html";
			}
		}
		else if (npc.getId() == RelicWatcher3)
		{
			if (st.getInt("cond") < 3)
			{
				htmltext = "32807-01.html";
			}
		}
		return htmltext;
	}
}
