package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * A fleeing NPC.<br>
 * <br>
 * His behavior is to always flee, and never attack.
 */
public class FleeingNPCs extends L2AttackableAIScript
{
	public FleeingNPCs()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(20432);
		addSpawnId(20432);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.disableCoreAi(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		// Wait the NPC to be immobile to move him again.
		if (!npc.isMoving())
			npc.fleeFrom(attacker, Config.MAX_DRIFT_RANGE);
		
		return null;
	}
}