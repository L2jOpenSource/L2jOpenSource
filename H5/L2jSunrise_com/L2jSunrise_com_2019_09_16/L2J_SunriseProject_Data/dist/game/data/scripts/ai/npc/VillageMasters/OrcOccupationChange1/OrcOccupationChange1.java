package ai.npc.VillageMasters.OrcOccupationChange1;

import l2r.gameserver.enums.Race;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.base.ClassId;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.util.Util;

import ai.npc.AbstractNpcAI;

public class OrcOccupationChange1 extends AbstractNpcAI
{
	// NPCs
	private static int[] NPCS =
	{
		30500,
		30505,
		30508,
		32150
	};
	
	// Items
	private static int MARK_OF_RAIDER = 1592;
	private static int KHAVATARI_TOTEM = 1615;
	private static int MASK_OF_MEDIUM = 1631;
	
	// Rewards
	private static int SHADOW_WEAPON_COUPON_DGRADE = 8869;
	
	private static int[][] CLASSES =
	{
		{
			45,
			44,
			9,
			10,
			11,
			12,
			MARK_OF_RAIDER
		},
		{
			47,
			44,
			13,
			14,
			15,
			16,
			KHAVATARI_TOTEM
		},
		{
			50,
			49,
			17,
			18,
			19,
			20,
			MASK_OF_MEDIUM
		}
	};
	
	public OrcOccupationChange1()
	{
		super(OrcOccupationChange1.class.getSimpleName(), "ai/npc/VillageMasters");
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
			if ((cid.getRace() == Race.ORC) && (cid.getId() == CLASSES[i][1]))
			{
				int suffix;
				final boolean item = st.hasQuestItems(CLASSES[i][6]);
				if (player.getLevel() < 20)
				{
					suffix = (!item) ? CLASSES[i][2] : CLASSES[i][3];
				}
				else
				{
					if (!item)
					{
						suffix = CLASSES[i][4];
					}
					else
					{
						suffix = CLASSES[i][5];
						st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15);
						st.takeItems(CLASSES[i][6], -1);
						player.setClassId(CLASSES[i][0]);
						player.setBaseClass(CLASSES[i][0]);
						playSound(player, Sound.ITEMSOUND_QUEST_FANFARE_2);
						player.broadcastUserInfo();
						st.exitQuest(false);
					}
				}
				String htm = suffix == 9 ? "09" : String.valueOf(suffix);
				event = npc.getId() + "-" + htm + ".htm";
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
		
		if (cid.getRace() == Race.ORC)
		{
			switch (cid)
			{
				case orcFighter:
				{
					htmltext = npc.getId() + "-01.htm";
					break;
				}
				case orcMage:
				{
					htmltext = npc.getId() + "-06.htm";
					break;
				}
				default:
				{
					if (cid.level() == 1)
					{
						return npc.getId() + "-21.htm";
					}
					else if (cid.level() >= 2)
					{
						return npc.getId() + "-22.htm";
					}
				}
			}
		}
		else
		{
			htmltext = npc.getId() + "-23.htm";
		}
		return htmltext;
	}
}