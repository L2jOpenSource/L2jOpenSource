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
package quests.Q00255_Tutorial;

import java.util.HashMap;

import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.enums.audio.Voice;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.QuestTimer;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.serverpackets.PlaySound;

import quests.Q00192_SevenSignsSeriesOfDoubt.Q00192_SevenSignsSeriesOfDoubt;

/**
 * @author vGodFather
 */
public class Q00255_Tutorial extends Quest
{
	public final String[][] QTEXMTWO =
	{
		{
			"0",
			"tutorial_voice_001a",
			"tutorial_human_fighter001.htm"
		},
		{
			"10",
			"tutorial_voice_001b",
			"tutorial_human_mage001.htm"
		},
		{
			"18",
			"tutorial_voice_001c",
			"tutorial_elven_fighter001.htm"
		},
		{
			"25",
			"tutorial_voice_001d",
			"tutorial_elven_mage001.htm"
		},
		{
			"31",
			"tutorial_voice_001e",
			"tutorial_delf_fighter001.htm"
		},
		{
			"38",
			"tutorial_voice_001f",
			"tutorial_delf_mage001.htm"
		},
		{
			"44",
			"tutorial_voice_001g",
			"tutorial_orc_fighter001.htm"
		},
		{
			"49",
			"tutorial_voice_001h",
			"tutorial_orc_mage001.htm"
		},
		{
			"53",
			"tutorial_voice_001i",
			"tutorial_dwarven_fighter001.htm"
		},
		{
			"123",
			"tutorial_voice_001k",
			"tutorial_kamael_male001.htm"
		},
		{
			"124",
			"tutorial_voice_001j",
			"tutorial_kamael_female001.htm"
		}
	};
	// table for Client Event Enable (8) [raceId, html, x, y, z]
	public final String[][] CEEa =
	{
		{
			"0",
			"tutorial_human_fighter007.htm",
			"-71424",
			"258336",
			"-3109"
		},
		{
			"10",
			"tutorial_human_mage007.htm",
			"-91036",
			"248044",
			"-3568"
		},
		{
			"18",
			"tutorial_elf007.htm",
			"46112",
			"41200",
			"-3504"
		},
		{
			"25",
			"tutorial_elf007.htm",
			"46112",
			"41200",
			"-3504"
		},
		{
			"31",
			"tutorial_delf007.htm",
			"28384",
			"11056",
			"-4233"
		},
		{
			"38",
			"tutorial_delf007.htm",
			"28384",
			"11056",
			"-4233"
		},
		{
			"44",
			"tutorial_orc007.htm",
			"-56736",
			"-113680",
			"-672"
		},
		{
			"49",
			"tutorial_orc007.htm",
			"-56736",
			"-113680",
			"-672"
		},
		{
			"53",
			"tutorial_dwarven_fighter007.htm",
			"108567",
			"-173994",
			"-406"
		},
		{
			"123",
			"tutorial_kamael007.htm",
			"-125872",
			"38016",
			"1251"
		},
		{
			"124",
			"tutorial_kamael007.htm",
			"-125872",
			"38016",
			"1251"
		}
	};
	// table for Question Mark Clicked (9 & 11) learning skills [raceId, html, x, y, z]
	public final String[][] QMCa =
	{
		{
			"0",
			"tutorial_fighter017.htm",
			"-83165",
			"242711",
			"-3720"
		},
		{
			"10",
			"tutorial_mage017.htm",
			"-85247",
			"244718",
			"-3720"
		},
		{
			"18",
			"tutorial_fighter017.htm",
			"45610",
			"52206",
			"-2792"
		},
		{
			"25",
			"tutorial_mage017.htm",
			"45610",
			"52206",
			"-2792"
		},
		{
			"31",
			"tutorial_fighter017.htm",
			"10344",
			"14445",
			"-4242"
		},
		{
			"38",
			"tutorial_mage017.htm",
			"10344",
			"14445",
			"-4242"
		},
		{
			"44",
			"tutorial_fighter017.htm",
			"-46324",
			"-114384",
			"-200"
		},
		{
			"49",
			"tutorial_fighter017.htm",
			"-46305",
			"-112763",
			"-200"
		},
		{
			"53",
			"tutorial_fighter017.htm",
			"115447",
			"-182672",
			"-1440"
		},
		{
			"123",
			"tutorial_fighter017.htm",
			"-118132",
			"42788",
			"723"
		},
		{
			"124",
			"tutorial_fighter017.htm",
			"-118132",
			"42788",
			"723"
		}
	};
	// table for Question Mark Clicked (24) newbie lvl [raceId, html]
	public final HashMap<Integer, String> QMCb = new HashMap<>();
	// table for Question Mark Clicked (35) 1st class transfer [raceId, html]
	public final HashMap<Integer, String> QMCc = new HashMap<>();
	// table for Tutorial Close Link (26) 2nd class transfer [raceId, html]
	public final HashMap<Integer, String> TCLa = new HashMap<>();
	// table for Tutorial Close Link (23) 2nd class transfer [raceId, html]
	public final HashMap<Integer, String> TCLb = new HashMap<>();
	// table for Tutorial Close Link (24) 2nd class transfer [raceId, html]
	public final HashMap<Integer, String> TCLc = new HashMap<>();
	
	public Q00255_Tutorial()
	{
		super(255, Q00255_Tutorial.class.getSimpleName(), "Tutorial");
		
		QMCb.put(0, "tutorial_human009.htm");
		QMCb.put(10, "tutorial_human009.htm");
		QMCb.put(18, "tutorial_elf009.htm");
		QMCb.put(25, "tutorial_elf009.htm");
		QMCb.put(31, "tutorial_delf009.htm");
		QMCb.put(38, "tutorial_delf009.htm");
		QMCb.put(44, "tutorial_orc009.htm");
		QMCb.put(49, "tutorial_orc009.htm");
		QMCb.put(53, "tutorial_dwarven009.htm");
		QMCb.put(123, "tutorial_kamael009.htm");
		QMCb.put(124, "tutorial_kamael009.htm");
		
		QMCc.put(0, "tutorial_21.htm");
		QMCc.put(10, "tutorial_21a.htm");
		QMCc.put(18, "tutorial_21b.htm");
		QMCc.put(25, "tutorial_21c.htm");
		QMCc.put(31, "tutorial_21g.htm");
		QMCc.put(38, "tutorial_21h.htm");
		QMCc.put(44, "tutorial_21d.htm");
		QMCc.put(49, "tutorial_21e.htm");
		QMCc.put(53, "tutorial_21f.htm");
		
		TCLa.put(1, "tutorial_22w.htm");
		TCLa.put(4, "tutorial_22.htm");
		TCLa.put(7, "tutorial_22b.htm");
		TCLa.put(11, "tutorial_22c.htm");
		TCLa.put(15, "tutorial_22d.htm");
		TCLa.put(19, "tutorial_22e.htm");
		TCLa.put(22, "tutorial_22f.htm");
		TCLa.put(26, "tutorial_22g.htm");
		TCLa.put(29, "tutorial_22h.htm");
		TCLa.put(32, "tutorial_22n.htm");
		TCLa.put(35, "tutorial_22o.htm");
		TCLa.put(39, "tutorial_22p.htm");
		TCLa.put(42, "tutorial_22q.htm");
		TCLa.put(45, "tutorial_22i.htm");
		TCLa.put(47, "tutorial_22j.htm");
		TCLa.put(50, "tutorial_22k.htm");
		TCLa.put(54, "tutorial_22l.htm");
		TCLa.put(56, "tutorial_22m.htm");
		
		TCLb.put(4, "tutorial_22aa.htm");
		TCLb.put(7, "tutorial_22ba.htm");
		TCLb.put(11, "tutorial_22ca.htm");
		TCLb.put(15, "tutorial_22da.htm");
		TCLb.put(19, "tutorial_22ea.htm");
		TCLb.put(22, "tutorial_22fa.htm");
		TCLb.put(26, "tutorial_22ga.htm");
		TCLb.put(32, "tutorial_22na.htm");
		TCLb.put(35, "tutorial_22oa.htm");
		TCLb.put(39, "tutorial_22pa.htm");
		TCLb.put(50, "tutorial_22ka.htm");
		
		TCLc.put(4, "tutorial_22ab.htm");
		TCLc.put(7, "tutorial_22bb.htm");
		TCLc.put(11, "tutorial_22cb.htm");
		TCLc.put(15, "tutorial_22db.htm");
		TCLc.put(19, "tutorial_22eb.htm");
		TCLc.put(22, "tutorial_22fb.htm");
		TCLc.put(26, "tutorial_22gb.htm");
		TCLc.put(32, "tutorial_22nb.htm");
		TCLc.put(35, "tutorial_22ob.htm");
		TCLc.put(39, "tutorial_22pb.htm");
		TCLc.put(50, "tutorial_22kb.htm");
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		QuestState qs = st.getPlayer().getQuestState(Q00192_SevenSignsSeriesOfDoubt.class.getSimpleName());
		
		String htmlText = "";
		int classId = st.getPlayer().getClassId().getId();
		int Ex = st.getInt("Ex");
		
		// User connected
		if (event.startsWith("UC"))
		{
			if (st.getPlayer().getLevel() < 6)
			{
				int uc = st.getInt("ucMemo");
				switch (uc)
				{
					case 0:
						st.set("ucMemo", "0");
						st.startQuestTimer("QT", 10000);
						st.set("ucMemo", "0");
						st.set("Ex", "-2");
						break;
					case 1:
						st.showQuestionMark(1);
						st.playSound(Voice.TUTORIAL_VOICE_006_1000);
						st.playSound(Sound.ITEMSOUND_QUEST_MIDDLE);
						break;
					case 2:
						if (Ex == 2)
						{
							st.showQuestionMark(3);
							st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						}
						
						if (st.getQuestItemsCount(6353) > 0)
						{
							st.showQuestionMark(5);
							st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						}
						break;
					case 3:
						st.showQuestionMark(12);
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.onTutorialClientEvent(0);
						break;
				}
			}
			else if (st.getPlayer().getLevel() == 18)
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.addRadar(-13900, 123822, -3128);
				htmlText = "KamalokaOn18.htm";
			}
			else if (st.getPlayer().getLevel() == 28)
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.addRadar(18199, 146081, -3096);
				htmlText = "KamalokaOn28.htm";
			}
			else if (st.getPlayer().getLevel() == 38)
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.addRadar(108384, 221563, -3608);
				htmlText = "KamalokaOn38.htm";
			}
			else if (st.getPlayer().getLevel() == 48)
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.addRadar(81023, 56456, -1568);
				htmlText = "KamalokaOn48.htm";
			}
			else if (st.getPlayer().getLevel() == 58)
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.addRadar(85865, -142153, -1344);
				htmlText = "KamalokaOn58.htm";
			}
			else if (st.getPlayer().getLevel() == 68)
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.addRadar(42596, -47988, -792);
				htmlText = "KamalokaOn68.htm";
			}
			// Player is level 79 and haven't started/ completed the 7s epic quest yet.
			else if ((st.getPlayer().getLevel() == 79) && ((qs == null) || (qs.getState() == State.CREATED)))
			{
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				// st.addRadar(Ex, Ex, Ex); Need to check coords, not sure
				htmlText = "tutorial_epic_quest.htm";
			}
			else
			{
				return event;
			}
		}
		// Quest timer
		else if (event.startsWith("QT"))
		{
			if (Ex == -2)
			{
				String voice = "";
				for (String[] element : QTEXMTWO)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						voice = element[1];
						htmlText = element[2];
					}
				}
				player.sendPacket(PlaySound.createSound(voice));
				st.set("Ex", "-3");
				QuestTimer timer = st.getQuestTimer("QT");
				if (timer != null)
				{
					timer.cancel();
				}
				st.startQuestTimer("QT", 30000);
			}
			else if (Ex == -3)
			{
				st.playSound(Voice.TUTORIAL_VOICE_002_1000);
				st.set("Ex", "0");
			}
			else if (Ex == -4)
			{
				st.playSound(Voice.TUTORIAL_VOICE_008_1000);
				st.set("Ex", "-5");
			}
		}
		// Tutorial close
		else if (event.startsWith("TE"))
		{
			QuestTimer timer = st.getQuestTimer("TE");
			if (timer != null)
			{
				timer.cancel();
			}
			int event_id = 0;
			if (!event.equalsIgnoreCase("TE"))
			{
				event_id = Integer.valueOf(event.substring(2));
			}
			
			if (event_id == 0)
			{
				st.closeTutorialHtml();
			}
			else if (event_id == 1)
			{
				st.closeTutorialHtml();
				st.playSound(Voice.TUTORIAL_VOICE_006_1000);
				st.showQuestionMark(1);
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.startQuestTimer("QT", 30000);
				st.set("Ex", "-4");
			}
			else if (event_id == 2)
			{
				st.playSound(Voice.TUTORIAL_VOICE_003_2000);
				htmlText = "tutorial_02.htm";
				st.onTutorialClientEvent(1);
				st.set("Ex", "-5");
			}
			else if (event_id == 3)
			{
				htmlText = "tutorial_03.htm";
				st.onTutorialClientEvent(2);
			}
			else if (event_id == 5)
			{
				htmlText = "tutorial_05.htm";
				st.onTutorialClientEvent(8);
			}
			else if (event_id == 7)
			{
				htmlText = "tutorial_100.htm";
				st.onTutorialClientEvent(0);
			}
			else if (event_id == 8)
			{
				htmlText = "tutorial_101.htm";
				st.onTutorialClientEvent(0);
			}
			else if (event_id == 10)
			{
				htmlText = "tutorial_103.htm";
				st.onTutorialClientEvent(0);
			}
			else if (event_id == 12)
			{
				st.closeTutorialHtml();
			}
			else if ((event_id == 23) && TCLb.containsKey(classId))
			{
				htmlText = TCLb.get(classId);
			}
			else if ((event_id == 24) && TCLc.containsKey(classId))
			{
				htmlText = TCLc.get(classId);
			}
			else if (event_id == 25)
			{
				htmlText = "tutorial_22cc.htm";
			}
			else if ((event_id == 26) && TCLa.containsKey(classId))
			{
				htmlText = TCLa.get(classId);
			}
			else if (event_id == 27)
			{
				htmlText = "tutorial_29.htm";
			}
			else if (event_id == 28)
			{
				htmlText = "tutorial_28.htm";
			}
		}
		// Client Event
		else if (event.startsWith("CE"))
		{
			int event_id = Integer.parseInt(event.substring(2));
			if ((event_id == 1) && (st.getPlayer().getLevel() < 6))
			{
				st.playSound(Voice.TUTORIAL_VOICE_004_5000);
				htmlText = "tutorial_03.htm";
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.onTutorialClientEvent(2);
			}
			else if ((event_id == 2) && (st.getPlayer().getLevel() < 6))
			{
				st.playSound(Voice.TUTORIAL_VOICE_005_5000);
				htmlText = "tutorial_05.htm";
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.onTutorialClientEvent(8);
			}
			else if ((event_id == 8) && (st.getPlayer().getLevel() < 6))
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for (String[] element : CEEa)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						htmlText = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				}
				if (x != 0)
				{
					st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					st.addRadar(x, y, z);
					st.playSound(Voice.TUTORIAL_VOICE_007_3500);
					st.set("ucMemo", "1");
					st.set("Ex", "-5");
				}
			}
			else if ((event_id == 30) && (st.getPlayer().getLevel() < 6) && (st.getInt("Die") == 0))
			{
				st.playSound(Voice.TUTORIAL_VOICE_016_1000);
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.set("Die", "1");
				st.showQuestionMark(8);
				st.onTutorialClientEvent(0);
			}
			else if ((event_id == 800000) && (st.getPlayer().getLevel() < 6) && (st.getInt("sit") == 0))
			{
				st.playSound(Voice.TUTORIAL_VOICE_018_1000);
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.set("sit", "1");
				st.onTutorialClientEvent(0);
				htmlText = "tutorial_21z.htm";
			}
			else if (event_id == 40)
			{
				if (st.getPlayer().getLevel() == 5)
				{
					if (((st.getInt("lvl") < 5) && !st.getPlayer().getClassId().isMage()) || (classId == 49))
					{
						st.playSound(Voice.TUTORIAL_VOICE_014_1000);
						st.showQuestionMark(9);
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.set("lvl", "5");
					}
				}
				if (st.getPlayer().getLevel() == 6)
				{
					if ((st.getInt("lvl") < 6) && (st.getPlayer().getClassId().level() == 0))
					{
						st.playSound(Voice.TUTORIAL_VOICE_020_1000);
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.showQuestionMark(24);
						st.set("lvl", "6");
					}
				}
				else if (st.getPlayer().getLevel() == 7)
				{
					if ((st.getInt("lvl") < 7) && st.getPlayer().getClassId().isMage() && (classId != 49) && (st.getPlayer().getClassId().level() == 0))
					{
						st.playSound(Voice.TUTORIAL_VOICE_019_1000);
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.set("lvl", "7");
						st.showQuestionMark(11);
					}
				}
				else if (st.getPlayer().getLevel() == 10)
				{
					if ((st.getInt("lvl") < 10) && (st.getPlayer().getClassId().level() == 0))
					{
						st.playSound(Voice.TUTORIAL_VOICE_030_1000);
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.set("lvl", "10");
						st.showQuestionMark(27);
					}
				}
				else if (st.getPlayer().getLevel() == 15)
				{
					if (st.getInt("lvl") < 15)
					{
						// st.playSound("tutorial_voice_???");
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.set("lvl", "15");
						st.showQuestionMark(17);
					}
				}
				else if (st.getPlayer().getLevel() == 19)
				{
					if ((st.getInt("lvl") < 19) && (st.getPlayer().getRace().ordinal() != 5) && (st.getPlayer().getClassId().level() == 0))
					{
						switch (classId)
						{
							case 0:
							case 10:
							case 18:
							case 25:
							case 31:
							case 38:
							case 44:
							case 49:
							case 52:
								// st.playSound("tutorial_voice_???");
								st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
								st.set("lvl", "19");
								st.showQuestionMark(35);
						}
					}
				}
				else if (st.getPlayer().getLevel() == 28)
				{
					if (st.getInt("lvl") < 28)
					{
						// st.playSound("tutorial_voice_???");
						st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
						st.set("lvl", "28");
						st.showQuestionMark(33);
					}
				}
				else if (st.getPlayer().getLevel() == 35)
				{
					if ((st.getInt("lvl") < 35) && (st.getPlayer().getRace().ordinal() != 5) && (st.getPlayer().getClassId().level() == 1))
					{
						switch (classId)
						{
							case 1:
							case 4:
							case 7:
							case 11:
							case 15:
							case 19:
							case 22:
							case 26:
							case 29:
							case 32:
							case 35:
							case 39:
							case 42:
							case 45:
							case 47:
							case 50:
							case 54:
							case 56:
								// st.playSound("tutorial_voice_???");
								st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
								st.set("lvl", "35");
								st.showQuestionMark(34);
						}
					}
				}
			}
			else if (st.getPlayer().getLevel() == 38)
			{
				if (st.getInt("lvl") < 38)
				{
					st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					st.set("lvl", "38");
					st.showQuestionMark(33);
				}
			}
			else if (st.getPlayer().getLevel() == 48)
			{
				if (st.getInt("lvl") < 48)
				{
					st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					st.set("lvl", "48");
					st.showQuestionMark(33);
				}
			}
			else if (st.getPlayer().getLevel() == 58)
			{
				if (st.getInt("lvl") < 58)
				{
					st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					st.set("lvl", "58");
					st.showQuestionMark(33);
				}
			}
			else if (st.getPlayer().getLevel() == 68)
			{
				if (st.getInt("lvl") < 68)
				{
					st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					st.set("lvl", "68");
					st.showQuestionMark(33);
				}
			}
			else if (st.getPlayer().getLevel() == 79)
			{
				if (st.getInt("lvl") < 79)
				{
					st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
					st.set("lvl", "79");
					st.showQuestionMark(33);
				}
			}
			else if ((event_id == 45) && (st.getPlayer().getLevel() < 10) && (st.getInt("HP") == 0))
			{
				st.playSound(Voice.TUTORIAL_VOICE_017_1000);
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.set("HP", "1");
				st.showQuestionMark(10);
				st.onTutorialClientEvent(800000);
			}
			else if ((event_id == 57) && (st.getPlayer().getLevel() < 10) && (st.getInt("Adena") == 0))
			{
				st.playSound(Voice.TUTORIAL_VOICE_012_1000);
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.set("Adena", "1");
				st.showQuestionMark(23);
			}
			else if ((event_id == 6353) && (st.getPlayer().getLevel() < 6) && (st.getInt("Gemstone") == 0))
			{
				st.playSound(Voice.TUTORIAL_VOICE_013_1000);
				st.playSound(Sound.ITEMSOUND_QUEST_TUTORIAL);
				st.set("Gemstone", "1");
				st.showQuestionMark(5);
			}
		}
		// Question mark clicked
		else if (event.startsWith("QM"))
		{
			int MarkId = Integer.parseInt(event.substring(2));
			if (MarkId == 1)
			{
				st.playSound(Voice.TUTORIAL_VOICE_007_3500);
				st.set("Ex", "-5");
				int x = 0;
				int y = 0;
				int z = 0;
				for (String[] element : CEEa)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						htmlText = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				}
				st.addRadar(x, y, z);
			}
			else if (MarkId == 3)
			{
				htmlText = "tutorial_09.htm";
				st.onTutorialClientEvent(1048576);
			}
			else if (MarkId == 5)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for (String[] element : CEEa)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						htmlText = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				}
				st.addRadar(x, y, z);
				htmlText = "tutorial_11.htm";
			}
			else if (MarkId == 7)
			{
				htmlText = "tutorial_15.htm";
				st.set("ucMemo", "3");
			}
			else if (MarkId == 8)
			{
				htmlText = "tutorial_18.htm";
			}
			else if (MarkId == 9)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for (String[] element : QMCa)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						htmlText = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				}
				if (x != 0)
				{
					st.addRadar(x, y, z);
				}
			}
			else if (MarkId == 10)
			{
				htmlText = "tutorial_19.htm";
			}
			else if (MarkId == 11)
			{
				int x = 0;
				int y = 0;
				int z = 0;
				for (String[] element : QMCa)
				{
					if (classId == Integer.valueOf(element[0]))
					{
						htmlText = element[1];
						x = Integer.valueOf(element[2]);
						y = Integer.valueOf(element[3]);
						z = Integer.valueOf(element[4]);
					}
				}
				if (x != 0)
				{
					st.addRadar(x, y, z);
				}
			}
			else if (MarkId == 12)
			{
				htmlText = "tutorial_15.htm";
				st.set("ucMemo", "4");
			}
			else if (MarkId == 13)
			{
				htmlText = "tutorial_30.htm";
			}
			else if (MarkId == 17)
			{
				htmlText = "tutorial_27.htm";
			}
			else if (MarkId == 23)
			{
				htmlText = "tutorial_24.htm";
			}
			else if ((MarkId == 24) && QMCb.containsKey(classId))
			{
				htmlText = QMCb.get(classId);
			}
			else if (MarkId == 26)
			{
				if (st.getPlayer().getClassId().isMage() && (classId != 49))
				{
					htmlText = "tutorial_newbie004b.htm";
				}
				else
				{
					htmlText = "tutorial_newbie004a.htm";
				}
			}
			else if (MarkId == 27)
			{
				htmlText = "tutorial_20.htm";
			}
			else if (MarkId == 33)
			{
				int lvl = player.getLevel();
				if (lvl == 18)
				{
					htmlText = "tutorial_kama_18.htm";
				}
				else if (lvl == 28)
				{
					htmlText = "tutorial_kama_28.htm";
				}
				else if (lvl == 38)
				{
					htmlText = "tutorial_kama_38.htm";
				}
				else if (lvl == 48)
				{
					htmlText = "tutorial_kama_48.htm";
				}
				else if (lvl == 58)
				{
					htmlText = "tutorial_kama_58.htm";
				}
				else if (lvl == 68)
				{
					htmlText = "tutorial_kama_68.htm";
				}
				else if (lvl == 79)
				{
					htmlText = "tutorial_epic_quest.htm";
				}
			}
			else if (MarkId == 34)
			{
				htmlText = "tutorial_28.htm";
			}
			else if ((MarkId == 35) && QMCc.containsKey(classId))
			{
				htmlText = QMCc.get(classId);
			}
		}
		
		if (htmlText.isEmpty())
		{
			return null;
		}
		st.showTutorialHTML(htmlText);
		return null;
	}
}