package l2r.gameserver.scripts.ai.npc.VillageMasters;

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

public final class KamaelOccupationChange extends Quest
{
	private final static String qn = "KamaelOccupationChange";
	
	private final String preffix = "32139";
	
	private static final int GWAINS_RECOMMENTADION = 9753;
	private static final int ORKURUS_RECOMMENDATION = 9760;
	private static final int STEELRAZOR_EVALUATION = 9772;
	private static final int KAMAEL_INQUISITOR_MARK = 9782;
	private static final int SOUL_BREAKER_CERTIFICATE = 9806;
	private static final int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	private static final int SHADOW_WEAPON_COUPON_CGRADE = 8870;
	
	private static final Map<String, int[]> CLASSES = new FastMap<>();
	
	private static final int[] NPCS_MALE1 =
	{
		32139,
		32196,
		32199
	};
	
	private static final int[] NPCS_FEMALE1 =
	{
		32140,
		32193,
		32202
	};
	
	private static final int[] NPCS_MALE2 =
	{
		32146,
		32205,
		32209,
		32213,
		32217,
		32221,
		32225,
		32229,
		32233
	};
	
	private static final int[] NPCS_FEMALE2 =
	{
		32145,
		32206,
		32210,
		32214,
		32218,
		32222,
		32226,
		32230,
		32234
	};
	
	private static final int[] NPCS_ALL =
	{
		32139,
		32196,
		32199,
		32140,
		32193,
		32202,
		32146,
		32205,
		32209,
		32213,
		32217,
		32221,
		32225,
		32229,
		32233,
		32145,
		32206,
		32210,
		32214,
		32218,
		32222,
		32226,
		32230,
		32234
	};
	
	static
	{
		CLASSES.put("DR", new int[]
		{
			125,
			123,
			5,
			20,
			16,
			17,
			18,
			19,
			GWAINS_RECOMMENTADION,
			SHADOW_WEAPON_COUPON_DGRADE
		});
		
		CLASSES.put("WA", new int[]
		{
			126,
			124,
			5,
			20,
			20,
			21,
			22,
			23,
			STEELRAZOR_EVALUATION,
			SHADOW_WEAPON_COUPON_DGRADE
		});
		
		CLASSES.put("BE", new int[]
		{
			127,
			125,
			5,
			40,
			24,
			25,
			26,
			27,
			ORKURUS_RECOMMENDATION,
			SHADOW_WEAPON_COUPON_CGRADE
		});
		
		CLASSES.put("AR", new int[]
		{
			130,
			126,
			5,
			40,
			28,
			29,
			30,
			31,
			KAMAEL_INQUISITOR_MARK,
			SHADOW_WEAPON_COUPON_CGRADE
		});
		
		CLASSES.put("SBF", new int[]
		{
			129,
			126,
			5,
			40,
			40,
			41,
			42,
			43,
			SOUL_BREAKER_CERTIFICATE,
			SHADOW_WEAPON_COUPON_CGRADE
		});
		
		CLASSES.put("SBM", new int[]
		{
			128,
			125,
			5,
			40,
			40,
			41,
			42,
			43,
			SOUL_BREAKER_CERTIFICATE,
			SHADOW_WEAPON_COUPON_CGRADE
		});
	}
	
	public KamaelOccupationChange(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int id : NPCS_ALL)
		{
			addStartNpc(id);
			addTalkId(id);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		String suffix = "";
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		if (!CLASSES.containsKey(event))
		{
			return event;
		}
		
		final int req_class = CLASSES.get(event)[1];
		final int req_race = CLASSES.get(event)[2];
		final int req_level = CLASSES.get(event)[3];
		final int low_ni = CLASSES.get(event)[4];
		final int low_i = CLASSES.get(event)[5];
		final int ok_ni = CLASSES.get(event)[6];
		final int ok_i = CLASSES.get(event)[7];
		final int req_item = CLASSES.get(event)[8];
		final boolean item = st.hasQuestItems(req_item);
		if ((player.getRace().ordinal() == req_race) && (player.getClassId().getId() == req_class))
		{
			if (player.getLevel() < req_level)
			{
				suffix = "" + low_i;
				if (!item)
				{
					suffix = "" + low_ni;
				}
			}
			else
			{
				if (!item)
				{
					suffix = "" + ok_ni;
				}
				else
				{
					suffix = "" + ok_i;
					changeClass(st, player, event, req_item);
				}
			}
		}
		st.exitQuest(true);
		htmltext = preffix + "-" + suffix + ".htm";
		return htmltext;
	}
	
	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (player.isSubClassActive())
		{
			return htmltext;
		}
		
		int race = player.getRace().ordinal();
		htmltext = preffix;
		if (race == 5)
		{
			ClassId classId = player.getClassId();
			int id = classId.getId();
			int npcId = npc.getId();
			if (classId.level() >= 2)
			{
				htmltext += "-32.htm";
			}
			else if (Util.contains(NPCS_MALE1, npcId))
			{
				if (id == 123)
				{
					htmltext = htmltext + "-01.htm";
				}
				else
				{
					htmltext = htmltext + "-34.htm";
				}
			}
			else if (Util.contains(NPCS_FEMALE1, npcId))
			{
				if (id == 124)
				{
					htmltext = htmltext + "-05.htm";
				}
				else
				{
					htmltext = htmltext + "-34.htm";
				}
			}
			else if (Util.contains(NPCS_MALE2, npcId))
			{
				if (id == 125)
				{
					htmltext = htmltext + "-09.htm";
				}
				else
				{
					htmltext = htmltext + "-34.htm";
				}
			}
			else if (Util.contains(NPCS_FEMALE2, npcId))
			{
				if (id == 126)
				{
					htmltext = htmltext + "-35.htm";
				}
				else
				{
					htmltext = htmltext + "-34.htm";
				}
			}
		}
		else
		{
			htmltext += "-33.htm";
		}
		st.exitQuest(true);
		return htmltext;
	}
	
	private void changeClass(final QuestState st, final L2PcInstance player, final String event, final int req_item)
	{
		final int newclass = CLASSES.get(event)[0];
		st.takeItems(req_item, 1);
		st.giveItems(CLASSES.get(event)[9], 15);
		st.playSound("ItemSound.quest_fanfare_2");
		player.setClassId(newclass);
		player.setBaseClass(newclass);
		player.broadcastUserInfo();
	}
	
	public static void main(String[] args)
	{
		new KamaelOccupationChange(-1, qn, "ai/npc/VillageMasters");
	}
}