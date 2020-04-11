package l2r.gameserver.scripts.ai.individual;

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class NPC_Walk extends AbstractNpcAI
{
	private L2Npc wharf_patrol01;
	private L2Npc wharf_patrol02;
	private L2Npc wharf_patrol03;
	private L2Npc wharf_patrol04;
	private static Map<Object, Object> walks99 = new FastMap<>();
	
	private void load99()
	{
		walks99.put("3262801", ((new Object[]
		{
			Integer.valueOf(0xfffdbcfa),
			Integer.valueOf(0x3e530),
			Integer.valueOf(-184),
			"3262802"
		})));
		walks99.put("3262802", ((new Object[]
		{
			Integer.valueOf(0xfffdbcc8),
			Integer.valueOf(0x3e364),
			Integer.valueOf(-184),
			"3262803"
		})));
		walks99.put("3262803", ((new Object[]
		{
			Integer.valueOf(0xfffdbb42),
			Integer.valueOf(0x3e1ac),
			Integer.valueOf(-184),
			"3262804"
		})));
		walks99.put("3262804", ((new Object[]
		{
			Integer.valueOf(0xfffdb912),
			Integer.valueOf(0x3e094),
			Integer.valueOf(-184),
			"3262805"
		})));
		walks99.put("3262805", ((new Object[]
		{
			Integer.valueOf(0xfffdbb42),
			Integer.valueOf(0x3e1ac),
			Integer.valueOf(-184),
			"3262806"
		})));
		walks99.put("3262806", ((new Object[]
		{
			Integer.valueOf(0xfffdbcc8),
			Integer.valueOf(0x3e364),
			Integer.valueOf(-184),
			"3262801"
		})));
		walks99.put("3262807", ((new Object[]
		{
			Integer.valueOf(0xfffdbcd2),
			Integer.valueOf(0x3e558),
			Integer.valueOf(-184),
			"3262808"
		})));
		walks99.put("3262808", ((new Object[]
		{
			Integer.valueOf(0xfffdbca0),
			Integer.valueOf(0x3e38c),
			Integer.valueOf(-184),
			"3262809"
		})));
		walks99.put("3262809", ((new Object[]
		{
			Integer.valueOf(0xfffdbb1a),
			Integer.valueOf(0x3e1d4),
			Integer.valueOf(-184),
			"3262810"
		})));
		walks99.put("3262810", ((new Object[]
		{
			Integer.valueOf(0xfffdb8ea),
			Integer.valueOf(0x3e0bc),
			Integer.valueOf(-184),
			"3262811"
		})));
		walks99.put("3262811", ((new Object[]
		{
			Integer.valueOf(0xfffdbb1a),
			Integer.valueOf(0x3e1d4),
			Integer.valueOf(-184),
			"3262812"
		})));
		walks99.put("3262812", ((new Object[]
		{
			Integer.valueOf(0xfffdbca0),
			Integer.valueOf(0x3e38c),
			Integer.valueOf(-184),
			"3262807"
		})));
		walks99.put("3262901", ((new Object[]
		{
			Integer.valueOf(0xfffdb41c),
			Integer.valueOf(0x3e530),
			Integer.valueOf(-184),
			"3262902"
		})));
		walks99.put("3262902", ((new Object[]
		{
			Integer.valueOf(0xfffdb44e),
			Integer.valueOf(0x3e364),
			Integer.valueOf(-184),
			"3262903"
		})));
		walks99.put("3262903", ((new Object[]
		{
			Integer.valueOf(0xfffdb5d4),
			Integer.valueOf(0x3e1ac),
			Integer.valueOf(-184),
			"3262904"
		})));
		walks99.put("3262904", ((new Object[]
		{
			Integer.valueOf(0xfffdb804),
			Integer.valueOf(0x3e094),
			Integer.valueOf(-184),
			"3262905"
		})));
		walks99.put("3262905", ((new Object[]
		{
			Integer.valueOf(0xfffdb5d4),
			Integer.valueOf(0x3e1ac),
			Integer.valueOf(-184),
			"3262906"
		})));
		walks99.put("3262906", ((new Object[]
		{
			Integer.valueOf(0xfffdb44e),
			Integer.valueOf(0x3e364),
			Integer.valueOf(-184),
			"3262901"
		})));
		walks99.put("3262907", ((new Object[]
		{
			Integer.valueOf(0xfffdb444),
			Integer.valueOf(0x3e558),
			Integer.valueOf(-184),
			"3262908"
		})));
		walks99.put("3262908", ((new Object[]
		{
			Integer.valueOf(0xfffdb476),
			Integer.valueOf(0x3e38c),
			Integer.valueOf(-184),
			"3262909"
		})));
		walks99.put("3262909", ((new Object[]
		{
			Integer.valueOf(0xfffdb5fc),
			Integer.valueOf(0x3e1d4),
			Integer.valueOf(-184),
			"3262910"
		})));
		walks99.put("3262910", ((new Object[]
		{
			Integer.valueOf(0xfffdb82c),
			Integer.valueOf(0x3e0bc),
			Integer.valueOf(-184),
			"3262911"
		})));
		walks99.put("3262911", ((new Object[]
		{
			Integer.valueOf(0xfffdb5fc),
			Integer.valueOf(0x3e1d4),
			Integer.valueOf(-184),
			"3262912"
		})));
		walks99.put("3262912", ((new Object[]
		{
			Integer.valueOf(0xfffdb476),
			Integer.valueOf(0x3e38c),
			Integer.valueOf(-184),
			"3262907"
		})));
	}
	
	public NPC_Walk(int id, String name, String descr)
	{
		super(name, descr);
		load99();
		wharf_patrol01 = addSpawn(32628, 0xfffdbcfa, 0x3e530, -184, 0, false, 0L);
		wharf_patrol02 = addSpawn(32628, 0xfffdbcd2, 0x3e558, -184, 0, false, 0L);
		wharf_patrol03 = addSpawn(32629, 0xfffdb41c, 0x3e530, -184, 0, false, 0L);
		wharf_patrol04 = addSpawn(32629, 0xfffdb444, 0x3e558, -184, 0, false, 0L);
		startQuestTimer("3262801", 5000L, wharf_patrol01, null);
		startQuestTimer("3262807", 5000L, wharf_patrol02, null);
		startQuestTimer("3262904", 5000L, wharf_patrol03, null);
		startQuestTimer("3262910", 5000L, wharf_patrol04, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (walks99.containsKey(event))
		{
			int x = ((Integer) ((Object[]) walks99.get(event))[0]).intValue();
			int y = ((Integer) ((Object[]) walks99.get(event))[1]).intValue();
			int z = ((Integer) ((Object[]) walks99.get(event))[2]).intValue();
			String nextEvent = (String) ((Object[]) walks99.get(event))[3];
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
			if (npc.getX() - 100 <= x && npc.getX() + 100 >= x && npc.getY() - 100 <= y && npc.getY() + 100 >= y)
				startQuestTimer(nextEvent, 1000L, npc, null);
			else
				startQuestTimer(event, 1000L, npc, null);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public static void main(String args[])
	{
		new NPC_Walk(-1, NPC_Walk.class.getSimpleName(), "ai");
	}
}
