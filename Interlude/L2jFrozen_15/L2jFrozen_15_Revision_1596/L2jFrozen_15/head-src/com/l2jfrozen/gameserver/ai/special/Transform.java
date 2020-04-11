package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.util.random.Rnd;

public class Transform extends Quest implements Runnable
{
	private final ArrayList<Transformer> mobs = new ArrayList<>();
	
	private static class Transformer
	{
		private final int id;
		private final int idPoly;
		private final int chance;
		private final int message;
		
		protected Transformer(final int id, final int idPoly, final int chance, final int message)
		{
			this.id = id;
			this.idPoly = idPoly;
			this.chance = chance;
			this.message = message;
		}
		
		protected int getId()
		{
			return id;
		}
		
		protected int getIdPoly()
		{
			return idPoly;
		}
		
		protected int getChance()
		{
			return chance;
		}
		
		protected int getMessage()
		{
			return message;
		}
	}
	
	private static String[] Message =
	{
		"I cannot despise the fellow! I see his sincerity in the duel.",
		"Nows we truly begin!",
		"Fool! Right now is only practice!",
		"Have a look at my true strength.",
		"This time at the last! The end!"
	};
	
	public Transform(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		
		mobs.add(new Transformer(21261, 21262, 1, 5)); // 1st mutation Ol Mahum Transcender
		mobs.add(new Transformer(21262, 21263, 1, 5)); // 2st mutation Ol Mahum Transcender
		mobs.add(new Transformer(21263, 21264, 1, 5)); // 3rd mutation Ol Mahum Transcender
		mobs.add(new Transformer(21258, 21259, 100, 5)); // always mutation on atk Fallen Orc Shaman
		mobs.add(new Transformer(20835, 21608, 1, 5)); // zaken's seer to zaken's watchman
		mobs.add(new Transformer(21608, 21609, 1, 5)); // zaken's watchman
		mobs.add(new Transformer(20832, 21602, 1, 5)); // Zaken's pikeman
		mobs.add(new Transformer(21602, 21603, 1, 5)); // Zaken's pikeman
		mobs.add(new Transformer(20833, 21605, 1, 5)); // Zaken's archet
		mobs.add(new Transformer(21605, 21606, 1, 5)); // Zaken's archet
		mobs.add(new Transformer(21625, 21623, 1, 5)); // zaken's Elite Guard to zaken's Guard
		mobs.add(new Transformer(21623, 21624, 1, 5)); // zaken's Guard
		mobs.add(new Transformer(20842, 21620, 1, 5)); // Musveren
		mobs.add(new Transformer(21620, 21621, 1, 5)); // Musveren
		mobs.add(new Transformer(20830, 20859, 100, 0)); //
		mobs.add(new Transformer(21067, 21068, 100, 0)); //
		mobs.add(new Transformer(21062, 21063, 100, 0)); // Angels
		mobs.add(new Transformer(20831, 20860, 100, 0)); //
		mobs.add(new Transformer(21070, 21071, 100, 0)); //
		
		final int[] mobsKill =
		{
			20830,
			21067,
			21062,
			20831,
			21070
		};
		
		for (final int mob : mobsKill)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
		}
		
		final int[] mobsAttack =
		{
			21620,
			20842,
			21623,
			21625,
			21605,
			20833,
			21602,
			20832,
			21608,
			20835,
			21258
		};
		
		for (final int mob : mobsAttack)
		{
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
	}
	
	@Override
	public String onAttack(final L2NpcInstance npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		for (final Transformer monster : mobs)
		{
			if (npc.getNpcId() == monster.getId())
			{
				if (Rnd.get(100) <= monster.getChance() * Config.RATE_DROP_QUEST)
				{
					if (monster.getMessage() != 0)
					{
						npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), Message[Rnd.get(monster.getMessage())]));
					}
					npc.onDecay();
					final L2Attackable newNpc = (L2Attackable) this.addSpawn(monster.getIdPoly(), npc);
					final L2Character originalAttacker = isPet ? attacker.getPet() : attacker;
					newNpc.setRunning();
					newNpc.addDamageHate(originalAttacker, 0, 999);
					newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
					
					// NPC Spawn Effect L2OFF
					final NPCSpawnTask spawnEffectTask = new NPCSpawnTask(newNpc, 4000, 800000);
					final Thread effectThread = new Thread(spawnEffectTask);
					effectThread.start();
					
					// Like L2OFF auto target new mob (like an aggression)
					originalAttacker.setTargetTrasformedNpc(newNpc);
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(final L2NpcInstance npc, final L2PcInstance killer, final boolean isPet)
	{
		for (final Transformer monster : mobs)
		{
			if (npc.getNpcId() == monster.getId())
			{
				if (monster.getMessage() != 0)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), Message[Rnd.get(monster.getMessage())]));
				}
				final L2Attackable newNpc = (L2Attackable) this.addSpawn(monster.getIdPoly(), npc);
				final L2Character originalAttacker = isPet ? killer.getPet() : killer;
				newNpc.setRunning();
				newNpc.addDamageHate(originalAttacker, 0, 999);
				newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, originalAttacker);
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	@Override
	public void run()
	{
	}
	
	private class NPCSpawnTask implements Runnable
	{
		
		private final L2NpcInstance spawn;
		private final long spawnEffectTime;
		private final int spawnAbnormalEffect;
		
		/**
		 * @param spawn
		 * @param spawnEffectTime
		 * @param spawnAbnormalEffect
		 */
		public NPCSpawnTask(final L2NpcInstance spawn, final long spawnEffectTime, final int spawnAbnormalEffect)
		{
			super();
			this.spawn = spawn;
			this.spawnEffectTime = spawnEffectTime;
			this.spawnAbnormalEffect = Integer.decode("0x" + spawnAbnormalEffect);
		}
		
		@Override
		public void run()
		{
			spawn.startAbnormalEffect(spawnAbnormalEffect);
			
			try
			{
				Thread.sleep(spawnEffectTime);
			}
			catch (final InterruptedException e)
			{
			}
			
			spawn.stopAbnormalEffect(spawnAbnormalEffect);
		}
	}
}