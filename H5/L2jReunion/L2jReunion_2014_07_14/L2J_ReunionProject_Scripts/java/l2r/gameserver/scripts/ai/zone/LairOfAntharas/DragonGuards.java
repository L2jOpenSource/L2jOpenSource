package l2r.gameserver.scripts.ai.zone.LairOfAntharas;

import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2MonsterInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class DragonGuards extends AbstractNpcAI
{
	private static final int DRAGON_GUARD = 22852;
	private static final int DRAGON_MAGE = 22853;
	
	private static final int[] WALL_MONSTERS =
	{
		DRAGON_GUARD,
		DRAGON_MAGE
	};
	
	public DragonGuards(String name, String descr)
	{
		super(name, descr);
		
		for (int mobId : WALL_MONSTERS)
		{
			addSpawnId(mobId);
			addAggroRangeEnterId(mobId);
			addAttackId(mobId);
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc instanceof L2MonsterInstance)
		{
			for (int mobId : WALL_MONSTERS)
			{
				if (mobId == npc.getId())
				{
					final L2MonsterInstance monster = (L2MonsterInstance) npc;
					monster.setIsImmobilized(true);
					break;
				}
			}
		}
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isSummon)
	{
		if ((!npc.isCastingNow()) && (!npc.isAttackingNow()) && (!npc.isInCombat()) && (!player.isDead()))
		{
			npc.setIsImmobilized(false);
			npc.setRunning();
			((L2Attackable) npc).addDamageHate(player, 0, 999);
			((L2Attackable) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
		}
		return super.onAggroRangeEnter(npc, player, isSummon);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isSummon)
	{
		if (npc instanceof L2MonsterInstance)
		{
			for (int mobId : WALL_MONSTERS)
			{
				if (mobId == npc.getId())
				{
					final L2MonsterInstance monster = (L2MonsterInstance) npc;
					monster.setIsImmobilized(false);
					monster.setRunning();
					break;
				}
			}
		}
		return super.onAttack(npc, player, damage, isSummon);
	}
	
	public static void main(String[] args)
	{
		new DragonGuards(DragonGuards.class.getSimpleName(), "ai");
	}
}