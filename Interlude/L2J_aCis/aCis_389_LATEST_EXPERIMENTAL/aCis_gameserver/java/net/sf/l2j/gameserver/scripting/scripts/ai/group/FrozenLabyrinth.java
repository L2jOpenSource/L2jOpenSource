package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Frozen Labyrinth<br>
 * Those mobs split if you use physical attacks on them.
 */
public final class FrozenLabyrinth extends L2AttackableAIScript
{
	private static final int PRONGHORN_SPIRIT = 22087;
	private static final int PRONGHORN = 22088;
	private static final int LOST_BUFFALO = 22093;
	private static final int FROST_BUFFALO = 22094;
	
	public FrozenLabyrinth()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addAttackId(PRONGHORN, FROST_BUFFALO);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		// Offensive physical skill casted on npc.
		if (skill != null && !skill.isMagic())
		{
			int spawnId = LOST_BUFFALO;
			if (npc.getNpcId() == PRONGHORN)
				spawnId = PRONGHORN_SPIRIT;
			
			Attackable monster = (Attackable) addSpawn(spawnId, npc, false, 120000, false);
			attack(monster, attacker);
			
			monster = (Attackable) addSpawn(spawnId, npc.getX() + 20, npc.getY(), npc.getZ(), npc.getHeading(), false, 120000, false);
			attack(monster, attacker);
			
			monster = (Attackable) addSpawn(spawnId, npc.getX() + 40, npc.getY(), npc.getZ(), npc.getHeading(), false, 120000, false);
			attack(monster, attacker);
			
			monster = (Attackable) addSpawn(spawnId, npc.getX(), npc.getY() + 20, npc.getZ(), npc.getHeading(), false, 120000, false);
			attack(monster, attacker);
			
			monster = (Attackable) addSpawn(spawnId, npc.getX(), npc.getY() + 40, npc.getZ(), npc.getHeading(), false, 120000, false);
			attack(monster, attacker);
			
			monster = (Attackable) addSpawn(spawnId, npc.getX(), npc.getY() + 60, npc.getZ(), npc.getHeading(), false, 120000, false);
			attack(monster, attacker);
			
			npc.deleteMe();
		}
		return super.onAttack(npc, attacker, damage, skill);
	}
}