package net.sf.l2j.gameserver.scripting.scripts.ai.group;

import net.sf.l2j.commons.util.ArraysUtil;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.spawn.Spawn;
import net.sf.l2j.gameserver.scripting.scripts.ai.L2AttackableAIScript;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Primeval Isle AIs. This script controls following behaviors :
 * <ul>
 * <li>Sprigant : casts a spell if you enter in aggro range, finish task if die or none around.</li>
 * <li>Ancient Egg : call all NPCs in a 2k range if attacked.</li>
 * <li>Pterosaurs and Tyrannosaurus : can see through Silent Move.</li>
 * </ul>
 */
public class PrimevalIsle extends L2AttackableAIScript
{
	private static final int[] SPRIGANTS =
	{
		18345,
		18346
	};
	
	private static final int[] MOBIDS =
	{
		22199,
		22215,
		22216,
		22217
	};
	
	private static final int ANCIENT_EGG = 18344;
	
	private static final L2Skill ANESTHESIA = SkillTable.getInstance().getInfo(5085, 1);
	private static final L2Skill POISON = SkillTable.getInstance().getInfo(5086, 1);
	
	public PrimevalIsle()
	{
		super("ai/group");
		
		for (Spawn npc : SpawnTable.getInstance().getSpawns())
			if (ArraysUtil.contains(MOBIDS, npc.getNpcId()) && npc.getNpc() != null && npc.getNpc() instanceof Attackable)
				((Attackable) npc.getNpc()).seeThroughSilentMove(true);
	}
	
	@Override
	protected void registerNpcs()
	{
		addEventIds(SPRIGANTS, ScriptEventType.ON_AGGRO, ScriptEventType.ON_KILL);
		addAttackId(ANCIENT_EGG);
		addSpawnId(MOBIDS);
	}
	
	@Override
	public String onTimer(String name, Npc npc, Player player)
	{
		if (!(npc instanceof Attackable))
			return null;
		
		if (name.equalsIgnoreCase("skill"))
		{
			int playableCounter = 0;
			for (Playable playable : npc.getKnownTypeInRadius(Playable.class, npc.getTemplate().getAggroRange()))
			{
				if (!playable.isDead())
					playableCounter++;
			}
			
			// If no one is inside aggro range, drop the task.
			if (playableCounter == 0)
			{
				cancelQuestTimers("skill", npc);
				return null;
			}
			
			// npc.setTarget(npc);
			npc.getAI().tryTo(IntentionType.CAST, new SkillUseHolder((npc.getNpcId() == 18345) ? ANESTHESIA : POISON, npc, false, false), null);
		}
		return null;
	}
	
	@Override
	public String onAggro(Npc npc, Player player, boolean isPet)
	{
		if (player == null)
			return null;
			
		// Instant use
		// npc.setTarget(npc);
		npc.getAI().tryTo(IntentionType.CAST, new SkillUseHolder((npc.getNpcId() == 18345) ? ANESTHESIA : POISON, npc, false, false), null);
		
		// Launch a task every 15sec.
		startQuestTimerAtFixedRate("skill", npc, null, 15000);
		
		return super.onAggro(npc, player, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Creature killer)
	{
		cancelQuestTimers("skill", npc);
		
		return super.onKill(npc, killer);
	}
	
	@Override
	public String onAttack(Npc npc, Creature attacker, int damage, L2Skill skill)
	{
		// Make all mobs found in a radius 2k aggressive towards attacker.
		for (Attackable called : attacker.getKnownTypeInRadius(Attackable.class, 2000))
		{
			// Caller hasn't AI or is dead.
			if (!called.hasAI() || called.isDead())
				continue;
			
			// Check if the Attackable can help the actor.
			final IntentionType calledIntention = called.getAI().getCurrentIntention().getType();
			if ((calledIntention == IntentionType.IDLE || calledIntention == IntentionType.ACTIVE || (calledIntention == IntentionType.MOVE_TO && !called.isRunning())))
				attack(called, attacker, 1);
		}
		
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		if (npc instanceof Attackable)
			((Attackable) npc).seeThroughSilentMove(true);
		
		return super.onSpawn(npc);
	}
}