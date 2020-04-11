package com.l2jfrozen.gameserver.ai.special;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.GrandBossManager;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.Announcements;
import com.l2jfrozen.gameserver.model.quest.Quest;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.util.random.Rnd;

public class QueenAnt extends Quest implements Runnable
{
	private static final int QUEEN = 29001;
	private static final int LARVA = 29002;
	private static final int NURSE = 29003;
	private static final int GUARD = 29004;
	private static final int ROYAL = 29005;
	
	// QUEEN Status Tracking :
	private static final int LIVE = 0; // Queen Ant is spawned.
	private static final int DEAD = 1; // Queen Ant has been killed.
	
	private L2MonsterInstance larva = null;
	private L2MonsterInstance queen = null;
	private List<L2MonsterInstance> royalGuards = new ArrayList<>();
	private List<L2MonsterInstance> antNurses = new ArrayList<>();
	
	public QueenAnt(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		final int[] mobs =
		{
			QUEEN,
			LARVA,
			NURSE,
			GUARD,
			ROYAL
		};
		for (int mob : mobs)
		{
			addEventId(mob, Quest.QuestEventType.ON_KILL);
			addEventId(mob, Quest.QuestEventType.ON_ATTACK);
		}
		
		StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
		
		int status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		switch (status)
		{
			case DEAD:
			{
				long milisecondsTime = info.getLong("respawn_time") - System.currentTimeMillis();
				if (milisecondsTime > 0)
				{
					startQuestTimer("QUEEN_ANT_SPAWN", milisecondsTime, null, null);
				}
			}
				break;
			case LIVE:
				startQuestTimer("QUEEN_ANT_SPAWN", 0, null, null);
				break;
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		switch (event)
		{
			case "QUEEN_ANT_SPAWN":
			{
				L2GrandBossInstance queen = (L2GrandBossInstance) addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0);
				
				GmListTable.broadcastMessageToGMs("Spawning Grand Boss " + queen.getName() + " (" + queen.getNpcId() + ").");
				
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					Announcements.getInstance().announceToAll("Grand Boss " + queen.getName() + " spawned in world.");
				}
				
				GrandBossManager.getInstance().setBossStatus(QUEEN, LIVE);
				GrandBossManager.getInstance().addBoss(queen);
				
				startQuestTimer("ACTION", 10000, queen, null, true);
				queen.broadcastPacket(new PlaySound(1, "BS02_D", 1, queen.getObjectId(), queen.getX(), queen.getY(), queen.getZ()));
				startQuestTimer("SPAWN_ROYAL", 1000, queen, null);
				startQuestTimer("NURSES_SPAWN", 1000, queen, null);
				startQuestTimer("CHECK_MINIONS_ZONE", 30000, queen, null, true);
				startQuestTimer("HEAL", 1000, null, null, true);
				larva = (L2MonsterInstance) addSpawn(LARVA, -21600, 179482, -5846, Rnd.get(360), false, 0);
				larva.setIsUnkillable(true);
				larva.setIsImobilised(true);
				larva.setIsAttackDisabled(true);
			}
				break;
			case "LARVA_DESPAWN":
				larva.decayMe();
				break;
			case "NURSES_SPAWN":
			{
				int radius = 400;
				for (int i = 0; i < 6; i++)
				{
					final int x = (int) (radius * Math.cos(i * 1.407)); // 1.407~2pi/6
					final int y = (int) (radius * Math.sin(i * 1.407));
					antNurses.add((L2MonsterInstance) addSpawn(NURSE, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
					antNurses.get(i).setIsAttackDisabled(true);
				}
			}
				break;
			case "SPAWN_ROYAL":
			{
				int radius = 400;
				for (int i = 0; i < 8; i++)
				{
					int x = (int) (radius * Math.cos(i * .7854)); // .7854~2pi/8
					int y = (int) (radius * Math.sin(i * .7854));
					royalGuards.add((L2MonsterInstance) addSpawn(ROYAL, npc.getX() + x, npc.getY() + y, npc.getZ(), 0, false, 0));
				}
			}
				break;
			case "RESPAWN_ROYAL":
				royalGuards.add((L2MonsterInstance) addSpawn(ROYAL, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));
				break;
			case "RESPAWN_NURSE":
				antNurses.add((L2MonsterInstance) addSpawn(NURSE, npc.getX(), npc.getY(), npc.getZ(), 0, true, 0));
				break;
			case "DESPAWN_MINIONS":
			{
				for (int i = 0; i < royalGuards.size(); i++)
				{
					L2Attackable mob = royalGuards.get(i);
					if (mob != null)
					{
						mob.decayMe();
					}
				}
				for (int k = 0; k < antNurses.size(); k++)
				{
					final L2MonsterInstance nurse = antNurses.get(k);
					if (nurse != null)
					{
						nurse.decayMe();
					}
				}
				antNurses.clear();
				royalGuards.clear();
			}
				break;
			case "CHECK_MINIONS_ZONE":
			{
				for (int i = 0; i < royalGuards.size(); i++)
				{
					L2Attackable mob = royalGuards.get(i);
					
					if (mob != null && !mob.isInsideRadius(npc.getX(), npc.getY(), 700, false))
					{
						mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					}
				}
			}
				break;
			case "ACTION":
			{
				if (Rnd.get(3) == 0)
				{
					if (Rnd.get(2) == 0)
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
					}
					else
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), 4));
					}
				}
			}
				break;
			case "HEAL":
			{
				boolean notCasting;
				boolean larvaNeedHeal = larva != null && larva.getCurrentHp() < larva.getMaxHp();
				boolean queenNeedHeal = queen != null && queen.getCurrentHp() < queen.getMaxHp();
				boolean nurseNeedHeal = false;
				for (int i = 0; i < antNurses.size(); i++)
				{
					L2MonsterInstance antNurse = antNurses.get(i);
					nurseNeedHeal = antNurse != null && antNurse.getCurrentHp() < antNurse.getMaxHp();
					if (antNurse == null || antNurse.isDead() || antNurse.isCastingNow())
					{
						continue;
					}
					notCasting = antNurse.getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST;
					if (larvaNeedHeal)
					{
						if (antNurse.getTarget() != larva || notCasting)
						{
							getIntoPosition(antNurse, larva);
							antNurse.setTarget(larva);
							antNurse.doCast(SkillTable.getInstance().getInfo(4020, 1));
							antNurse.doCast(SkillTable.getInstance().getInfo(4024, 1));
						}
						continue;
					}
					if (queenNeedHeal)
					{
						if (antNurse.getTarget() != queen || notCasting)
						{
							getIntoPosition(antNurse, queen);
							antNurse.setTarget(queen);
							antNurse.doCast(SkillTable.getInstance().getInfo(4020, 1));
						}
						continue;
					}
					if (nurseNeedHeal)
					{
						if (antNurse.getTarget() != antNurse || notCasting)
						{
							for (int k = 0; k < antNurses.size(); k++)
							{
								getIntoPosition(antNurses.get(k), antNurse);
								antNurses.get(k).setTarget(antNurse);
								antNurses.get(k).doCast(SkillTable.getInstance().getInfo(4020, 1));
							}
							
						}
					}
					if (notCasting && antNurse.getTarget() != null)
					{
						antNurse.setTarget(null);
					}
				}
			}
				break;
			default:
				LOGGER.info("QUEEN: Not defined event: " + event + "!");
				break;
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		int npcId = npc.getNpcId();
		if (npcId == NURSE)
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
			return null;
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		
		int status = GrandBossManager.getInstance().getBossStatus(QUEEN);
		
		if (npcId == QUEEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			
			if (!npc.getSpawn().isCustomRaidBoss())
			{
				GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD);
				// time is 36hour +/- 17hour
				long respawnTime = (Config.QA_RESP_FIRST + Rnd.get(Config.QA_RESP_SECOND)) * 3600000;
				startQuestTimer("QUEEN_ANT_SPAWN", respawnTime, null, null);
				startQuestTimer("LARVA_DESPAWN", 4 * 60 * 60 * 1000, null, null);
				cancelQuestTimer("ACTION", npc, null);
				cancelQuestTimer("SPAWN_ROYAL", npc, null);
				cancelQuestTimer("CHECK_MINIONS_ZONE", npc, null);
				cancelQuestTimer("HEAL", null, null);
				// also save the respawn time so that the info is maintained past reboots
				StatsSet info = GrandBossManager.getInstance().getStatsSet(QUEEN);
				info.set("respawn_time", System.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatsSet(QUEEN, info);
			}
			
			startQuestTimer("DESPAWN_MINIONS", 10000, null, null);
		}
		else if (status == LIVE)
		{
			if (npcId == NURSE)
			{
				antNurses.remove(npc);
				
				if (antNurses.size() < 6)
				{
					startQuestTimer("RESPAWN_NURSE", Config.QA_RESP_NURSE * 1000, npc, null);
				}
			}
			else if (npcId == ROYAL)
			{
				royalGuards.remove(npc);
				
				if (antNurses.size() < 8)
				{
					startQuestTimer("RESPAWN_ROYAL", (Config.QA_RESP_ROYAL + Rnd.get(40)) * 1000, npc, null);
				}
			}
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public void getIntoPosition(L2MonsterInstance nurse, L2MonsterInstance caller)
	{
		if (!nurse.isInsideRadius(caller, 300, false, false))
		{
			nurse.getAI().moveToPawn(caller, 300);
		}
	}
	
	@Override
	public void run()
	{
	}
}