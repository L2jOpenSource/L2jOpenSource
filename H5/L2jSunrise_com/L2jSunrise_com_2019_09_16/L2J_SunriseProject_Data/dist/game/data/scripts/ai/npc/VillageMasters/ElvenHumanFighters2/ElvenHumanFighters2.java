package ai.npc.VillageMasters.ElvenHumanFighters2;

import l2r.gameserver.enums.Race;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

public class ElvenHumanFighters2 extends AbstractNpcAI
{
	// NPCs
	private static int[] NPCS =
	{
		30109,
		30187,
		30689,
		30849,
		30900,
		31965,
		32094
	};
	
	// Items
	private static int MARK_OF_CHALLENGER = 2627;
	private static int MARK_OF_DUTY = 2633;
	private static int MARK_OF_SEEKER = 2673;
	private static int MARK_OF_TRUST = 2734;
	private static int MARK_OF_DUELIST = 2762;
	private static int MARK_OF_SEARCHER = 2809;
	private static int MARK_OF_HEALER = 2820;
	private static int MARK_OF_LIFE = 3140;
	private static int MARK_OF_CHAMPION = 3276;
	private static int MARK_OF_SAGITTARIUS = 3293;
	private static int MARK_OF_WITCHCRAFT = 3307;
	
	private static int[][] CLASSES =
	{
		{
			20,
			19,
			36,
			37,
			38,
			39,
			MARK_OF_DUTY,
			MARK_OF_LIFE,
			MARK_OF_HEALER
		},
		{
			21,
			19,
			40,
			41,
			42,
			43,
			MARK_OF_CHALLENGER,
			MARK_OF_LIFE,
			MARK_OF_DUELIST
		},
		{
			5,
			4,
			44,
			45,
			46,
			47,
			MARK_OF_DUTY,
			MARK_OF_TRUST,
			MARK_OF_HEALER
		},
		{
			6,
			4,
			48,
			49,
			50,
			51,
			MARK_OF_DUTY,
			MARK_OF_TRUST,
			MARK_OF_WITCHCRAFT
		},
		{
			8,
			7,
			52,
			53,
			54,
			55,
			MARK_OF_SEEKER,
			MARK_OF_TRUST,
			MARK_OF_SEARCHER
		},
		{
			9,
			7,
			56,
			57,
			58,
			59,
			MARK_OF_SEEKER,
			MARK_OF_TRUST,
			MARK_OF_SAGITTARIUS
		},
		{
			23,
			22,
			60,
			61,
			62,
			63,
			MARK_OF_SEEKER,
			MARK_OF_LIFE,
			MARK_OF_SEARCHER
		},
		{
			24,
			22,
			64,
			65,
			66,
			67,
			MARK_OF_SEEKER,
			MARK_OF_LIFE,
			MARK_OF_SAGITTARIUS
		},
		{
			2,
			1,
			68,
			69,
			70,
			71,
			MARK_OF_CHALLENGER,
			MARK_OF_TRUST,
			MARK_OF_DUELIST
		},
		{
			3,
			1,
			72,
			73,
			74,
			75,
			MARK_OF_CHALLENGER,
			MARK_OF_TRUST,
			MARK_OF_CHAMPION
		}
	};
	
	public ElvenHumanFighters2()
	{
		super(ElvenHumanFighters2.class.getSimpleName(), "ai/npc/VillageMasters");
		addStartNpc(NPCS);
		addTalkId(NPCS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg(player);
		}
		
		if (Util.isDigit(event))
		{
			int i = Integer.valueOf(event);
			final ClassId cid = player.getClassId();
			if (((cid.getRace() == Race.ELF) || (cid.getRace() == Race.HUMAN)) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item1 = st.hasQuestItems(CLASSES[i][6]);
				final boolean item2 = st.hasQuestItems(CLASSES[i][7]);
				final boolean item3 = st.hasQuestItems(CLASSES[i][8]);
				if (player.getLevel() < 40)
				{
					suffix = (!item1 || !item2 || !item3) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item1 || !item2 || !item3)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.takeItems(CLASSES[i][6], -1);
						st.takeItems(CLASSES[i][7], -1);
						st.takeItems(CLASSES[i][8], -1);
						playSound(player, Sound.ITEMSOUND_QUEST_FANFARE_2);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						player.broadcastUserInfo();
						st.exitQuest(false);
					}
				}
				event = "30109-" + suffix + ".htm";
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		if (player.isSubClassActive())
		{
			return htmltext;
		}
		
		final ClassId cid = player.getClassId();
		if ((cid.getRace() == Race.ELF) || (cid.getRace() == Race.HUMAN))
		{
			switch (cid)
			{
				case elvenKnight:
				{
					htmltext = "30109-01.htm";
					break;
				}
				case knight:
				{
					htmltext = "30109-08.htm";
					break;
				}
				case rogue:
				{
					htmltext = "30109-15.htm";
					break;
				}
				case elvenScout:
				{
					htmltext = "30109-22.htm";
					break;
				}
				case warrior:
				{
					htmltext = "30109-29.htm";
					break;
				}
				default:
				{
					if (cid.level() == 0)
					{
						htmltext = "30109-76.htm";
					}
					else if (cid.level() >= 2)
					{
						htmltext = "30109-77.htm";
					}
					else
					{
						htmltext = "30109-78.htm";
					}
				}
			}
		}
		else
		{
			htmltext = "30109-78.htm";
		}
		return htmltext;
	}
}