package ai.npc.VillageMasters.ElvenHumanMystics2;

import l2r.gameserver.enums.Race;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

public class ElvenHumanMystics2 extends AbstractNpcAI
{
	// NPCs
	private static int[] NPCS =
	{
		30115,
		30174,
		30176,
		30694,
		30854,
		31996
	};
	
	// Items
	private static int MARK_OF_SCHOLAR = 2674;
	private static int MARK_OF_TRUST = 2734;
	private static int MARK_OF_MAGUS = 2840;
	private static int MARK_OF_LIFE = 3140;
	private static int MARK_OF_WITCHCRAFT = 3307;
	private static int MARK_OF_SUMMONER = 3336;
	
	private static int[][] CLASSES =
	{
		{
			27,
			26,
			18,
			19,
			20,
			21,
			MARK_OF_SCHOLAR,
			MARK_OF_LIFE,
			MARK_OF_MAGUS
		},
		{
			28,
			26,
			22,
			23,
			24,
			25,
			MARK_OF_SCHOLAR,
			MARK_OF_LIFE,
			MARK_OF_SUMMONER
		},
		{
			12,
			11,
			26,
			27,
			28,
			29,
			MARK_OF_SCHOLAR,
			MARK_OF_TRUST,
			MARK_OF_MAGUS
		},
		{
			13,
			11,
			30,
			31,
			32,
			33,
			MARK_OF_SCHOLAR,
			MARK_OF_TRUST,
			MARK_OF_WITCHCRAFT
		},
		{
			14,
			11,
			34,
			35,
			36,
			37,
			MARK_OF_SCHOLAR,
			MARK_OF_TRUST,
			MARK_OF_SUMMONER
		}
	};
	
	public ElvenHumanMystics2()
	{
		super(ElvenHumanMystics2.class.getSimpleName(), "ai/npc/VillageMasters");
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
				event = "30115-" + suffix + ".htm";
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
				case elvenWizard:
				{
					htmltext = "30115-01.htm";
					break;
				}
				case wizard:
				{
					htmltext = "30115-08.htm";
					break;
				}
				default:
				{
					if (cid.level() == 0)
					{
						htmltext = "30115-38.htm";
					}
					else if (cid.level() >= 2)
					{
						htmltext = "30115-39.htm";
					}
					else
					{
						htmltext = "30115-40.htm";
					}
				}
			}
		}
		else
		{
			htmltext = "30115-40.htm";
		}
		return htmltext;
	}
}