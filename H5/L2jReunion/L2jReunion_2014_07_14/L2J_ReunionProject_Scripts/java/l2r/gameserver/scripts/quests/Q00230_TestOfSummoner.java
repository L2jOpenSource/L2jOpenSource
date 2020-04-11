package l2r.gameserver.scripts.quests;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.util.Rnd;

public class Q00230_TestOfSummoner extends Quest
{
	private static final String qn = "Q00230_TestOfSummoner";
	
	static int MARK_OF_SUMMONER_ID = 3336;
	static int LETOLIZARDMAN_AMULET_ID = 3337;
	static int SAC_OF_REDSPORES_ID = 3338;
	static int KARULBUGBEAR_TOTEM_ID = 3339;
	static int SHARDS_OF_MANASHEN_ID = 3340;
	static int BREKAORC_TOTEM_ID = 3341;
	static int CRIMSON_BLOODSTONE_ID = 3342;
	static int TALONS_OF_TYRANT_ID = 3343;
	static int WINGS_OF_DRONEANT_ID = 3344;
	static int TUSK_OF_WINDSUS_ID = 3345;
	static int FANGS_OF_WYRM_ID = 3346;
	static int LARS_LIST1_ID = 3347;
	static int LARS_LIST2_ID = 3348;
	static int LARS_LIST3_ID = 3349;
	static int LARS_LIST4_ID = 3350;
	static int LARS_LIST5_ID = 3351;
	static int GALATEAS_LETTER_ID = 3352;
	static int BEGINNERS_ARCANA_ID = 3353;
	static int ALMORS_ARCANA_ID = 3354;
	static int CAMONIELL_ARCANA_ID = 3355;
	static int BELTHUS_ARCANA_ID = 3356;
	static int BASILLIA_ARCANA_ID = 3357;
	static int CELESTIEL_ARCANA_ID = 3358;
	static int BRYNTHEA_ARCANA_ID = 3359;
	static int CRYSTAL_OF_PROGRESS1_ID = 3360;
	static int CRYSTAL_OF_INPROGRESS1_ID = 3361;
	static int CRYSTAL_OF_FOUL1_ID = 3362;
	static int CRYSTAL_OF_DEFEAT1_ID = 3363;
	static int CRYSTAL_OF_VICTORY1_ID = 3364;
	static int CRYSTAL_OF_PROGRESS2_ID = 3365;
	static int CRYSTAL_OF_INPROGRESS2_ID = 3366;
	static int CRYSTAL_OF_FOUL2_ID = 3367;
	static int CRYSTAL_OF_DEFEAT2_ID = 3368;
	static int CRYSTAL_OF_VICTORY2_ID = 3369;
	static int CRYSTAL_OF_PROGRESS3_ID = 3370;
	static int CRYSTAL_OF_INPROGRESS3_ID = 3371;
	static int CRYSTAL_OF_FOUL3_ID = 3372;
	static int CRYSTAL_OF_DEFEAT3_ID = 3373;
	static int CRYSTAL_OF_VICTORY3_ID = 3374;
	static int CRYSTAL_OF_PROGRESS4_ID = 3375;
	static int CRYSTAL_OF_INPROGRESS4_ID = 3376;
	static int CRYSTAL_OF_FOUL4_ID = 3377;
	static int CRYSTAL_OF_DEFEAT4_ID = 3378;
	static int CRYSTAL_OF_VICTORY4_ID = 3379;
	static int CRYSTAL_OF_PROGRESS5_ID = 3380;
	static int CRYSTAL_OF_INPROGRESS5_ID = 3381;
	static int CRYSTAL_OF_FOUL5_ID = 3382;
	static int CRYSTAL_OF_DEFEAT5_ID = 3383;
	static int CRYSTAL_OF_VICTORY5_ID = 3384;
	static int CRYSTAL_OF_PROGRESS6_ID = 3385;
	static int CRYSTAL_OF_INPROGRESS6_ID = 3386;
	static int CRYSTAL_OF_FOUL6_ID = 3387;
	static int CRYSTAL_OF_DEFEAT6_ID = 3388;
	static int CRYSTAL_OF_VICTORY6_ID = 3389;
	
	static int[] npc =
	{
		30063,
		30634,
		30635,
		30636,
		30637,
		30638,
		30639,
		30640
	};
	
	static int Lara = npc[0];
	static int Galatea = npc[1];
	static int Almors = npc[2];
	static int Camoniell = npc[3];
	static int Belthus = npc[4];
	static int Basilla = npc[5];
	static int Celestiel = npc[6];
	static int Brynthea = npc[7];
	
	static int[][] SUMMONERS =
	{
		{
			30635,
			ALMORS_ARCANA_ID,
			CRYSTAL_OF_VICTORY1_ID
		},
		{
			30636,
			CAMONIELL_ARCANA_ID,
			CRYSTAL_OF_VICTORY2_ID
		},
		{
			30637,
			BELTHUS_ARCANA_ID,
			CRYSTAL_OF_VICTORY3_ID
		},
		{
			30638,
			BASILLIA_ARCANA_ID,
			CRYSTAL_OF_VICTORY4_ID
		},
		{
			30639,
			CELESTIEL_ARCANA_ID,
			CRYSTAL_OF_VICTORY5_ID
		},
		{
			30640,
			BRYNTHEA_ARCANA_ID,
			CRYSTAL_OF_VICTORY6_ID
		}
	};
	
	static Map<Integer, String> NAMES = new HashMap<>();
	
	static
	{
		NAMES.put(30635, "Almors");
		NAMES.put(30636, "Camoniell");
		NAMES.put(30637, "Belthus");
		NAMES.put(30638, "Basilla");
		NAMES.put(30639, "Celestiel");
		NAMES.put(30640, "Brynthea");
	}
	
	static Map<Integer, Integer[]> DROPLIST_LARA = new HashMap<>();
	
	static
	{
		DROPLIST_LARA.put(20555, new Integer[]
		{
			1,
			80,
			SAC_OF_REDSPORES_ID
		});
		DROPLIST_LARA.put(20557, new Integer[]
		{
			1,
			25,
			LETOLIZARDMAN_AMULET_ID
		});
		DROPLIST_LARA.put(20558, new Integer[]
		{
			1,
			25,
			LETOLIZARDMAN_AMULET_ID
		});
		DROPLIST_LARA.put(20559, new Integer[]
		{
			1,
			25,
			LETOLIZARDMAN_AMULET_ID
		});
		DROPLIST_LARA.put(20580, new Integer[]
		{
			1,
			50,
			LETOLIZARDMAN_AMULET_ID
		});
		DROPLIST_LARA.put(20581, new Integer[]
		{
			1,
			75,
			LETOLIZARDMAN_AMULET_ID
		});
		DROPLIST_LARA.put(20582, new Integer[]
		{
			1,
			75,
			LETOLIZARDMAN_AMULET_ID
		});
		DROPLIST_LARA.put(20600, new Integer[]
		{
			2,
			80,
			KARULBUGBEAR_TOTEM_ID
		});
		DROPLIST_LARA.put(20563, new Integer[]
		{
			2,
			80,
			SHARDS_OF_MANASHEN_ID
		});
		DROPLIST_LARA.put(20552, new Integer[]
		{
			3,
			60,
			CRIMSON_BLOODSTONE_ID
		});
		DROPLIST_LARA.put(20267, new Integer[]
		{
			3,
			25,
			BREKAORC_TOTEM_ID
		});
		DROPLIST_LARA.put(20268, new Integer[]
		{
			3,
			25,
			BREKAORC_TOTEM_ID
		});
		DROPLIST_LARA.put(20271, new Integer[]
		{
			3,
			25,
			BREKAORC_TOTEM_ID
		});
		DROPLIST_LARA.put(20269, new Integer[]
		{
			3,
			50,
			BREKAORC_TOTEM_ID
		});
		DROPLIST_LARA.put(20270, new Integer[]
		{
			3,
			50,
			BREKAORC_TOTEM_ID
		});
		DROPLIST_LARA.put(20553, new Integer[]
		{
			4,
			70,
			TUSK_OF_WINDSUS_ID
		});
		DROPLIST_LARA.put(20192, new Integer[]
		{
			4,
			50,
			TALONS_OF_TYRANT_ID
		});
		DROPLIST_LARA.put(20193, new Integer[]
		{
			4,
			50,
			TALONS_OF_TYRANT_ID
		});
		DROPLIST_LARA.put(20089, new Integer[]
		{
			5,
			30,
			WINGS_OF_DRONEANT_ID
		});
		DROPLIST_LARA.put(20090, new Integer[]
		{
			5,
			60,
			WINGS_OF_DRONEANT_ID
		});
		DROPLIST_LARA.put(20176, new Integer[]
		{
			5,
			50,
			FANGS_OF_WYRM_ID
		});
	}
	
	static String[] STATS =
	{
		"cond",
		"step",
		"Lara_Part",
		"Arcanas",
		"Belthus",
		"Brynthea",
		"Celestiel",
		"Camoniell",
		"Basilla",
		"Almors"
	};
	
	static int[][] LISTS =
	{
		{},
		{
			LARS_LIST1_ID,
			SAC_OF_REDSPORES_ID,
			LETOLIZARDMAN_AMULET_ID
		},
		{
			LARS_LIST2_ID,
			KARULBUGBEAR_TOTEM_ID,
			SHARDS_OF_MANASHEN_ID
		},
		{
			LARS_LIST3_ID,
			CRIMSON_BLOODSTONE_ID,
			BREKAORC_TOTEM_ID
		},
		{
			LARS_LIST4_ID,
			TUSK_OF_WINDSUS_ID,
			TALONS_OF_TYRANT_ID
		},
		{
			LARS_LIST5_ID,
			WINGS_OF_DRONEANT_ID,
			FANGS_OF_WYRM_ID
		}
	};
	
	public Q00230_TestOfSummoner()
	{
		super(230, qn, "");
		addStartNpc(Galatea);
		for (int npcId : npc)
		{
			addTalkId(npcId);
		}
		
		for (int mobId : DROPLIST_LARA.keySet())
		{
			addKillId(mobId);
		}
		for (int mobId : DROPLIST_SUMMON.keySet())
		{
			addKillId(mobId);
			addAttackId(mobId);
		}
		
		for (int i = 3337; i <= 3389; i++)
		{
			registerQuestItems(i);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		if (event.equalsIgnoreCase("30634-08.htm"))
		{
			for (String var : STATS)
			{
				if (var.equalsIgnoreCase("Arcanas") || var.equalsIgnoreCase("Lara_Part"))
				{
					continue;
				}
				st.set(var, "1");
			}
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30634-07.htm"))
		{
			st.giveItems(GALATEAS_LETTER_ID, 1);
		}
		else if (event.equalsIgnoreCase("30063-02.htm"))
		{
			int random = Rnd.get(5) + 1;
			st.giveItems(LISTS[random][0], 1);
			st.takeItems(GALATEAS_LETTER_ID, 1);
			st.set("Lara_Part", String.valueOf(random));
			st.set("step", "2");
		}
		else if (event.equalsIgnoreCase("30063-04.htm"))
		{
			int random = Rnd.get(5) + 1;
			st.giveItems(LISTS[random][0], 1);
			st.set("Lara_Part", String.valueOf(random));
		}
		else if (event.equalsIgnoreCase("30635-02.htm"))
		{
			if (st.getQuestItemsCount(BEGINNERS_ARCANA_ID) > 0)
			{
				htmltext = "30635-03.htm";
				st.set("Almors", "2");
			}
		}
		else if (event.equalsIgnoreCase("30635-04.htm"))
		{
			st.giveItems(CRYSTAL_OF_PROGRESS1_ID, 1);
			st.takeItems(CRYSTAL_OF_FOUL1_ID, -1);
			st.takeItems(CRYSTAL_OF_DEFEAT1_ID, -1);
			st.takeItems(BEGINNERS_ARCANA_ID, 1);
		}
		else if (event.equalsIgnoreCase("30636-02.htm"))
		{
			if (st.getQuestItemsCount(BEGINNERS_ARCANA_ID) > 0)
			{
				htmltext = "30636-03.htm";
				st.set("Camoniell", "2");
			}
		}
		else if (event.equalsIgnoreCase("30636-04.htm"))
		{
			st.giveItems(CRYSTAL_OF_PROGRESS2_ID, 1);
			st.takeItems(CRYSTAL_OF_FOUL2_ID, -1);
			st.takeItems(CRYSTAL_OF_DEFEAT2_ID, -1);
			st.takeItems(BEGINNERS_ARCANA_ID, 1);
		}
		else if (event.equalsIgnoreCase("30637-02.htm"))
		{
			if (st.getQuestItemsCount(BEGINNERS_ARCANA_ID) > 0)
			{
				htmltext = "30637-03.htm";
				st.set("Belthus", "2");
			}
		}
		else if (event.equalsIgnoreCase("30637-04.htm"))
		{
			st.giveItems(CRYSTAL_OF_PROGRESS3_ID, 1);
			st.takeItems(CRYSTAL_OF_FOUL3_ID, -1);
			st.takeItems(CRYSTAL_OF_DEFEAT3_ID, -1);
			st.takeItems(BEGINNERS_ARCANA_ID, 1);
		}
		else if (event.equalsIgnoreCase("30638-02.htm"))
		{
			if (st.getQuestItemsCount(BEGINNERS_ARCANA_ID) > 0)
			{
				htmltext = "30638-03.htm";
				st.set("Basilla", "2");
			}
		}
		else if (event.equalsIgnoreCase("30638-04.htm"))
		{
			st.giveItems(CRYSTAL_OF_PROGRESS4_ID, 1);
			st.takeItems(CRYSTAL_OF_FOUL4_ID, -1);
			st.takeItems(CRYSTAL_OF_DEFEAT4_ID, -1);
			st.takeItems(BEGINNERS_ARCANA_ID, 1);
		}
		else if (event.equalsIgnoreCase("30639-02.htm"))
		{
			if (st.getQuestItemsCount(BEGINNERS_ARCANA_ID) > 0)
			{
				htmltext = "30639-03.htm";
				st.set("Celestiel", "2");
			}
		}
		else if (event.equalsIgnoreCase("30639-04.htm"))
		{
			st.giveItems(CRYSTAL_OF_PROGRESS5_ID, 1);
			st.takeItems(CRYSTAL_OF_FOUL5_ID, -1);
			st.takeItems(CRYSTAL_OF_DEFEAT5_ID, -1);
			st.takeItems(BEGINNERS_ARCANA_ID, 1);
		}
		else if (event.equalsIgnoreCase("30640-02.htm"))
		{
			if (st.getQuestItemsCount(BEGINNERS_ARCANA_ID) > 0)
			{
				htmltext = "30640-03.htm";
				st.set("Brynthea", "2");
			}
		}
		else if (event.equalsIgnoreCase("30640-04.htm"))
		{
			st.giveItems(CRYSTAL_OF_PROGRESS6_ID, 1);
			st.takeItems(CRYSTAL_OF_FOUL6_ID, -1);
			st.takeItems(CRYSTAL_OF_DEFEAT6_ID, -1);
			st.takeItems(BEGINNERS_ARCANA_ID, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return htmltext;
		}
		
		int npcId = npc.getId();
		
		switch (st.getState())
		{
			case State.CREATED:
				if (npcId == Galatea)
				{
					for (String var : STATS)
					{
						st.set(var, "0");
					}
					if ((player.getClassId().getId() == 0x0b) || (player.getClassId().getId() == 0x1a) || (player.getClassId().getId() == 0x27))
					{
						if (st.getPlayer().getLevel() > 38)
						{
							htmltext = "30634-03.htm";
						}
						else
						{
							htmltext = "30634-02.htm";
							st.exitQuest(true);
						}
					}
					else
					{
						htmltext = "30634-01.htm";
						st.exitQuest(true);
					}
				}
				break;
			case State.STARTED:
				int LaraPart = st.getInt("Lara_Part");
				int Arcanas = st.getInt("Arcanas");
				int step = st.getInt("step");
				if (npcId == 30634)
				{
					if (step == 1)
					{
						htmltext = "30634-09.htm";
					}
					else if (step == 2)
					{
						if (Arcanas == 6)
						{
							htmltext = "30634-12.htm";
							st.takeItems(LARS_LIST1_ID, -1);
							st.takeItems(LARS_LIST2_ID, -1);
							st.takeItems(LARS_LIST3_ID, -1);
							st.takeItems(LARS_LIST4_ID, -1);
							st.takeItems(LARS_LIST5_ID, -1);
							st.takeItems(ALMORS_ARCANA_ID, -1);
							st.takeItems(BASILLIA_ARCANA_ID, -1);
							st.takeItems(CAMONIELL_ARCANA_ID, -1);
							st.takeItems(CELESTIEL_ARCANA_ID, -1);
							st.takeItems(BELTHUS_ARCANA_ID, -1);
							st.takeItems(BRYNTHEA_ARCANA_ID, -1);
							st.giveItems(MARK_OF_SUMMONER_ID, 1);
							st.addExpAndSp(1664494, 114220);
							st.giveItems(57, 300960);
							st.giveItems(7562, 122);
							st.giveItems(8870, 15);
							st.playSound("ItemSound.quest_finish");
							st.exitQuest(false);
						}
					}
					else
					{
						htmltext = "30634-10.htm";
					}
				}
				else if (npcId == Lara)
				{
					if (step == 1)
					{
						htmltext = "30063-01.htm";
					}
					else if (LaraPart == 0)
					{
						htmltext = "30063-03.htm";
					}
					else
					{
						long ItemCount1 = st.getQuestItemsCount(LISTS[LaraPart][1]);
						long ItemCount2 = st.getQuestItemsCount(LISTS[LaraPart][2]);
						if ((ItemCount1 < 30) || (ItemCount2 < 30))
						{
							htmltext = "30063-05.htm";
						}
						else if ((ItemCount1 > 29) && (ItemCount2 > 29))
						{
							htmltext = "30063-06.htm";
							st.giveItems(BEGINNERS_ARCANA_ID, 2);
							st.takeItems(LISTS[LaraPart][0], 1);
							st.takeItems(LISTS[LaraPart][1], -1);
							st.takeItems(LISTS[LaraPart][2], -1);
							st.set("Lara_Part", "0");
						}
					}
				}
				else
				{
					for (int[] i : SUMMONERS)
					{
						if (i[0] == npcId)
						{
							Integer[] k = DROPLIST_SUMMON.get((npcId - 30635) + 27102);
							int SummonerStat = st.getInt(NAMES.get(i[0]));
							if (step > 1)
							{
								if (st.getQuestItemsCount(k[0]) > 0)
								{
									htmltext = String.valueOf(npcId) + "-08.htm";
								}
								else if (st.getQuestItemsCount(k[1]) > 0)
								{
									st.addNotifyOfDeath(player);
									htmltext = String.valueOf(npcId) + "-09.htm";
								}
								else if (st.getQuestItemsCount(k[3]) > 0)
								{
									htmltext = String.valueOf(npcId) + "-05.htm";
								}
								else if (st.getQuestItemsCount(k[2]) > 0)
								{
									htmltext = String.valueOf(npcId) + "-06.htm";
								}
								else if (st.getQuestItemsCount(k[4]) > 0)
								{
									htmltext = String.valueOf(npcId) + "-07.htm";
									st.takeItems(SUMMONERS[npcId - 30635][2], -1);
									st.giveItems(SUMMONERS[npcId - 30635][1], 1);
									st.set(NAMES.get(i[0]), "7");
									st.set("Arcanas", String.valueOf(Arcanas + 1));
								}
								else if (SummonerStat == 7)
								{
									htmltext = String.valueOf(npcId) + "-10.htm";
								}
								else
								{
									htmltext = String.valueOf(npcId) + "-01.htm";
								}
							}
						}
					}
				}
				break;
			case State.COMPLETED:
				if (npcId == Galatea)
				{
					htmltext = getAlreadyCompletedMsg(player);
				}
				break;
		}
		return htmltext;
	}
	
	static Map<Integer, Integer[]> DROPLIST_SUMMON = new HashMap<>();
	
	static
	{
		DROPLIST_SUMMON.put(27102, new Integer[]
		{
			CRYSTAL_OF_PROGRESS1_ID,
			CRYSTAL_OF_INPROGRESS1_ID,
			CRYSTAL_OF_FOUL1_ID,
			CRYSTAL_OF_DEFEAT1_ID,
			CRYSTAL_OF_VICTORY1_ID
		});
		DROPLIST_SUMMON.put(27103, new Integer[]
		{
			CRYSTAL_OF_PROGRESS2_ID,
			CRYSTAL_OF_INPROGRESS2_ID,
			CRYSTAL_OF_FOUL2_ID,
			CRYSTAL_OF_DEFEAT2_ID,
			CRYSTAL_OF_VICTORY2_ID
		});
		DROPLIST_SUMMON.put(27104, new Integer[]
		{
			CRYSTAL_OF_PROGRESS3_ID,
			CRYSTAL_OF_INPROGRESS3_ID,
			CRYSTAL_OF_FOUL3_ID,
			CRYSTAL_OF_DEFEAT3_ID,
			CRYSTAL_OF_VICTORY3_ID
		});
		DROPLIST_SUMMON.put(27105, new Integer[]
		{
			CRYSTAL_OF_PROGRESS4_ID,
			CRYSTAL_OF_INPROGRESS4_ID,
			CRYSTAL_OF_FOUL4_ID,
			CRYSTAL_OF_DEFEAT4_ID,
			CRYSTAL_OF_VICTORY4_ID
		});
		DROPLIST_SUMMON.put(27106, new Integer[]
		{
			CRYSTAL_OF_PROGRESS5_ID,
			CRYSTAL_OF_INPROGRESS5_ID,
			CRYSTAL_OF_FOUL5_ID,
			CRYSTAL_OF_DEFEAT5_ID,
			CRYSTAL_OF_VICTORY5_ID
		});
		DROPLIST_SUMMON.put(27107, new Integer[]
		{
			CRYSTAL_OF_PROGRESS6_ID,
			CRYSTAL_OF_INPROGRESS6_ID,
			CRYSTAL_OF_FOUL6_ID,
			CRYSTAL_OF_DEFEAT6_ID,
			CRYSTAL_OF_VICTORY6_ID
		});
	}
	
	static Map<Integer, String> DROPLIST_SUMMON_VARS = new HashMap<>();
	
	static
	{
		NAMES.put(27102, "Almors");
		NAMES.put(27103, "Camoniell");
		NAMES.put(27104, "Belthus");
		NAMES.put(27105, "Basilla");
		NAMES.put(27106, "Celestiel");
		NAMES.put(27107, "Brynthea");
	}
	
	@Override
	public String onDeath(L2Character killer, L2Character victim, QuestState st)
	{
		if (!(killer.isPlayer()) && !(victim.isPlayer()))
		{
			return "";
		}
		
		int npcId = ((L2Npc) killer).getId();
		
		if ((victim == killer) || (victim == killer.getSummon()))
		{
			if ((npcId >= 27102) && (npcId <= 27107))
			{
				String[] VARS =
				{
					"Almors",
					"Camoniell",
					"Belthus",
					"Basilla",
					"Celestiel",
					"Brynthea"
				};
				String var = VARS[npcId - 27102];
				Integer[] i = DROPLIST_SUMMON.get(npcId);
				int defeat = i[3];
				if (st.getInt(var) == 3)
				{
					st.set(var, "4");
					st.giveItems(defeat, 1);
				}
			}
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		final QuestState st = attacker.getQuestState(getName());
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		
		if ((npcId >= 27102) && (npcId <= 27107))
		{
			String[] VARS =
			{
				"Almors",
				"Camoniell",
				"Belthus",
				"Basilla",
				"Celestiel",
				"Brynthea"
			};
			String var = VARS[npcId - 27102];
			Integer[] i = DROPLIST_SUMMON.get(npcId);
			int start = i[0];
			int progress = i[1];
			if (st.getInt(var) == 2)
			{
				st.set(var, "3");
				st.giveItems(progress, 1);
				st.takeItems(start, 1);
				st.playSound("ItemSound.quest_itemget");
			}
			
			if (st.getQuestItemsCount(i[2]) != 0)
			{
				return null;
			}
			
			if ((attacker.getSummon() == null) || attacker.getSummon().isSummon())
			{
				st.giveItems(i[2], 1);
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		if (DROPLIST_LARA.containsKey(npcId))
		{
			Integer[] i = DROPLIST_LARA.get(npcId);
			String var = "Lara_Part";
			int value = i[0];
			int chance = i[1];
			int item = i[2];
			long count = st.getQuestItemsCount(item);
			if ((st.getInt(var) == value) && (count < 30) && Rnd.chance(chance))
			{
				st.giveItems(item, 1);
				if (count == 29)
				{
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
		else if (DROPLIST_SUMMON.containsKey(npcId))
		{
			String[] VARS =
			{
				"Almors",
				"Camoniell",
				"Belthus",
				"Basilla",
				"Celestiel",
				"Brynthea"
			};
			String var = VARS[npcId - 27102];
			Integer[] i = DROPLIST_SUMMON.get(npcId);
			int progress = i[1];
			int foul = i[2];
			int victory = i[4];
			if (st.getInt(var) == 3)
			{
				boolean isFoul = st.getQuestItemsCount(foul) == 0;
				int isName = 1;
				for (Integer item : DROPLIST_SUMMON.get(npcId))
				{
					if (isName != 1)
					{
						st.takeItems(item, -1);
					}
					isName = 0;
				}
				
				st.takeItems(progress, -1);
				if (isFoul)
				{
					st.set(var, "6");
					st.giveItems(victory, 1);
					st.playSound("ItemSound.quest_middle");
				}
				else
				{
					st.set(var, "5");
				}
			}
		}
		return null;
	}
}