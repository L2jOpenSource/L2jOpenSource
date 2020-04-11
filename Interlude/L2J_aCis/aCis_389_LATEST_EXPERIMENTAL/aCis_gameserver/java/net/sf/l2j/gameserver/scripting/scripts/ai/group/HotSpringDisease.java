package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.commons.random.Rnd;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class HotSpringDisease extends L2AttackableAIScript
{
	// Monsters
	private static final Map<Integer, Integer> MONSTERS_DISEASES = new HashMap<>(6);
	{
		MONSTERS_DISEASES.put(21314, 4551);
		MONSTERS_DISEASES.put(21316, 4552);
		MONSTERS_DISEASES.put(21317, 4553);
		MONSTERS_DISEASES.put(21319, 4552);
		MONSTERS_DISEASES.put(21321, 4551);
		MONSTERS_DISEASES.put(21322, 4553);
	}
	
	public HotSpringDisease()
	{
		super("ai/group");
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(MONSTERS_DISEASES.keySet(), ScriptEventType.ON_ATTACK_ACT, ScriptEventType.ON_FACTION_CALL, ScriptEventType.ON_SKILL_SEE);
	}
	
	@Override
	public String onAttackAct(Npc npc, Player victim)
	{
		// Try to apply Malaria.
		tryToApplyEffect(npc, victim, 4554);
		
		// Try to apply another disease, based on npcId.
		tryToApplyEffect(npc, victim, MONSTERS_DISEASES.get(npc.getNpcId()));
		
		return super.onAttackAct(npc, victim);
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isPet)
	{
		// Try to apply Malaria.
		tryToApplyEffect(npc, attacker, 4554);
		
		// Try to apply another disease, based on npcId.
		tryToApplyEffect(npc, attacker, MONSTERS_DISEASES.get(npc.getNpcId()));
		
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onSkillSee(Npc npc, Player caster, L2Skill skill, WorldObject[] targets, boolean isPet)
	{
		// Try to apply Malaria.
		tryToApplyEffect(npc, caster, 4554);
		
		// Try to apply another disease, based on npcId.
		tryToApplyEffect(npc, caster, MONSTERS_DISEASES.get(npc.getNpcId()));
		
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	/**
	 * Try to apply a disease with a 10% luck.
	 * @param npc : The {@link Npc} used as caster.
	 * @param player : The {@link Player} used as target.
	 * @param skillId : The id of the {@link L2Skill} to launch.
	 */
	private static void tryToApplyEffect(Npc npc, Player player, int skillId)
	{
		if (Rnd.get(100) < 10)
		{
			int level = 1;
			
			for (AbstractEffect effect : player.getAllEffects())
			{
				if (effect.getSkill().getId() != skillId)
					continue;
				
				// Calculate the new level skill to apply.
				level = Math.min(10, effect.getSkill().getLevel() + 1);
				
				// Exit the previous effect.
				effect.exit();
				break;
			}
			
			// Apply new effect.
			SkillTable.getInstance().getInfo(skillId, level).getEffects(npc, player);
		}
	}
}