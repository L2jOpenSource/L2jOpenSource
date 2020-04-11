package com.l2jfrozen.gameserver.model;

import java.util.concurrent.Future;

import com.l2jfrozen.gameserver.datatables.sql.NpcTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PenaltyMonsterInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExFishingHpRegen;
import com.l2jfrozen.gameserver.network.serverpackets.ExFishingStartCombat;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.random.Rnd;

public class L2Fishing implements Runnable
{
	private L2PcInstance fisher;
	private int time;
	private int stop = 0;
	private int goodUse = 0;
	private int anim = 0;
	private int mode = 0;
	private int deceptiveMode = 0;
	private Future<?> fishAiTask;
	private boolean thinking;
	// Fish datas
	private final int fishId;
	private final int fishMaxHp;
	private int fishCurHp;
	private final double regenHp;
	private final boolean isUpperGrade;
	private int lureType;
	
	@Override
	public void run()
	{
		if (fisher == null)
		{
			return;
		}
		
		if (fishCurHp >= fishMaxHp * 2)
		{
			// The fish got away
			fisher.sendPacket(new SystemMessage(SystemMessageId.BAIT_STOLEN_BY_FISH));
			doDie(false);
		}
		else if (time <= 0)
		{
			// Time is up, so that fish got away
			fisher.sendPacket(new SystemMessage(SystemMessageId.FISH_SPIT_THE_HOOK));
			doDie(false);
		}
		else
		{
			aiTask();
		}
	}
	
	public L2Fishing(final L2PcInstance Fisher, final FishData fish, final boolean isNoob, final boolean isUpperGrade)
	{
		fisher = Fisher;
		fishMaxHp = fish.getHP();
		fishCurHp = fishMaxHp;
		regenHp = fish.getHpRegen();
		fishId = fish.getId();
		time = fish.getCombatTime() / 1000;
		this.isUpperGrade = isUpperGrade;
		
		if (isUpperGrade)
		{
			deceptiveMode = Rnd.get(100) >= 90 ? 1 : 0;
			lureType = 2;
		}
		else
		{
			deceptiveMode = 0;
			lureType = isNoob ? 0 : 1;
		}
		
		mode = Rnd.get(100) >= 80 ? 1 : 0;
		
		ExFishingStartCombat efsc = new ExFishingStartCombat(fisher, time, fishMaxHp, mode, lureType, deceptiveMode);
		fisher.broadcastPacket(efsc);
		fisher.sendPacket(new PlaySound(1, "SF_S_01", 0, 0, 0, 0, 0));
		// Succeeded in getting a bite
		fisher.sendPacket(new SystemMessage(SystemMessageId.GOT_A_BITE));
		
		if (fishAiTask == null)
		{
			fishAiTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(this, 1000, 1000);
		}
		
		efsc = null;
	}
	
	public void changeHp(final int hp, final int pen)
	{
		fishCurHp -= hp;
		if (fishCurHp < 0)
		{
			fishCurHp = 0;
		}
		
		ExFishingHpRegen efhr = new ExFishingHpRegen(fisher, time, fishCurHp, mode, goodUse, anim, pen, deceptiveMode);
		fisher.broadcastPacket(efhr);
		anim = 0;
		if (fishCurHp > fishMaxHp * 2)
		{
			fishCurHp = fishMaxHp * 2;
			doDie(false);
			return;
		}
		else if (fishCurHp == 0)
		{
			doDie(true);
			return;
		}
		
		efhr = null;
	}
	
	public synchronized void doDie(final boolean win)
	{
		if (fishAiTask != null)
		{
			fishAiTask.cancel(false);
			fishAiTask = null;
		}
		
		if (fisher == null)
		{
			return;
		}
		
		if (win)
		{
			final int check = Rnd.get(100);
			if (check <= 5)
			{
				PenaltyMonster();
			}
			else
			{
				fisher.sendPacket(new SystemMessage(SystemMessageId.YOU_CAUGHT_SOMETHING));
				fisher.addItem("Fishing", fishId, 1, null, true);
			}
		}
		fisher.EndFishing(win);
		fisher = null;
	}
	
	protected void aiTask()
	{
		if (thinking)
		{
			return;
		}
		
		thinking = true;
		time--;
		
		try
		{
			if (mode == 1)
			{
				if (deceptiveMode == 0)
				{
					fishCurHp += (int) regenHp;
				}
			}
			else
			{
				if (deceptiveMode == 1)
				{
					fishCurHp += (int) regenHp;
				}
			}
			
			if (stop == 0)
			{
				stop = 1;
				int check = Rnd.get(100);
				
				if (check >= 70)
				{
					mode = mode == 0 ? 1 : 0;
				}
				if (isUpperGrade)
				{
					check = Rnd.get(100);
					if (check >= 90)
					{
						deceptiveMode = deceptiveMode == 0 ? 1 : 0;
					}
				}
			}
			else
			{
				stop--;
			}
		}
		finally
		{
			thinking = false;
			ExFishingHpRegen efhr = new ExFishingHpRegen(fisher, time, fishCurHp, mode, 0, anim, 0, deceptiveMode);
			if (anim != 0)
			{
				fisher.broadcastPacket(efhr);
			}
			else
			{
				fisher.sendPacket(efhr);
			}
			
			efhr = null;
		}
	}
	
	public void useRealing(final int dmg, final int pen)
	{
		anim = 2;
		if (Rnd.get(100) > 90)
		{
			fisher.sendPacket(new SystemMessage(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
			goodUse = 0;
			changeHp(0, pen);
			return;
		}
		
		if (fisher == null)
		{
			return;
		}
		
		if (mode == 1)
		{
			if (deceptiveMode == 0)
			{
				// Reeling is successful, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				
				if (pen == 50)
				{
					sm = new SystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					fisher.sendPacket(sm);
				}
				
				goodUse = 1;
				changeHp(dmg, pen);
				
				sm = null;
			}
			else
			{
				// Reeling failed, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				goodUse = 2;
				changeHp(-dmg, pen);
				
				sm = null;
			}
		}
		else
		{
			if (deceptiveMode == 0)
			{
				// Reeling failed, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				goodUse = 2;
				changeHp(-dmg, pen);
				
				sm = null;
			}
			else
			{
				// Reeling is successful, Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				
				if (pen == 50)
				{
					sm = new SystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					fisher.sendPacket(sm);
				}
				
				goodUse = 1;
				changeHp(dmg, pen);
				
				sm = null;
			}
		}
	}
	
	public void usePomping(final int dmg, final int pen)
	{
		anim = 1;
		
		if (Rnd.get(100) > 90)
		{
			fisher.sendPacket(new SystemMessage(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN));
			goodUse = 0;
			changeHp(0, pen);
			return;
		}
		
		if (fisher == null)
		{
			return;
		}
		
		if (mode == 0)
		{
			if (deceptiveMode == 0)
			{
				// Pumping is successful. Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				
				if (pen == 50)
				{
					sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					fisher.sendPacket(sm);
				}
				
				goodUse = 1;
				changeHp(dmg, pen);
				
				sm = null;
			}
			else
			{
				// Pumping failed, Regained: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				goodUse = 2;
				changeHp(-dmg, pen);
				
				sm = null;
			}
		}
		else
		{
			if (deceptiveMode == 0)
			{
				// Pumping failed, Regained: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				goodUse = 2;
				changeHp(-dmg, pen);
				sm = null;
			}
			else
			{
				// Pumping is successful. Damage: $s1
				SystemMessage sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE);
				sm.addNumber(dmg);
				fisher.sendPacket(sm);
				
				if (pen == 50)
				{
					sm = new SystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1);
					sm.addNumber(pen);
					fisher.sendPacket(sm);
				}
				
				goodUse = 1;
				changeHp(dmg, pen);
				
				sm = null;
			}
		}
	}
	
	private void PenaltyMonster()
	{
		final int lvl = (int) Math.round(fisher.getLevel() * 0.1);
		int npcid;
		
		fisher.sendPacket(new SystemMessage(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK));
		switch (lvl)
		{
			case 0:
			case 1:
				npcid = 18319;
				break;
			case 2:
				npcid = 18320;
				break;
			case 3:
				npcid = 18321;
				break;
			case 4:
				npcid = 18322;
				break;
			case 5:
				npcid = 18323;
				break;
			case 6:
				npcid = 18324;
				break;
			case 7:
				npcid = 18325;
				break;
			case 8:
			case 9:
				npcid = 18326;
				break;
			default:
				npcid = 18319;
				break;
		}
		
		L2NpcTemplate temp;
		temp = NpcTable.getInstance().getTemplate(npcid);
		
		if (temp != null)
		{
			try
			{
				L2Spawn spawn = new L2Spawn(temp);
				spawn.setLocx(fisher.getFishx());
				spawn.setLocy(fisher.getFishy());
				spawn.setLocz(fisher.getFishz());
				spawn.setAmount(1);
				spawn.setHeading(fisher.getHeading());
				spawn.stopRespawn();
				((L2PenaltyMonsterInstance) spawn.doSpawn()).setPlayerToKill(fisher);
				spawn = null;
			}
			catch (final Exception e)
			{
				// Nothing
			}
		}
		
		temp = null;
	}
}
