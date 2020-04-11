package ai.modifier;

import l2r.gameserver.model.actor.L2Npc;

import ai.npc.AbstractNpcAI;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class RunningNpcs extends AbstractNpcAI
{
	// @formatter:off
	private static final int RUNNERS[] =
	{
		// Antharas Lair Cycle Runners
		22848, 22849, 22850, 22851, 22857,
	};
	// @formatter:on
	
	public RunningNpcs()
	{
		super(RunningNpcs.class.getSimpleName(), "ai/modifiers");
		addSpawnId(RUNNERS);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsRunner(true);
		return super.onSpawn(npc);
	}
}