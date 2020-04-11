package l2r.gameserver.scripts.quests;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

public class Q00337_AudienceWithTheLandDragon extends Quest
{
	private static final String qn = "Q00337_AudienceWithTheLandDragon";
	
	private static boolean jewel1 = false;
	private static boolean jewel2 = false;
	private static boolean jewel3 = false;
	
	private static final int GABRIELLE = 30753;
	private static final int ORVEN = 30857;
	private static final int KENDRA = 30851;
	private static final int CHAKIRIS = 30705;
	private static final int KAIENA = 30720;
	private static final int MOKE = 30498;
	private static final int HELTON = 30678;
	private static final int GILMORE = 30754;
	private static final int THEODRIC = 30755;
	
	private static final int BLOOD_QUEEN = 18001;
	private static final int SACRIFICE_OF_THE_SACRIFICED = 27171;
	private static final int HARIT_LIZARDMAN_SHAMAN = 20644;
	private static final int HARIT_LIZARDMAN_MATRIARCH = 20645;
	private static final int HARIT_LIZARDMAN_ZEALOT = 27172;
	private static final int KRANROT = 20650;
	private static final int HAMRUT = 20649;
	private static final int MARSH_DRAKE = 20680;
	private static final int MARSH_STALKER = 20679;
	private static final int ABYSSAL_JEWEL_1 = 27165;
	private static final int JEWEL_GUARDIAN_MARA = 27168;
	private static final int ABYSSAL_JEWEL_2 = 27166;
	private static final int JEWEL_GUARDIAN_MUSFEL = 27169;
	private static final int CAVE_MAIDEN = 20134;
	private static final int CAVE_KEEPER = 20246;
	private static final int ABYSSAL_JEWEL_3 = 27167;
	private static final int JEWEL_GUARDIAN_PYTON = 27170;
	
	private static final int FEATHER_OF_GABRIELLE = 3852;
	private static final int MARK_OF_WATCHMAN = 3864;
	private static final int REMAINS_OF_SACRIFIED = 3857;
	private static final int TOTEM_OF_LAND_DRAGON = 3858;
	private static final int KRANROT_SKIN = 3855;
	private static final int HAMRUT_LEG = 3856;
	private static final int MARSH_DRAKE_TALONS = 3854;
	private static final int MARSH_STALKER_HORN = 3853;
	private static final int FIRST_FRAGMENT_OF_ABYSS_JEWEL = 3859;
	private static final int MARA_FANG = 3862;
	private static final int SECOND_FRAGMENT_OF_ABYSS_JEWEL = 3860;
	private static final int MUSFEL_FANG = 3863;
	private static final int HERALD_OF_SLAYER = 3890;
	private static final int THIRD_FRAGMENT_OF_ABYSS_JEWEL = 3861;
	private static final int PORTAL_STONE = 3865;
	
	private static final int[][] DROPS_ON_KILL =
	{
		{
			SACRIFICE_OF_THE_SACRIFICED,
			1,
			1,
			5,
			REMAINS_OF_SACRIFIED
		},
		{
			HARIT_LIZARDMAN_ZEALOT,
			1,
			2,
			25,
			TOTEM_OF_LAND_DRAGON
		},
		{
			KRANROT,
			1,
			3,
			20,
			KRANROT_SKIN
		},
		{
			HAMRUT,
			1,
			3,
			20,
			HAMRUT_LEG
		},
		{
			MARSH_DRAKE,
			1,
			4,
			20,
			MARSH_DRAKE_TALONS
		},
		{
			MARSH_STALKER,
			1,
			4,
			20,
			MARSH_STALKER_HORN
		},
		{
			JEWEL_GUARDIAN_MARA,
			2,
			5,
			20,
			MARA_FANG
		},
		{
			JEWEL_GUARDIAN_MUSFEL,
			2,
			6,
			20,
			MUSFEL_FANG
		}
	};
	
	private static final int[][] DROP_ON_ATTACK =
	{
		{
			ABYSSAL_JEWEL_1,
			2,
			5,
			FIRST_FRAGMENT_OF_ABYSS_JEWEL,
			20,
			JEWEL_GUARDIAN_MARA
		},
		{
			ABYSSAL_JEWEL_2,
			2,
			6,
			SECOND_FRAGMENT_OF_ABYSS_JEWEL,
			20,
			JEWEL_GUARDIAN_MUSFEL
		},
		{
			ABYSSAL_JEWEL_3,
			4,
			7,
			THIRD_FRAGMENT_OF_ABYSS_JEWEL,
			3,
			JEWEL_GUARDIAN_PYTON
		}
	};
	
	public Q00337_AudienceWithTheLandDragon()
	{
		super(337, qn, "");
		addStartNpc(GABRIELLE);
		addTalkId(GABRIELLE, ORVEN, KENDRA, CHAKIRIS, KAIENA, MOKE, HELTON, GILMORE, THEODRIC);
		
		addAttackId(ABYSSAL_JEWEL_1, ABYSSAL_JEWEL_2, ABYSSAL_JEWEL_3);
		addKillId(BLOOD_QUEEN, SACRIFICE_OF_THE_SACRIFICED, HARIT_LIZARDMAN_SHAMAN, HARIT_LIZARDMAN_MATRIARCH, HARIT_LIZARDMAN_ZEALOT, KRANROT, HAMRUT, MARSH_DRAKE, MARSH_STALKER, JEWEL_GUARDIAN_MARA, JEWEL_GUARDIAN_MUSFEL, CAVE_MAIDEN, CAVE_KEEPER, JEWEL_GUARDIAN_PYTON);
		
		questItemIds = new int[]
		{
			FEATHER_OF_GABRIELLE,
			MARK_OF_WATCHMAN,
			REMAINS_OF_SACRIFIED,
			TOTEM_OF_LAND_DRAGON,
			KRANROT_SKIN,
			HAMRUT_LEG,
			MARSH_DRAKE_TALONS,
			MARSH_STALKER_HORN,
			FIRST_FRAGMENT_OF_ABYSS_JEWEL,
			MARA_FANG,
			SECOND_FRAGMENT_OF_ABYSS_JEWEL,
			MUSFEL_FANG,
			HERALD_OF_SLAYER,
			THIRD_FRAGMENT_OF_ABYSS_JEWEL
		};
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
		
		if (event.equalsIgnoreCase("30753-05.htm"))
		{
			st.set("drop1", "1");
			st.set("drop2", "1");
			st.set("drop3", "1");
			st.set("drop4", "1");
			st.giveItems(FEATHER_OF_GABRIELLE, 1);
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30753-09.htm"))
		{
			if (st.getQuestItemsCount(MARK_OF_WATCHMAN) >= 4)
			{
				st.set("drop5", "2");
				st.set("drop6", "2");
				st.takeItems(MARK_OF_WATCHMAN, 4);
				st.set("cond", "2");
				st.playSound("ItemSound.quest_middle");
			}
			else
			{
				htmltext = null;
			}
			
		}
		else if (event.equalsIgnoreCase("30755-05.htm"))
		{
			if (st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL))
			{
				st.takeItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL, 1);
				st.takeItems(HERALD_OF_SLAYER, 1);
				st.giveItems(PORTAL_STONE, 1);
				st.playSound("ItemSound.quest_finish");
				st.exitQuest(true);
			}
			else
			{
				htmltext = null;
			}
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
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() < 50)
				{
					st.exitQuest(true);
					htmltext = "30753-02.htm";
				}
				else
				{
					htmltext = "30753-01.htm";
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case GABRIELLE:
						if (cond == 1)
						{
							if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
							{
								htmltext = "30753-06.htm";
							}
							else
							{
								htmltext = "30753-08.htm";
							}
						}
						else if (cond == 2)
						{
							if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 2)
							{
								htmltext = "30753-10.htm";
							}
							else
							{
								st.takeItems(FEATHER_OF_GABRIELLE, 1);
								st.takeItems(MARK_OF_WATCHMAN, 1);
								st.giveItems(HERALD_OF_SLAYER, 1);
								st.set("cond", "3");
								st.playSound("ItemSound.quest_middle");
								htmltext = "30753-11.htm";
							}
						}
						else if (cond == 3)
						{
							htmltext = "30753-12.htm";
						}
						else if (cond == 4)
						{
							htmltext = "30753-13.htm";
						}
						break;
					case ORVEN:
						if (cond == 1)
						{
							if (st.getInt("drop1") == 1)
							{
								if (st.hasQuestItems(REMAINS_OF_SACRIFIED))
								{
									st.takeItems(REMAINS_OF_SACRIFIED, 1);
									st.giveItems(MARK_OF_WATCHMAN, 1);
									st.unset("drop1");
									st.playSound("ItemSound.quest_middle");
									htmltext = "30857-02.htm";
								}
								else
								{
									htmltext = "30857-01.htm";
								}
							}
							else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
							{
								htmltext = "30857-03.htm";
							}
							else
							{
								htmltext = "30857-04.htm";
							}
						}
						break;
					case KENDRA:
						if (cond == 1)
						{
							if (st.getInt("drop2") == 1)
							{
								if (st.hasQuestItems(TOTEM_OF_LAND_DRAGON))
								{
									st.takeItems(TOTEM_OF_LAND_DRAGON, 1);
									st.giveItems(MARK_OF_WATCHMAN, 1);
									st.unset("drop2");
									st.playSound("ItemSound.quest_middle");
									htmltext = "30851-02.htm";
								}
								else
								{
									htmltext = "30851-01.htm";
								}
							}
							else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
							{
								htmltext = "30851-03.htm";
							}
							else
							{
								htmltext = "30851-04.htm";
							}
						}
						break;
					case CHAKIRIS:
						if (cond == 1)
						{
							if (st.getInt("drop3") == 1)
							{
								if ((st.hasQuestItems(KRANROT_SKIN)) && (st.hasQuestItems(HAMRUT_LEG)))
								{
									st.takeItems(KRANROT_SKIN, 1);
									st.takeItems(HAMRUT_LEG, 1);
									st.giveItems(MARK_OF_WATCHMAN, 1);
									st.unset("drop3");
									st.playSound("ItemSound.quest_middle");
									htmltext = "30705-02.htm";
								}
								else
								{
									htmltext = "30705-01.htm";
								}
							}
							else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
							{
								htmltext = "30705-03.htm";
							}
							else
							{
								htmltext = "30705-04.htm";
							}
						}
						break;
					case KAIENA:
						if (cond == 1)
						{
							if (st.getInt("drop4") == 1)
							{
								if ((st.hasQuestItems(MARSH_DRAKE_TALONS)) && (st.hasQuestItems(MARSH_STALKER_HORN)))
								{
									st.takeItems(MARSH_DRAKE_TALONS, 1);
									st.takeItems(MARSH_STALKER_HORN, 1);
									st.giveItems(MARK_OF_WATCHMAN, 1);
									st.unset("drop4");
									st.playSound("ItemSound.quest_middle");
									htmltext = "30720-02.htm";
								}
								else
								{
									htmltext = "30720-01.htm";
								}
							}
							else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
							{
								htmltext = "30720-03.htm";
							}
							else
							{
								htmltext = "30720-04.htm";
							}
						}
						break;
					case MOKE:
						if (cond == 2)
						{
							switch (st.getInt("drop5"))
							{
								case 2:
									st.set("drop5", "1");
									htmltext = "30498-01.htm";
									break;
								case 1:
									if ((st.hasQuestItems(FIRST_FRAGMENT_OF_ABYSS_JEWEL)) && (st.hasQuestItems(MARA_FANG)))
									{
										st.takeItems(FIRST_FRAGMENT_OF_ABYSS_JEWEL, 1);
										st.takeItems(MARA_FANG, 1);
										st.giveItems(MARK_OF_WATCHMAN, 1);
										st.unset("drop5");
										st.playSound("ItemSound.quest_middle");
										htmltext = "30498-03.htm";
									}
									else
									{
										htmltext = "30498-02.htm";
									}
									break;
								case 0:
									if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 2)
									{
										htmltext = "30498-04.htm";
									}
									else
									{
										htmltext = "30498-05.htm";
									}
									break;
							}
						}
						break;
					case HELTON:
						if (cond == 2)
						{
							switch (st.getInt("drop6"))
							{
								case 2:
									st.set("drop6", "1");
									htmltext = "30678-01.htm";
									break;
								case 1:
									if ((st.hasQuestItems(SECOND_FRAGMENT_OF_ABYSS_JEWEL)) && (st.hasQuestItems(MUSFEL_FANG)))
									{
										st.takeItems(SECOND_FRAGMENT_OF_ABYSS_JEWEL, 1);
										st.takeItems(MUSFEL_FANG, 1);
										st.giveItems(MARK_OF_WATCHMAN, 1);
										st.unset("drop6");
										st.playSound("ItemSound.quest_middle");
										htmltext = "30678-03.htm";
									}
									else
									{
										htmltext = "30678-02.htm";
									}
									break;
								case 0:
									if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 2)
									{
										htmltext = "30678-04.htm";
									}
									else
									{
										htmltext = "30678-05.htm";
									}
									break;
							}
						}
						break;
					case GILMORE:
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "30754-01.htm";
						}
						else if (cond == 3)
						{
							st.set("drop7", "1");
							st.set("cond", "4");
							st.playSound("ItemSound.quest_middle");
							htmltext = "30754-02.htm";
						}
						else if (cond == 4)
						{
							if (st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL))
							{
								htmltext = "30754-05.htm";
							}
							else
							{
								htmltext = "30754-04.htm";
							}
						}
						break;
					case THEODRIC:
						if ((cond == 1) || (cond == 2))
						{
							htmltext = "30755-01.htm";
						}
						else if (cond == 3)
						{
							htmltext = "30755-02.htm";
						}
						else if (cond == 4)
						{
							if (st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL))
							{
								htmltext = "30755-04.htm";
							}
							else
							{
								htmltext = "30755-03.htm";
							}
						}
						break;
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		QuestState st = attacker.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		
		int npcId = npc.getId();
		
		for (int[] npcInfo : DROP_ON_ATTACK)
		{
			if (npcInfo[0] == npcId)
			{
				if (npcInfo[1] != st.getInt("cond"))
				{
					break;
				}
				
				double percentHp = ((npc.getCurrentHp() + damage) * 100.0D) / npc.getMaxHp();
				
				if (percentHp < 33.0D)
				{
					if ((getRandom(100) >= 33) || (st.getInt("drop" + npcInfo[2]) != 1))
					{
						break;
					}
					int itemId = npcInfo[3];
					if (!st.hasQuestItems(itemId))
					{
						st.giveItems(itemId, 1);
						st.playSound("ItemSound.quest_itemget");
					}
					break;
				}
				
				if (percentHp < 66.0D)
				{
					if ((getRandom(100) >= 33) || (st.getInt("drop" + npcInfo[2]) != 1))
					{
						break;
					}
					
					boolean spawn;
					if (npcId == ABYSSAL_JEWEL_3)
					{
						spawn = jewel3;
					}
					else
					{
						if (npcId == ABYSSAL_JEWEL_2)
						{
							spawn = jewel2;
						}
						else
						{
							spawn = jewel1;
						}
					}
					if (spawn)
					{
						for (int i = 0; i < npcInfo[4]; i++)
						{
							L2Npc mob = addSpawn(npcInfo[5], npc.getX() + getRandom(-150, 150), npc.getY() + getRandom(-150, 150), npc.getZ(), npc.getHeading(), true, 60000L, false);
							mob.setRunning();
							((L2Attackable) mob).addDamageHate(attacker, 0, 500);
							mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
						}
						
						if (npcId == ABYSSAL_JEWEL_3)
						{
							jewel3 = false;
						}
						else if (npcId == ABYSSAL_JEWEL_2)
						{
							jewel2 = false;
						}
						else
						{
							jewel1 = false;
						}
					}
					break;
				}
				
				if (percentHp <= 90.0D)
				{
					break;
				}
				if (npcId == ABYSSAL_JEWEL_3)
				{
					jewel3 = true;
					break;
				}
				if (npcId == ABYSSAL_JEWEL_2)
				{
					jewel2 = true;
					break;
				}
				jewel1 = true;
				break;
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if (st.getState() != State.STARTED)
		{
			return null;
		}
		
		int cond = st.getInt("cond");
		int npcId = npc.getId();
		
		switch (npcId)
		{
			case HAMRUT:
			case KRANROT:
			case MARSH_STALKER:
			case MARSH_DRAKE:
			case JEWEL_GUARDIAN_MARA:
			case JEWEL_GUARDIAN_MUSFEL:
			case SACRIFICE_OF_THE_SACRIFICED:
			case HARIT_LIZARDMAN_ZEALOT:
				for (int[] npcInfo : DROPS_ON_KILL)
				{
					if (npcInfo[0] == npcId)
					{
						if ((npcInfo[1] != cond) || (st.getInt("drop" + npcInfo[2]) != 1) || (getRandom(100) >= npcInfo[3]))
						{
							break;
						}
						int itemId = npcInfo[4];
						if (!st.hasQuestItems(itemId))
						{
							st.giveItems(itemId, 1);
							st.playSound("ItemSound.quest_itemget");
						}
						break;
					}
				}
				break;
			case BLOOD_QUEEN:
				if ((cond == 1) && (getRandom(100) < 25))
				{
					if ((st.getInt("drop1") == 1) && (!st.hasQuestItems(REMAINS_OF_SACRIFIED)))
					{
						for (int i = 0; i < 10; i++)
						{
							addSpawn(SACRIFICE_OF_THE_SACRIFICED, npc.getX() + getRandom(-100, 100), npc.getY() + getRandom(-100, 100), npc.getZ(), npc.getHeading(), true, 60000L, false);
						}
					}
				}
				break;
			case HARIT_LIZARDMAN_SHAMAN:
			case HARIT_LIZARDMAN_MATRIARCH:
				if ((cond == 1) && (getRandom(100) < 15))
				{
					if ((st.getInt("drop2") == 1) && (!st.hasQuestItems(TOTEM_OF_LAND_DRAGON)))
					{
						for (int i = 0; i < 3; i++)
						{
							addSpawn(HARIT_LIZARDMAN_ZEALOT, npc.getX() + getRandom(-50, 50), npc.getY() + getRandom(-50, 50), npc.getZ(), npc.getHeading(), true, 60000L, false);
						}
					}
				}
				break;
			case CAVE_MAIDEN:
			case CAVE_KEEPER:
				if ((cond == 4) && (getRandom(100) < 15))
				{
					if (!st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL))
					{
						addSpawn(ABYSSAL_JEWEL_3, npc.getX() + getRandom(-50, 50), npc.getY() + getRandom(-50, 50), npc.getZ(), npc.getHeading(), true, 60000L, false);
					}
				}
				break;
		}
		return null;
	}
}