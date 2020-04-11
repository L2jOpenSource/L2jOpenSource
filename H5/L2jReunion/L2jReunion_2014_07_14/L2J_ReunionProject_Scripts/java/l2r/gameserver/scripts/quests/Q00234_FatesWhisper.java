package l2r.gameserver.scripts.quests;

import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.datatables.xml.ItemData;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;

public class Q00234_FatesWhisper extends Quest
{
	private static final String qn = "Q00234_FatesWhisper";
	
	private static final int REIRIAS_SOUL_ORB = 4666;
	private static final int KERMONS_INFERNIUM_SCEPTER = 4667;
	private static final int GOLKONDAS_INFERNIUM_SCEPTER = 4668;
	private static final int HALLATES_INFERNIUM_SCEPTER = 4669;
	private static final int INFERNIUM_VARNISH = 4672;
	private static final int REORINS_HAMMER = 4670;
	private static final int REORINS_MOLD = 4671;
	private static final int PIPETTE_KNIFE = 4665;
	private static final int RED_PIPETTE_KNIFE = 4673;
	private static final int CRYSTAL_B = 1460;
	private static final int STAR_OF_DESTINY = 5011;
	
	private final static int[] NPCs =
	{
		31002,
		30182,
		30847,
		30178,
		30833,
		31028,
		31029,
		31030,
		31027
	};
	
	private static final Map<Integer, Integer> CHEST_SPAWN = new HashMap<>();
	private static final Map<Integer, String> Weapons = new HashMap<>();
	
	public Q00234_FatesWhisper()
	{
		super(234, qn, "");
		addStartNpc(31002);
		
		for (int npc : NPCs)
		{
			addTalkId(npc);
		}
		
		addAttackId(29020);
		
		addKillId(25035, 25054, 25126, 25220);
		
		questItemIds = new int[]
		{
			PIPETTE_KNIFE,
			RED_PIPETTE_KNIFE
		};
		
		CHEST_SPAWN.put(Integer.valueOf(25035), Integer.valueOf(31027));
		CHEST_SPAWN.put(Integer.valueOf(25054), Integer.valueOf(31028));
		CHEST_SPAWN.put(Integer.valueOf(25126), Integer.valueOf(31029));
		CHEST_SPAWN.put(Integer.valueOf(25220), Integer.valueOf(31030));
		
		Weapons.put(Integer.valueOf(79), "Sword of Damascus");
		Weapons.put(Integer.valueOf(97), "Lance");
		Weapons.put(Integer.valueOf(171), "Deadman's Glory");
		Weapons.put(Integer.valueOf(175), "Art of Battle Axe");
		Weapons.put(Integer.valueOf(210), "Staff of Evil Spirits");
		Weapons.put(Integer.valueOf(234), "Demon Dagger");
		Weapons.put(Integer.valueOf(268), "Bellion Cestus");
		Weapons.put(Integer.valueOf(287), "Bow of Peril");
		Weapons.put(Integer.valueOf(2626), "Samurai Dual-sword");
		Weapons.put(Integer.valueOf(7883), "Guardian Sword");
		Weapons.put(Integer.valueOf(7889), "Wizard's Tear");
		Weapons.put(Integer.valueOf(7893), "Kaim Vanul's Bones");
		Weapons.put(Integer.valueOf(7901), "Star Buster");
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
		
		if (event.equalsIgnoreCase("31002-03.htm"))
		{
			st.set("cond", "1");
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		else if (event.equalsIgnoreCase("30182-01c.htm"))
		{
			st.giveItems(INFERNIUM_VARNISH, 1);
			st.playSound("ItemSound.quest_itemget");
		}
		else if (event.equalsIgnoreCase("30178-01a.htm"))
		{
			st.set("cond", "6");
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.equalsIgnoreCase("30833-01b.htm"))
		{
			st.set("cond", "7");
			st.giveItems(PIPETTE_KNIFE, 1);
			st.playSound("ItemSound.quest_middle");
		}
		else if (event.startsWith("selectBGrade_"))
		{
			if (st.getInt("bypass") == 1)
			{
				return null;
			}
			String bGradeId = event.replace("selectBGrade_", "");
			st.set("weaponId", bGradeId);
			htmltext = st.showHtmlFile("31002-13.htm").replace("%weaponname%", Weapons.get(Integer.valueOf(st.getInt("weaponId"))));
		}
		else if (event.startsWith("confirmWeapon"))
		{
			st.set("bypass", "1");
			htmltext = st.showHtmlFile("31002-14.htm").replace("%weaponname%", Weapons.get(Integer.valueOf(st.getInt("weaponId"))));
		}
		else if (event.startsWith("selectAGrade_"))
		{
			if (st.getInt("bypass") == 1)
			{
				int itemId = st.getInt("weaponId");
				if (st.hasQuestItems(itemId))
				{
					int aGradeItemId = Integer.parseInt(event.replace("selectAGrade_", ""));
					
					htmltext = st.showHtmlFile("31002-12.htm").replace("%weaponname%", ItemData.getInstance().getTemplate(aGradeItemId).getName());
					st.takeItems(itemId, 1);
					st.giveItems(aGradeItemId, 1);
					st.giveItems(STAR_OF_DESTINY, 1);
					st.playSound("ItemSound.quest_finish");
					st.exitQuest(false);
				}
				else
				{
					htmltext = st.showHtmlFile("31002-15.htm").replace("%weaponname%", Weapons.get(Integer.valueOf(itemId)));
				}
			}
			else
			{
				htmltext = "31002-16.htm";
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
				if (player.getLevel() >= 75)
				{
					htmltext = "31002-02.htm";
				}
				else
				{
					htmltext = "31002-01.htm";
					st.exitQuest(true);
				}
				break;
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getId())
				{
					case 31002:
						if (cond == 1)
						{
							if (!st.hasQuestItems(REIRIAS_SOUL_ORB))
							{
								htmltext = "31002-04b.htm";
							}
							else
							{
								st.set("cond", "2");
								htmltext = "31002-05.htm";
								st.takeItems(REIRIAS_SOUL_ORB, 1);
								st.playSound("ItemSound.quest_middle");
							}
						}
						else if (cond == 2)
						{
							if ((!st.hasQuestItems(KERMONS_INFERNIUM_SCEPTER)) || (!st.hasQuestItems(GOLKONDAS_INFERNIUM_SCEPTER)) || (!st.hasQuestItems(HALLATES_INFERNIUM_SCEPTER)))
							{
								htmltext = "31002-05c.htm";
							}
							else
							{
								st.set("cond", "3");
								htmltext = "31002-06.htm";
								st.takeItems(KERMONS_INFERNIUM_SCEPTER, 1);
								st.takeItems(GOLKONDAS_INFERNIUM_SCEPTER, 1);
								st.takeItems(HALLATES_INFERNIUM_SCEPTER, 1);
								st.playSound("ItemSound.quest_middle");
							}
						}
						else if (cond == 3)
						{
							if (st.getQuestItemsCount(INFERNIUM_VARNISH) < 1)
							{
								htmltext = "31002-06b.htm";
							}
							else
							{
								st.set("cond", "4");
								htmltext = "31002-07.htm";
								st.takeItems(INFERNIUM_VARNISH, 1);
								st.playSound("ItemSound.quest_middle");
							}
						}
						else if (cond == 4)
						{
							if (st.getQuestItemsCount(REORINS_HAMMER) < 1)
							{
								htmltext = "31002-07b.htm";
							}
							else
							{
								st.set("cond", "5");
								htmltext = "31002-08.htm";
								st.takeItems(REORINS_HAMMER, 1);
								st.playSound("ItemSound.quest_middle");
							}
						}
						else if ((cond > 4) && (cond < 8))
						{
							htmltext = "31002-08b.htm";
						}
						else if (cond == 8)
						{
							st.set("cond", "9");
							htmltext = "31002-09.htm";
							st.takeItems(REORINS_MOLD, 1);
							st.playSound("ItemSound.quest_middle");
						}
						else if (cond == 9)
						{
							if (st.getQuestItemsCount(CRYSTAL_B) < 984)
							{
								htmltext = "31002-09b.htm";
							}
							else
							{
								st.set("cond", "10");
								htmltext = "31002-BGradeList.htm";
								st.takeItems(CRYSTAL_B, 984);
								st.playSound("ItemSound.quest_middle");
							}
						}
						else if (cond == 10)
						{
							if (st.getInt("bypass") == 1)
							{
								int itemId = st.getInt("weaponId");
								htmltext = st.showHtmlFile(st.hasQuestItems(itemId) ? "31002-AGradeList.htm" : "31002-15.htm").replace("%weaponname%", Weapons.get(Integer.valueOf(itemId)));
							}
							else
							{
								htmltext = "31002-BGradeList.htm";
							}
						}
						break;
					case 30182:
						if (cond == 3)
						{
							if (!st.hasQuestItems(INFERNIUM_VARNISH))
							{
								htmltext = "30182-01.htm";
							}
							else
							{
								htmltext = "30182-02.htm";
							}
						}
						break;
					case 30847:
						if ((cond == 4) && (!st.hasQuestItems(REORINS_HAMMER)))
						{
							htmltext = "30847-01.htm";
							st.giveItems(REORINS_HAMMER, 1);
							st.playSound("ItemSound.quest_itemget");
						}
						else if ((cond >= 4) && (st.hasQuestItems(REORINS_HAMMER)))
						{
							htmltext = "30847-02.htm";
						}
						break;
					case 30178:
						if (cond == 5)
						{
							htmltext = "30178-01.htm";
						}
						else if (cond >= 6)
						{
							htmltext = "30178-02.htm";
						}
						break;
					case 30833:
						if (cond == 6)
						{
							htmltext = "30833-01.htm";
						}
						else if (cond == 7)
						{
							if ((st.hasQuestItems(PIPETTE_KNIFE)) && (!st.hasQuestItems(RED_PIPETTE_KNIFE)))
							{
								htmltext = "30833-02.htm";
							}
							else
							{
								htmltext = "30833-03.htm";
								st.set("cond", "8");
								st.takeItems(RED_PIPETTE_KNIFE, 1);
								st.giveItems(REORINS_MOLD, 1);
								st.playSound("ItemSound.quest_middle");
							}
						}
						else if (cond >= 8)
						{
							htmltext = "30833-04.htm";
						}
						break;
					case 31027:
						if (cond == 1)
						{
							if (!st.hasQuestItems(REIRIAS_SOUL_ORB))
							{
								htmltext = "31027-01.htm";
								st.giveItems(REIRIAS_SOUL_ORB, 1);
								st.playSound("ItemSound.quest_itemget");
							}
							else
							{
								htmltext = "31027-02.htm";
							}
						}
						else
						{
							htmltext = "31027-02.htm";
						}
						break;
					case 31028:
						if (cond == 2)
						{
							if (!st.hasQuestItems(KERMONS_INFERNIUM_SCEPTER))
							{
								htmltext = "31028-01.htm";
								st.giveItems(KERMONS_INFERNIUM_SCEPTER, 1);
								st.playSound("ItemSound.quest_itemget");
							}
							else
							{
								htmltext = "31028-02.htm";
							}
						}
						else
						{
							htmltext = "31028-02.htm";
						}
						break;
					case 31029:
						if (cond == 2)
						{
							if (!st.hasQuestItems(GOLKONDAS_INFERNIUM_SCEPTER))
							{
								htmltext = "31029-01.htm";
								st.giveItems(GOLKONDAS_INFERNIUM_SCEPTER, 1);
								st.playSound("ItemSound.quest_itemget");
							}
							else
							{
								htmltext = "31029-02.htm";
							}
						}
						else
						{
							htmltext = "31029-02.htm";
						}
						break;
					case 31030:
						if (cond == 2)
						{
							if (!st.hasQuestItems(HALLATES_INFERNIUM_SCEPTER))
							{
								htmltext = "31030-01.htm";
								st.giveItems(HALLATES_INFERNIUM_SCEPTER, 1);
								st.playSound("ItemSound.quest_itemget");
							}
							else
							{
								htmltext = "31030-02.htm";
							}
						}
						else
						{
							htmltext = "31030-02.htm";
						}
						break;
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		QuestState st = attacker.getQuestState(qn);
		if ((st == null) || (!st.isStarted()) || (isSummon))
		{
			return null;
		}
		
		if (st.getInt("cond") == 7)
		{
			if ((attacker.getActiveWeaponItem() != null) && (attacker.getActiveWeaponItem().getId() == PIPETTE_KNIFE) && (st.getQuestItemsCount(RED_PIPETTE_KNIFE) == 0))
			{
				st.giveItems(RED_PIPETTE_KNIFE, 1);
				st.takeItems(PIPETTE_KNIFE, 1);
				st.playSound("ItemSound.quest_itemget");
			}
		}
		return null;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		int npcId = npc.getId();
		if (CHEST_SPAWN.containsKey(Integer.valueOf(npcId)))
		{
			addSpawn(CHEST_SPAWN.get(Integer.valueOf(npcId)).intValue(), npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 60000L, false);
		}
		return null;
	}
}