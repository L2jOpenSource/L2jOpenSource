package l2r.gameserver.scripts.ai.individual;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class SeerFlouros extends AbstractNpcAI
{
	private static L2Npc SeerFlouros;
	private static L2Npc Follower;
	private static final int duration = 0x493e0;
	private static final int SeerFlourosId = 18559;
	private static final int FollowerId = 18560;
	private static long _LastAttack = 0L;
	private static boolean successDespawn = false;
	private static boolean minion = false;
	
	public SeerFlouros(int questId, String name, String descr)
	{
		super(name, descr);
		
		registerMobs(new int[]
		{
			SeerFlourosId,
			FollowerId
		});
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("despawn"))
		{
			if (!successDespawn && SeerFlouros != null && _LastAttack + 0x493e0L < System.currentTimeMillis())
			{
				cancelQuestTimer("despawn", npc, null);
				SeerFlouros.deleteMe();
				InstanceManager.getInstance().getInstance(SeerFlouros.getInstanceId()).setDuration(duration);
				successDespawn = true;
				if (Follower != null)
					Follower.deleteMe();
			}
		}
		else if (event.equalsIgnoreCase("respMinion") && SeerFlouros != null)
		{
			Follower = addSpawn(FollowerId, SeerFlouros.getX(), SeerFlouros.getY(), SeerFlouros.getZ(), SeerFlouros.getHeading(), false, 0L);
			L2Attackable target = (L2Attackable) SeerFlouros;
			Follower.setRunning();
			((L2Attackable) Follower).addDamageHate(target.getMostHated(), 0, 999);
			Follower.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == SeerFlourosId)
		{
			_LastAttack = System.currentTimeMillis();
			startQuestTimer("despawn", 60000L, npc, null, true);
			SeerFlouros = npc;
		}
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!minion)
		{
			Follower = addSpawn(FollowerId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 0L);
			minion = true;
		}
		_LastAttack = System.currentTimeMillis();
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getId() == SeerFlourosId)
		{
			cancelQuestTimer("despawn", npc, null);
			if (Follower != null)
				Follower.deleteMe();
		}
		else if (npc.getId() == FollowerId && SeerFlouros != null)
			startQuestTimer("respMinion", 30000L, npc, null);
		return null;
	}
	
	public static void main(String[] args)
	{
		new SeerFlouros(-1, SeerFlouros.class.getSimpleName(), "ai");
	}
}
