package l2r.gameserver.scripts.custom;

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.util.Rnd;

public class NevitsHerald extends Quest
{
	private static final List<L2Npc> spawns = new FastList<>();
	private static boolean isActive = false;
	
	private static final int NevitsHerald = 4326;
	private static final int[] Antharas =
	{
		29019,
		29066,
		29067,
		29068
	};
	private static final int Valakas = 29028;
	private static final NpcStringId[] spam =
	{
		NpcStringId.SHOW_RESPECT_TO_THE_HEROES_WHO_DEFEATED_THE_EVIL_DRAGON_AND_PROTECTED_THIS_ADEN_WORLD,
		NpcStringId.SHOUT_TO_CELEBRATE_THE_VICTORY_OF_THE_HEROES,
		NpcStringId.PRAISE_THE_ACHIEVEMENT_OF_THE_HEROES_AND_RECEIVE_NEVITS_BLESSING
	};
	
	private static final int[][] _spawns =
	{
		{
			86979,
			-142785,
			-1341,
			18259
		},
		{
			44168,
			-48513,
			-801,
			31924
		},
		{
			148002,
			-55279,
			-2735,
			44315
		},
		{
			147953,
			26656,
			-2205,
			20352
		},
		{
			82313,
			53280,
			-1496,
			14791
		},
		{
			81918,
			148305,
			-3471,
			49151
		},
		{
			16286,
			142805,
			-2706,
			15689
		},
		{
			-13968,
			122050,
			-2990,
			19497
		},
		{
			-83207,
			150896,
			-3129,
			30709
		},
		{
			116892,
			77277,
			-2695,
			45056
		}
	};
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		return "4326.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		
		if (npc.getId() == NevitsHerald)
		{
			if (event.equalsIgnoreCase("buff"))
			{
				if (player.getFirstEffect(L2EffectType.NEVIT_HOURGLASS) != null)
				{
					return "4326-1.htm";
				}
				
				npc.setTarget(player);
				npc.doCast(SkillData.getInstance().getInfo(23312, 1));
				return null;
			}
		}
		else if (event.equalsIgnoreCase("text_spam"))
		{
			cancelQuestTimer("text_spam", npc, player);
			npc.broadcastPacket(new NpcSay(NevitsHerald, Say2.SHOUT, NevitsHerald, spam[Rnd.get(0, spam.length - 1)]));
			startQuestTimer("text_spam", 60000, npc, player);
			return null;
		}
		else if (event.equalsIgnoreCase("despawn"))
		{
			despawnHeralds();
		}
		return htmltext;
	}
	
	private void despawnHeralds()
	{
		if (!spawns.isEmpty())
		{
			for (L2Npc npc : spawns)
			{
				npc.deleteMe();
			}
		}
		spawns.clear();
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		ExShowScreenMessage message = null;
		if (npc.getId() == Valakas)
		{
			// message = new ExShowScreenMessage(1900151, 10000, null);
			message = new ExShowScreenMessage(NpcStringId.THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED, 2, 10000);
		}
		else
		{
			// message = new ExShowScreenMessage(1900150, 10000, null);
			message = new ExShowScreenMessage(NpcStringId.THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED, 2, 10000);
		}
		
		// message.setUpperEffect(true);
		
		for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
		{
			if (onlinePlayer == null)
			{
				continue;
			}
			
			onlinePlayer.sendPacket(message);
		}
		
		if (!isActive)
		{
			isActive = true;
			
			spawns.clear();
			
			for (int[] _spawn : _spawns)
			{
				L2Npc herald = addSpawn(NevitsHerald, _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0);
				if (herald != null)
				{
					spawns.add(herald);
				}
			}
			startQuestTimer("despawn", 14400000, npc, killer);
			startQuestTimer("text_spam", 3000, npc, killer);
		}
		return null;
	}
	
	public NevitsHerald()
	{
		super(-1, "NevitsHerald", "custom");
		
		addFirstTalkId(NevitsHerald);
		addStartNpc(NevitsHerald);
		addTalkId(NevitsHerald);
		for (int _npc : Antharas)
		{
			addKillId(_npc);
		}
		addKillId(Valakas);
	}
	
	public static void main(String[] args)
	{
		new NevitsHerald();
	}
}
