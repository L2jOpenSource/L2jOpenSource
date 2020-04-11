/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00311_ExpulsionOfEvilSpirits;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.QuestSound;
import l2r.gameserver.instancemanager.ZoneManager;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.zone.L2ZoneType;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.util.Rnd;

/**
 * Expulsion of Evil Spirits (311)
 * @author Zoey76
 */
public final class Q00311_ExpulsionOfEvilSpirits extends Quest
{
	// NPC
	private static final int CHAIREN = 32655;
	// Items
	private static final int PROTECTION_SOULS_PENDANT = 14848;
	private static final int SOUL_CORE_CONTAINING_EVIL_SPIRIT = 14881;
	private static final int RAGNA_ORCS_AMULET = 14882;
	// Misc
	private static final int MIN_LEVEL = 80;
	private static final int SOUL_CORE_COUNT = 10;
	private static final int RAGNA_ORCS_KILLS_COUNT = 100;
	private static final int RAGNA_ORCS_AMULET_COUNT = 10;
	// Monsters
	private static final Map<Integer, Double> MONSTERS = new HashMap<>();
	
	// Misc
	private final static double CHANCE_AMULET = 70;
	private L2Npc _varangka;
	private L2Npc _varangkaMinion1;
	private L2Npc _varangkaMinion2;
	protected L2Npc _altar;
	private final int ALTARZONE = 20201;
	private long respawnTime = 0;
	
	private final static int ALTAR = 18811;
	private final static int VARANGKA = 18808;
	
	static
	{
		MONSTERS.put(22691, 0.694); // Ragna Orc
		MONSTERS.put(22692, 0.716); // Ragna Orc Warrior
		MONSTERS.put(22693, 0.736); // Ragna Orc Hero
		MONSTERS.put(22694, 0.712); // Ragna Orc Commander
		MONSTERS.put(22695, 0.698); // Ragna Orc Healer
		MONSTERS.put(22696, 0.692); // Ragna Orc Shaman
		MONSTERS.put(22697, 0.640); // Ragna Orc Seer
		MONSTERS.put(22698, 0.716); // Ragna Orc Archer
		MONSTERS.put(22699, 0.752); // Ragna Orc Sniper
		MONSTERS.put(22701, 0.716); // Varangka's Dre Vanul
		MONSTERS.put(22702, 0.662); // Varangka's Destroyer
		
		MONSTERS.put(18808, CHANCE_AMULET);
		MONSTERS.put(18809, 0.694);
		MONSTERS.put(18810, 0.694);
	}
	
	public Q00311_ExpulsionOfEvilSpirits()
	{
		super(311, Q00311_ExpulsionOfEvilSpirits.class.getSimpleName(), "Expulsion of Evil Spirits");
		addStartNpc(CHAIREN);
		addTalkId(CHAIREN);
		addKillId(MONSTERS.keySet());
		registerQuestItems(SOUL_CORE_CONTAINING_EVIL_SPIRIT, RAGNA_ORCS_AMULET);
		
		addEnterZoneId(ALTARZONE);
		addAttackId(ALTAR);
		
		try
		{
			respawnTime = Long.valueOf(loadGlobalQuestVar("VarangkaRespawn"));
		}
		catch (Exception e)
		{
		
		}
		saveGlobalQuestVar("VarangkaRespawn", String.valueOf(respawnTime));
		if ((respawnTime == 0) || ((respawnTime - System.currentTimeMillis()) < 0))
		{
			startQuestTimer("altarSpawn", 5000, null, null);
		}
		else
		{
			startQuestTimer("altarSpawn", respawnTime - System.currentTimeMillis(), null, null);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("altarSpawn"))
		{
			if (!checkIfSpawned(ALTAR))
			{
				_altar = addSpawn(ALTAR, 74120, -101920, -960, 32760, false, 0);
				_altar.setIsInvul(true);
				saveGlobalQuestVar("VarangkaRespawn", String.valueOf(0));
				Collection<L2PcInstance> inside = _altar.getKnownList().getKnownPlayersInRadius(1200);
				for (L2PcInstance pc : inside)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new zoneCheck(pc), 1000);
				}
			}
			return null;
		}
		else if (event.equalsIgnoreCase("minion1") && checkIfSpawned(VARANGKA))
		{
			if (!checkIfSpawned(VARANGKA + 1) && checkIfSpawned(VARANGKA))
			{
				_varangkaMinion1 = addSpawn(VARANGKA + 1, player.getX() + Rnd.get(10, 50), player.getY() + Rnd.get(10, 50), -967, 0, false, 0);
				_varangkaMinion1.setRunning();
				((L2Attackable) _varangkaMinion1).addDamageHate(_varangka.getTarget().getActingPlayer(), 1, 99999);
				_varangkaMinion1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _varangka.getTarget().getActingPlayer());
			}
			return null;
		}
		else if (event.equalsIgnoreCase("minion2"))
		{
			if (!checkIfSpawned(VARANGKA + 2) && checkIfSpawned(VARANGKA))
			{
				_varangkaMinion2 = addSpawn(VARANGKA + 2, player.getX() + Rnd.get(10, 50), player.getY() + Rnd.get(10, 50), -967, 0, false, 0);
				_varangkaMinion2.setRunning();
				((L2Attackable) _varangkaMinion2).addDamageHate(_varangka.getTarget().getActingPlayer(), 1, 99999);
				_varangkaMinion2.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, _varangka.getTarget().getActingPlayer());
			}
			return null;
		}
		
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		if (player.getLevel() < MIN_LEVEL)
		{
			return null;
		}
		
		switch (event)
		{
			case "32655-03.htm":
			case "32655-15.html":
			{
				htmltext = event;
				break;
			}
			case "32655-04.htm":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "32655-11.html":
			{
				if (getQuestItemsCount(player, SOUL_CORE_CONTAINING_EVIL_SPIRIT) >= SOUL_CORE_COUNT)
				{
					takeItems(player, SOUL_CORE_CONTAINING_EVIL_SPIRIT, SOUL_CORE_COUNT);
					giveItems(player, PROTECTION_SOULS_PENDANT, 1);
					htmltext = event;
				}
				else
				{
					htmltext = "32655-12.html";
				}
				break;
			}
			case "32655-13.html":
			{
				if (!hasQuestItems(player, SOUL_CORE_CONTAINING_EVIL_SPIRIT) && (getQuestItemsCount(player, RAGNA_ORCS_AMULET) >= RAGNA_ORCS_AMULET_COUNT))
				{
					qs.exitQuest(true, true);
					htmltext = event;
				}
				else
				{
					htmltext = "32655-14.html";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getRandomPartyMemberState(killer, 1, 2, npc);
		if (qs != null)
		{
			if (npc.getId() == VARANGKA)
			{
				if ((qs.getInt("cond") != 1))
				{
					return null;
				}
				qs.takeItems(PROTECTION_SOULS_PENDANT, 1);
				_altar.doDie(killer);
				_altar = null;
				_varangka = null;
				if (checkIfSpawned(VARANGKA + 1))
				{
					_varangkaMinion1.doDie(killer);
				}
				if (checkIfSpawned(VARANGKA + 2))
				{
					_varangkaMinion2.doDie(killer);
				}
				cancelQuestTimers("minion1");
				cancelQuestTimers("minion2");
				_varangkaMinion1 = null;
				_varangkaMinion2 = null;
				long respawn = Rnd.get(14400000, 28800000);
				saveGlobalQuestVar("VarangkaRespawn", String.valueOf(System.currentTimeMillis() + respawn));
				startQuestTimer("altarSpawn", respawn, null, null);
				return super.onKill(npc, killer, isSummon);
			}
			else if (npc.getId() == (VARANGKA + 1))
			{
				_varangkaMinion1 = null;
				startQuestTimer("minion1", Rnd.get(60000, 120000), npc, killer);
				return super.onKill(npc, killer, isSummon);
			}
			else if (npc.getId() == (VARANGKA + 2))
			{
				_varangkaMinion2 = null;
				startQuestTimer("minion2", Rnd.get(60000, 120000), npc, killer);
				return super.onKill(npc, killer, isSummon);
			}
			
			final int count = qs.getMemoStateEx(1) + 1;
			if ((count >= RAGNA_ORCS_KILLS_COUNT) && (getRandom(20) < ((count % 100) + 1)))
			{
				qs.setMemoStateEx(1, 0);
				qs.giveItems(SOUL_CORE_CONTAINING_EVIL_SPIRIT, 1);
				qs.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				qs.setMemoStateEx(1, count);
			}
			
			qs.giveItemRandomly(npc, RAGNA_ORCS_AMULET, 1, 0, MONSTERS.get(npc.getId()), true);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (qs == null)
		{
			return htmltext;
		}
		
		if (qs.isCreated())
		{
			htmltext = (player.getLevel() >= MIN_LEVEL) ? "32655-01.htm" : "32655-02.htm";
		}
		else if (qs.isStarted())
		{
			htmltext = !hasQuestItems(player, SOUL_CORE_CONTAINING_EVIL_SPIRIT, RAGNA_ORCS_AMULET) ? "32655-05.html" : "32655-06.html";
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		QuestState st = player.getQuestState(Q00311_ExpulsionOfEvilSpirits.class.getSimpleName());
		if (st == null)
		{
			return null;
		}
		
		if ((st.getQuestItemsCount(PROTECTION_SOULS_PENDANT) > 0) && (Rnd.get(100) < 20))
		{
			if ((_varangka == null) && !checkIfSpawned(VARANGKA))
			{
				_varangka = addSpawn(VARANGKA, 74914, -101922, -967, 0, false, 0);
				if ((_varangkaMinion1 == null) && !checkIfSpawned(VARANGKA + 1))
				{
					_varangkaMinion1 = addSpawn(VARANGKA + 1, 74914 + Rnd.get(10, 50), -101922 + Rnd.get(10, 50), -967, 0, false, 0);
				}
				if ((_varangkaMinion2 == null) && !checkIfSpawned(VARANGKA + 2))
				{
					_varangkaMinion2 = addSpawn(VARANGKA + 2, 74914 + Rnd.get(10, 50), -101922 + Rnd.get(10, 50), -967, 0, false, 0);
				}
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(ALTARZONE);
				for (L2Character c : zone.getCharactersInside())
				{
					if (c instanceof L2Attackable)
					{
						if ((c.getId() >= VARANGKA) && (c.getId() <= (VARANGKA + 2)))
						{
							c.setRunning();
							((L2Attackable) c).addDamageHate(player, 1, 99999);
							c.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						}
					}
				}
			}
		}
		else if (st.getQuestItemsCount(PROTECTION_SOULS_PENDANT) == 0)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new zoneCheck(player), 1000);
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new zoneCheck(character.getActingPlayer()), 1000);
		}
		return super.onEnterZone(character, zone);
	}
	
	private class zoneCheck implements Runnable
	{
		private final L2PcInstance _player;
		
		protected zoneCheck(L2PcInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_altar != null)
			{
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(ALTARZONE);
				if (zone.isCharacterInZone(_player))
				{
					QuestState st = _player.getQuestState(Q00311_ExpulsionOfEvilSpirits.class.getSimpleName());
					if (st == null)
					{
						castDebuff(_player);
						ThreadPoolManager.getInstance().scheduleGeneral(new zoneCheck(_player), 3000);
					}
					else if (st.getQuestItemsCount(PROTECTION_SOULS_PENDANT) == 0)
					{
						castDebuff(_player);
						ThreadPoolManager.getInstance().scheduleGeneral(new zoneCheck(_player), 3000);
					}
				}
			}
		}
		
		private void castDebuff(L2PcInstance player)
		{
			int skillId = 6148;
			int skillLevel = 1;
			if (player.getFirstEffect(skillId) != null)
			{
				player.stopSkillEffects(skillId);
			}
			L2Skill skill = SkillData.getInstance().getInfo(skillId, skillLevel);
			skill.getEffects(_altar, player);
			_altar.broadcastPacket(new MagicSkillUse(_altar, player, skillId, skillLevel, 1000, 0));
		}
	}
	
	private boolean checkIfSpawned(int npcId)
	{
		L2ZoneType zone = ZoneManager.getInstance().getZoneById(ALTARZONE);
		for (L2Character c : zone.getCharactersInside())
		{
			if (c.getId() == npcId)
			{
				return true;
			}
		}
		return false;
	}
}
