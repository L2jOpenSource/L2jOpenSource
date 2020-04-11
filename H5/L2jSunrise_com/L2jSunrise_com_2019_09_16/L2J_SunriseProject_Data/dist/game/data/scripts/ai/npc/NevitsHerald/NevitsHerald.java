package ai.npc.NevitsHerald;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.ExShowScreenMessage;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.util.Rnd;

import ai.npc.AbstractNpcAI;

public class NevitsHerald extends AbstractNpcAI
{
	private static final Set<L2Npc> spawns = ConcurrentHashMap.newKeySet();
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
	
	//@formatter:off
	private static final int[][] _spawns =
	{
		{ 86979, -142785, -1341, 18259 },
		{ 44168, -48513, -801, 31924 },
		{ 148002, -55279, -2735, 44315 },
		{ 147953, 26656, -2205, 20352 },
		{ 82313, 53280, -1496, 14791 },
		{ 81918, 148305, -3471, 49151 },
		{ 16286, 142805, -2706, 15689 },
		{ -13968, 122050, -2990, 19497 },
		{ -83207, 150896, -3129, 30709 },
		{ 116892, 77277, -2695, 45056 }
	};
	//@formatter:on
	
	public NevitsHerald()
	{
		super(NevitsHerald.class.getSimpleName(), "ai/npc");
		
		addFirstTalkId(NevitsHerald);
		addStartNpc(NevitsHerald);
		addTalkId(NevitsHerald);
		addKillId(Antharas);
		addKillId(Valakas);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
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
				if (player.isAffectedBySkill(23312))
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
		isActive = false;
		spawns.clear();
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		ExShowScreenMessage message = new ExShowScreenMessage(npc.getId() == Valakas ? NpcStringId.THE_EVIL_FIRE_DRAGON_VALAKAS_HAS_BEEN_DEFEATED : NpcStringId.THE_EVIL_LAND_DRAGON_ANTHARAS_HAS_BEEN_DEFEATED, 2, 10000);
		// message.setUpperEffect(true);
		
		for (L2PcInstance onlinePlayer : L2World.getInstance().getPlayers())
		{
			if (onlinePlayer.isOnline())
			{
				onlinePlayer.sendPacket(message);
			}
		}
		
		if (!isActive)
		{
			isActive = true;
			
			spawns.clear();
			
			for (int[] _spawn : _spawns)
			{
				L2Npc herald = addSpawn(NevitsHerald, _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0);
				spawns.add(herald);
			}
			startQuestTimer("despawn", 14400000, npc, killer);
			startQuestTimer("text_spam", 3000, npc, killer);
		}
		return null;
	}
}
