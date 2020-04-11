package ai.individual.extra;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;

import ai.npc.AbstractNpcAI;

/**
 * @author vGodFather
 */
public class HolyBrazier extends AbstractNpcAI
{
	private static final int HolyBrazier = 32027;
	private static final int GuardianOfTheGrail = 22133;
	
	private L2Npc _guard = null;
	private L2Npc _brazier = null;
	
	public HolyBrazier()
	{
		super(HolyBrazier.class.getSimpleName(), "ai/individual/extra");
		int[] mobs =
		{
			HolyBrazier,
			GuardianOfTheGrail
		};
		registerMobs(mobs);
	}
	
	private void spawnGuard(L2Npc npc)
	{
		if ((_guard == null) && (_brazier != null))
		{
			_guard = addSpawn(GuardianOfTheGrail, _brazier.getX(), _brazier.getY(), _brazier.getZ(), 0, false, 0);
			_guard.setIsNoRndWalk(true);
		}
		return;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == HolyBrazier)
		{
			_brazier = npc;
			_guard = null;
			npc.setIsNoRndWalk(true);
			spawnGuard(npc);
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if ((npc.getId() == GuardianOfTheGrail) && !npc.isInCombat() && (npc.getTarget() == null))
		{
			npc.setIsNoRndWalk(true);
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getId() == GuardianOfTheGrail)
		{
			_guard = null;
			spawnGuard(npc);
		}
		else if (npc.getId() == HolyBrazier)
		{
			if (_guard != null)
			{
				_guard.deleteMe();
				_guard = null;
				
			}
			_brazier = null;
		}
		return super.onKill(npc, killer, isPet);
	}
}