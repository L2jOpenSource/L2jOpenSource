package ai.zone.PlainsOfLizardman;

import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;

import ai.npc.AbstractNpcAI;

/**
 * @author vGodFather
 */
public class TantaLizardmanSummoner extends AbstractNpcAI
{
	// NPCs
	private static final int TANTA_LIZARDMAN_SUMMONER = 22774;
	private static final int TANTA_LIZARDMAN_SCOUT = 22768;
	private static final int TANTA_GUARD = 18862;
	
	// Skills
	private static final SkillHolder DEMOTIVATION_HEX = new SkillHolder(6425, 1);
	
	public TantaLizardmanSummoner()
	{
		super(TantaLizardmanSummoner.class.getSimpleName(), "ai/zone/PlainsOfLizardman");
		addAttackId(TANTA_LIZARDMAN_SUMMONER);
		addKillId(TANTA_LIZARDMAN_SUMMONER);
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getVariables().getInt("i_ai3", 0) == 0) && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.600000)))
		{
			npc.getVariables().set("i_ai3", 1);
			
			addSkillCastDesire(npc, npc, DEMOTIVATION_HEX.getSkill(), 2147483647);
			addAttackDesire(addSpawn(TANTA_LIZARDMAN_SCOUT, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false), attacker);
			addAttackDesire(addSpawn(TANTA_LIZARDMAN_SCOUT, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false), attacker);
		}
		
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		// Tanta Guard
		if (getRandom(1000) == 0)
		{
			final L2Attackable guard = (L2Attackable) addSpawn(TANTA_GUARD, npc);
			attackPlayer(guard, killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
}