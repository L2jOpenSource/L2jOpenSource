package ai.individual.extra;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

public class Kechi extends AbstractNpcAI
{
	private static final int KECHI = 25532;
	private static final int GUARD1 = 22309;
	private static final int GUARD2 = 22310;
	private static final int GUARD3 = 22417;
	public int keshiStatus;
	
	public Kechi()
	{
		super(Kechi.class.getSimpleName(), "ai/individual/extra");
		addKillId(KECHI);
		addAttackId(KECHI);
		addSpawnId(KECHI);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == KECHI)
		{
			keshiStatus = 0;
		}
		return super.onSpawn(npc);
	}
	
	private void SpawnMobs(L2Npc npc)
	{
		addSpawn(GUARD1, 154184, 149230, -12151, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD1, 153975, 149823, -12152, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD1, 154364, 149665, -12151, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD1, 153786, 149367, -12151, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD2, 154188, 149825, -12152, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD2, 153945, 149224, -12151, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD3, 154374, 149399, -12152, 0, false, 0L, false, npc.getInstanceId());
		addSpawn(GUARD3, 153796, 149646, -12159, 0, false, 0L, false, npc.getInstanceId());
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (npc.getId() == 25532)
		{
			int maxHp = npc.getMaxHp();
			double nowHp = npc.getStatus().getCurrentHp();
			
			switch (keshiStatus)
			{
				case 0:
					if (nowHp >= (maxHp * 0.8))
					{
						break;
					}
					keshiStatus = 1;
					SpawnMobs(npc);
					break;
				case 1:
					if (nowHp >= (maxHp * 0.6))
					{
						break;
					}
					keshiStatus = 2;
					SpawnMobs(npc);
					break;
				case 2:
					if (nowHp >= (maxHp * 0.4))
					{
						break;
					}
					keshiStatus = 3;
					SpawnMobs(npc);
					break;
				case 3:
					if (nowHp >= (maxHp * 0.3))
					{
						break;
					}
					keshiStatus = 4;
					SpawnMobs(npc);
					break;
				case 4:
					if (nowHp >= (maxHp * 0.2))
					{
						break;
					}
					keshiStatus = 5;
					SpawnMobs(npc);
					break;
				case 5:
					if (nowHp >= (maxHp * 0.1))
					{
						break;
					}
					keshiStatus = 6;
					SpawnMobs(npc);
					break;
				case 6:
					if (nowHp >= (maxHp * 0.05))
					{
						break;
					}
					keshiStatus = 7;
					SpawnMobs(npc);
			}
			
		}
		
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getId() == KECHI)
		{
			addSpawn(32279, 154077, 149527, -12159, 0, false, 0L, false, killer.getInstanceId());
		}
		
		return super.onKill(npc, killer, isPet);
	}
}