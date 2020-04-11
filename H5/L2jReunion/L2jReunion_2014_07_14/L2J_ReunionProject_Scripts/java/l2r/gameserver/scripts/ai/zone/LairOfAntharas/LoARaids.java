package l2r.gameserver.scripts.ai.zone.LairOfAntharas;

import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.scripts.ai.npc.AbstractNpcAI;

public class LoARaids extends AbstractNpcAI
{
	private static final int DRAKE_LORD = 25725;
	private static final int BEHEMOTH_LEADER = 25726;
	private static final int DRAGON_BEAST = 25727;
	L2Npc DragonBeast = null;
	L2Npc BehemothLeader = null;
	L2Npc DrakeLord = null;
	
	public LoARaids()
	{
		super(LoARaids.class.getSimpleName(), "ai");
		addKillId(new int[]
		{
			DRAKE_LORD,
			BEHEMOTH_LEADER,
			DRAGON_BEAST
		});
		addSpawnId(new int[]
		{
			DRAKE_LORD,
			BEHEMOTH_LEADER,
			DRAGON_BEAST
		});
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (npc.getId() == DRAKE_LORD)
		{
			if (DrakeLord != null)
			{
				DrakeLord.deleteMe();
			}
		}
		
		if (npc.getId() == BEHEMOTH_LEADER)
		{
			if (BehemothLeader != null)
			{
				BehemothLeader.deleteMe();
			}
		}
		
		if (npc.getId() == DRAGON_BEAST)
		{
			if (DragonBeast != null)
			{
				DragonBeast.deleteMe();
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if (killer.isInParty() && killer.getParty().isInCommandChannel() && (killer.getParty().getCommandChannel().getMemberCount() > 18))
		{
			switch (npc.getId())
			{
				case DRAKE_LORD:
				{
					DrakeLord = addSpawn(32884, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 300000, true);
					break;
				}
				case BEHEMOTH_LEADER:
				{
					BehemothLeader = addSpawn(32885, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 300000, true);
					break;
				}
				case DRAGON_BEAST:
				{
					DragonBeast = addSpawn(32886, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), false, 300000, true);
					break;
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new LoARaids();
	}
}