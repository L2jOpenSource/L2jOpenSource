/*
 * Copyright (C) 2004-2016 L2J DataPack
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
package quests.Q00727_HopeWithinTheDarkness;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.audio.Sound;
import l2r.gameserver.instancemanager.FortManager;
import l2r.gameserver.instancemanager.GlobalVariablesManager;
import l2r.gameserver.instancemanager.InstanceManager;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.L2Playable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2QuestGuardInstance;
import l2r.gameserver.model.entity.Castle;
import l2r.gameserver.model.entity.Fort;
import l2r.gameserver.model.entity.Instance;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.instancezone.InstanceWorld;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.quest.State;
import l2r.gameserver.network.NpcStringId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.clientpackets.Say2;
import l2r.gameserver.network.serverpackets.NpcSay;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vGodFather
 */
public final class Q00727_HopeWithinTheDarkness extends Quest
{
	public class CAUWorld extends InstanceWorld
	{
		public boolean underAttack = false;
		public boolean allMonstersDead = true;
	}
	
	public static class CastleDungeon
	{
		private final int INSTANCEID;
		private final int _wardenId;
		
		public CastleDungeon(int iId, int wardenId)
		{
			INSTANCEID = iId;
			_wardenId = wardenId;
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			long tmp = GlobalVariablesManager.getInstance().getLong("Castle_dungeon_" + String.valueOf(_wardenId), 0);
			return tmp;
		}
		
		public void setReEnterTime(long time)
		{
			GlobalVariablesManager.getInstance().set("Castle_dungeon_" + String.valueOf(_wardenId), time);
		}
	}
	
	private static final boolean debug = false;
	private static final boolean CHECK_FOR_CONTRACT = false;
	private static final long REENTER_INTERVAL = 14400000;
	private static final long INITIAL_SPAWN_DELAY = 120000; // Spawn NPC's and 1'st Wave bosses (2 min)
	private static final long WAVE_SPAWN_DELAY = 480000; // Spawn next wave's bosses (8 min)
	private static final long PRIVATE_SPAWN_DELAY = 5000; // Spawn monsters (3 min after boss had been spawned)
	
	public static final Logger _log = LoggerFactory.getLogger(Q00727_HopeWithinTheDarkness.class);
	
	private final Map<Integer, CastleDungeon> _castleDungeons = new HashMap<>(21);
	
	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;
	
	// NPC's
	private static final int NPC_KNIGHT = 36562;
	private static final int NPC_RANGER = 36563;
	private static final int NPC_MAGE = 36564;
	private static final int NPC_WARRIOR = 36565;
	
	//@formatter:off
	private static final int[] BOSSES = { 25653, 25654, 25655 };
	private static final int[] MONSTERS ={ 25656, 25657, 25658 };
	
	// Spawns
	public static final int[][] BOSSES_FIRST_WAVE = {{ BOSSES[0], 50943, -12224, -9321, 32768 }};
	
	public static final int[][] BOSSES_SECOND_WAVE = {{ BOSSES[1], 50943, -12224, -9321, 32768 }};
	
	public static final int[][] BOSSES_THIRD_WAVE =
	{
		{ BOSSES[2], 50943, -12004, -9321, 32768 },
		{ BOSSES[2], 50943, -12475, -9321, 32768 }
	};
	
	public static final int[][] MONSTERS_FIRST_WAVE =
	{
		{ MONSTERS[0], 50343, -12552, -9388, 32768 },
		{ MONSTERS[0], 50344, -12340, -9380, 32768 },
		{ MONSTERS[0], 50341, -12134, -9381, 32768 },
		{ MONSTERS[0], 50342, -11917, -9389, 32768 },
		{ MONSTERS[0], 50476, -12461, -9392, 32768 },
		{ MONSTERS[0], 50481, -12021, -9390, 32768 },
		{ MONSTERS[0], 50605, -12407, -9392, 32768 },
		{ MONSTERS[0], 50602, -12239, -9380, 32768 },
		{ MONSTERS[0], 50606, -12054, -9390, 32768 }
	};
	
	public static final int[][] MONSTERS_SECOND_WAVE =
	{
		{ MONSTERS[1], 50343, -12552, -9388, 32768 },
		{ MONSTERS[1], 50344, -12340, -9380, 32768 },
		{ MONSTERS[1], 50341, -12134, -9381, 32768 },
		{ MONSTERS[1], 50342, -11917, -9389, 32768 },
		{ MONSTERS[1], 50476, -12461, -9392, 32768 },
		{ MONSTERS[1], 50481, -12021, -9390, 32768 },
		{ MONSTERS[1], 50605, -12407, -9392, 32768 },
		{ MONSTERS[1], 50602, -12239, -9380, 32768 },
		{ MONSTERS[1], 50606, -12054, -9390, 32768}
	};
	
	public static final int[][] MONSTERS_THIRD_WAVE =
	{
		{ MONSTERS[1], 50343, -12552, -9388, 32768 },
		{ MONSTERS[1], 50344, -12340, -9380, 32768 },
		{ MONSTERS[1], 50341, -12134, -9381, 32768 },
		{ MONSTERS[1], 50342, -11917, -9389, 32768 },
		{ MONSTERS[2], 50476, -12461, -9392, 32768 },
		{ MONSTERS[2], 50481, -12021, -9390, 32768 },
		{ MONSTERS[2], 50605, -12407, -9392, 32768 },
		{ MONSTERS[2], 50602, -12239, -9380, 32768 },
		{ MONSTERS[2], 50606, -12054, -9390, 32768 }
	};
	
	// NPCs'_SPAWNS (id, x, y, z, heading)
	public static final int[][] NPCS =
	{
		{ NPC_WARRIOR, 49093, -12077, -9395, 0 },
		{ NPC_RANGER, 49094, -12238, -9386, 0 },
		{ NPC_MAGE, 49093, -12401, -9388, 0 },
		{ NPC_KNIGHT, 49232, -12239, -9386, 0 }
	};
	//@formatter:on
	
	// Strings
	private static final NpcStringId[] NPC_INJURED_FSTRINGID =
	{
		NpcStringId.YOUR_MIND_IS_GOING_BLANK,
		NpcStringId.YOUR_MIND_IS_GOING_BLANK
	};
	private static final NpcStringId[] NPC_DIE_FSTRINGID =
	{
		NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH,
		NpcStringId.I_CANT_STAND_IT_ANYMORE_AAH,
		NpcStringId.KYAAAK,
		NpcStringId.GASP_HOW_CAN_THIS_BE
	};
	public static final NpcStringId NPC_WIN_FSTRINGID = NpcStringId.YOUVE_DONE_IT_WE_BELIEVED_IN_YOU_WARRIOR_WE_WANT_TO_SHOW_OUR_SINCERITY_THOUGH_IT_IS_SMALL_PLEASE_GIVE_ME_SOME_OF_YOUR_TIME;
	private static final NpcStringId BOSS_DIE_FSTRINGID = NpcStringId.HOW_DARE_YOU;
	private static final NpcStringId[] BOSS_SPAWN_FSTRINGID =
	{
		NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES,
		NpcStringId.ILL_RIP_THE_FLESH_FROM_YOUR_BONES,
		NpcStringId.YOULL_FLOUNDER_IN_DELUSION_FOR_THE_REST_OF_YOUR_LIFE
	};
	
	// Buffs
	private static Map<Integer, SkillHolder> NPC_BUFFS = new HashMap<>();
	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);
	
	public Q00727_HopeWithinTheDarkness()
	{
		super(727, Q00727_HopeWithinTheDarkness.class.getSimpleName(), "Hope Within The Darkness");
		_castleDungeons.put(36403, new CastleDungeon(80, 36403));
		_castleDungeons.put(36404, new CastleDungeon(81, 36404));
		_castleDungeons.put(36405, new CastleDungeon(82, 36405));
		_castleDungeons.put(36406, new CastleDungeon(83, 36406));
		_castleDungeons.put(36407, new CastleDungeon(84, 36407));
		_castleDungeons.put(36408, new CastleDungeon(85, 36408));
		_castleDungeons.put(36409, new CastleDungeon(86, 36409));
		_castleDungeons.put(36410, new CastleDungeon(87, 36410));
		_castleDungeons.put(36411, new CastleDungeon(88, 36411));
		
		NPC_BUFFS.put(NPC_KNIGHT, new SkillHolder(5970, 1));
		NPC_BUFFS.put(NPC_RANGER, new SkillHolder(5971, 1));
		NPC_BUFFS.put(NPC_MAGE, new SkillHolder(5972, 1));
		NPC_BUFFS.put(NPC_WARRIOR, new SkillHolder(5973, 1));
		
		addStartNpc(_castleDungeons.keySet());
		addTalkId(_castleDungeons.keySet());
		
		for (int i = NPC_KNIGHT; i <= NPC_WARRIOR; i++)
		{
			addSpawnId(i);
			addKillId(i);
			addAttackId(i);
			addTalkId(i);
			addFirstTalkId(i);
		}
		
		addSpawnId(BOSSES);
		addKillId(BOSSES);
		addAttackId(BOSSES);
		
		addKillId(MONSTERS);
		addAttackId(MONSTERS);
	}
	
	private String checkEnterConditions(L2PcInstance player, L2Npc npc)
	{
		if (debug || player.isGM())
		{
			return null;
		}
		
		Castle castle = npc.getCastle();
		CastleDungeon dungeon = _castleDungeons.get(npc.getId());
		boolean haveContract = false;
		
		if ((castle == null) || (dungeon == null))
		{
			return "CastleWarden-03.htm";
		}
		
		// check if castle have contract with fortress
		if (CHECK_FOR_CONTRACT)
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getCastleId() == castle.getResidenceId())
				{
					haveContract = true;
					break;
				}
			}
			
			if (!haveContract)
			{
				return "CastleWarden-13a.htm";
			}
		}
		
		QuestState st = player.getQuestState(getName());
		
		// check if player have quest
		if ((st == null) || (st.getCond() < 1))
		{
			// check if player is from clan, that owns castle
			if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getResidenceId()))
			{
				return "CastleWarden-08.htm";
			}
			
			if (player.getLevel() >= 80)
			{
				return "CastleWarden-06.htm";
			}
			
			return "CastleWarden-07.htm";
		}
		
		L2Party party = player.getParty();
		
		if (party == null)
		{
			return "CastleWarden-09.htm";
		}
		
		if (party.getLeader() != player)
		{
			return getHtm(player, player.getHtmlPrefix(), "CastleWarden-10.htm").replace("%leader%", party.getLeader().getName());
		}
		
		for (L2PcInstance partyMember : party.getMembers())
		{
			st = partyMember.getQuestState(getName());
			
			// check if each party member has quest
			if ((st == null) || (st.getCond() < 1))
			{
				return getHtm(player, player.getHtmlPrefix(), "CastleWarden-12.htm").replace("%player%", partyMember.getName());
			}
			
			if ((player.getClan() == null) || (player.getClan().getCastleId() != castle.getResidenceId()))
			{
				return getHtm(player, player.getHtmlPrefix(), "CastleWarden-11.htm").replace("%player%", partyMember.getName());
			}
			
			// check if each party member not very far from leader
			if (!Util.checkIfInRange(1000, player, partyMember, true))
			{
				return getHtm(player, player.getHtmlPrefix(), "CastleWarden-17.htm").replace("%player%", partyMember.getName());
			}
		}
		
		if (dungeon.getReEnterTime() > System.currentTimeMillis())
		{
			return "CastleWarden-18.htm";
		}
		
		return null;
	}
	
	protected String enterInstance(L2PcInstance player, String template, Location coords, CastleDungeon dungeon, String ret)
	{
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof CAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ENTERED_ANOTHER_INSTANT_ZONE_THEREFORE_YOU_CANNOT_ENTER_CORRESPONDING_DUNGEON));
				return "";
			}
			teleportPlayer(player, coords, world.getInstanceId());
			return "";
		}
		// New instance
		if (ret != null)
		{
			return ret;
		}
		
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setExitLoc(new Location(player));
		world = new CAUWorld();
		world.setInstanceId(instanceId);
		world.setTemplateId(dungeon.getInstanceId());
		world.setStatus(0);
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTER_INTERVAL);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Castle HopeWithinTheDarkness started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs((CAUWorld) world), INITIAL_SPAWN_DELAY);
		
		if (debug || player.isGM())
		{
			teleportPlayer(player, coords, instanceId);
			world.addAllowed(player.getObjectId());
			if (player.getQuestState(getName()) == null)
			{
				newQuestState(player);
			}
			
			player.getQuestState(getName()).setMemoState(2);
		}
		else
		{
			L2Party party = player.getParty();
			if (party == null)
			{
				return "CastleWarden-09.htm";
			}
			
			for (L2PcInstance partyMember : party.getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.addAllowed(partyMember.getObjectId());
				if (partyMember.getQuestState(getName()) == null)
				{
					newQuestState(partyMember);
				}
				
				partyMember.getQuestState(getName()).setMemoState(2);
			}
		}
		return getHtm(player, player.getHtmlPrefix(), "CastleWarden-13.htm").replace("%clan%", player.getClan().getName());
	}
	
	// Spawns npc's and bosses
	private class spawnNpcs implements Runnable
	{
		private final CAUWorld _world;
		
		public spawnNpcs(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (_world.getStatus() == 0)
			{
				for (int[] spawn : NPCS)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
				for (int[] spawn : BOSSES_FIRST_WAVE)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs(_world), WAVE_SPAWN_DELAY);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
			}
			else if (_world.getStatus() == 1)
			{
				for (int[] spawn : BOSSES_SECOND_WAVE)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnNpcs(_world), WAVE_SPAWN_DELAY);
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
			}
			else if (_world.getStatus() == 2)
			{
				for (int[] spawn : BOSSES_THIRD_WAVE)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
				
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnPrivates(_world), PRIVATE_SPAWN_DELAY);
			}
		}
	}
	
	// Spawns monsters (minions)
	private class spawnPrivates implements Runnable
	{
		private final CAUWorld _world;
		
		public spawnPrivates(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			if (_world.getStatus() == 0)
			{
				for (int[] spawn : MONSTERS_FIRST_WAVE)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
				
				_world.underAttack = true;
			}
			else if (_world.getStatus() == 1)
			{
				for (int[] spawn : MONSTERS_SECOND_WAVE)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
			}
			else if (_world.getStatus() == 2)
			{
				for (int[] spawn : MONSTERS_THIRD_WAVE)
				{
					addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, _world.getInstanceId());
				}
			}
			
			_world.setStatus(_world.getStatus() + 1);
		}
	}
	
	// Manages win event
	private class Win implements Runnable
	{
		private final CAUWorld _world;
		private final L2PcInstance _player;
		
		public Win(CAUWorld world, L2PcInstance player)
		{
			_world = world;
			_player = player;
		}
		
		@Override
		public void run()
		{
			_world.underAttack = false;
			
			Instance inst = InstanceManager.getInstance().getInstance(_world.getInstanceId());
			for (L2Npc _npc : inst.getNpcs())
			{
				if ((_npc != null) && ((_npc.getId() >= NPC_KNIGHT) && (_npc.getId() <= NPC_WARRIOR)))
				{
					cancelQuestTimer("check_for_foes", _npc, null);
					cancelQuestTimer("buff", _npc, null);
					
					if (_npc.getId() == NPC_KNIGHT)
					{
						_npc.broadcastPacket(new NpcSay(_npc.getObjectId(), Say2.SHOUT, _npc.getId(), NPC_WIN_FSTRINGID));
					}
				}
			}
			
			if (_player != null)
			{
				L2Party party = _player.getParty();
				
				if (party == null)
				{
					rewardPlayer(_player);
				}
				else
				{
					for (L2PcInstance partyMember : party.getMembers())
					{
						if ((partyMember != null) && (partyMember.getInstanceId() == _player.getInstanceId()))
						{
							rewardPlayer(partyMember);
						}
					}
				}
			}
		}
	}
	
	public void rewardPlayer(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if ((st != null) && (st.getMemoState() == 2))
		{
			st.setMemoState(3);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			Location tele = new Location(48163, -12195, -9140);
			return enterInstance(player, "Castlepailaka.xml", tele, _castleDungeons.get(npc.getId()), checkEnterConditions(player, npc));
		}
		else if (event.equalsIgnoreCase("suicide"))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			tmpworld.setStatus(5);
			Instance inst = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			if (inst != null)
			{
				for (L2Npc _npc : inst.getNpcs())
				{
					if ((_npc != null) && ((_npc.getId() >= NPC_KNIGHT) && (_npc.getId() <= NPC_WARRIOR)))
					{
						cancelQuestTimer("check_for_foes", _npc, null);
						cancelQuestTimer("buff", _npc, null);
						
						if (!_npc.isDead())
						{
							_npc.doDie(null);
						}
					}
				}
				
				// Destroy instance after 5 minutes
				inst.setDuration(5 * 60000);
				inst.setEmptyDestroyTime(0);
			}
			
			return null;
		}
		else if (event.equalsIgnoreCase("buff"))
		{
			Collection<L2PcInstance> players = npc.getKnownList().getKnownPlayers().values();
			for (L2PcInstance pl : players)
			{
				if ((pl != null) && Util.checkIfInRange(75, npc, pl, false) && (NPC_BUFFS.get(npc.getId()) != null))
				{
					npc.setTarget(pl);
					npc.doCast(NPC_BUFFS.get(npc.getId()).getSkill());
				}
			}
			startQuestTimer("buff", 120000, npc, null);
			return null;
		}
		else if (event.equalsIgnoreCase("check_for_foes"))
		{
			if (npc.getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				for (L2Character foe : npc.getKnownList().getKnownCharactersInRadius(npc.getAggroRange()))
				{
					if ((foe instanceof L2Attackable) && !(foe instanceof L2QuestGuardInstance))
					{
						((L2QuestGuardInstance) npc).addDamageHate(foe, 0, 999);
						((L2QuestGuardInstance) npc).getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, foe, null);
					}
				}
			}
			
			startQuestTimer("check_for_foes", 5000, npc, null);
			return null;
		}
		
		final QuestState st = getQuestState(player, true);
		if (event.equalsIgnoreCase("CastleWarden-05.htm"))
		{
			if (st.getCond() == 0)
			{
				st.setCond(1);
				st.setState(State.STARTED);
				playSound(player, Sound.ITEMSOUND_QUEST_ACCEPT);
			}
		}
		else if (event.equalsIgnoreCase("CastleWarden-15.htm"))
		{
			playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(true);
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			
			if (world.underAttack)
			{
				return "Victim-02.htm";
			}
			else if (world.getStatus() == 4)
			{
				return "Victim-03.htm";
			}
			else
			{
				return "Victim-01.htm";
			}
		}
		
		return null;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = getQuestState(player, true);
		
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if ((tmpworld != null) && (tmpworld instanceof CAUWorld))
			{
				CAUWorld world = (CAUWorld) tmpworld;
				
				world.removeAllowed(player.getObjectId());
				final Instance inst = InstanceManager.getInstance().getInstance(world.getInstanceId());
				teleportPlayer(player, inst.getExitLoc(), 0);
				return null;
			}
		}
		
		if ((player.getClan() == null) || (npc.getCastle() == null) || (player.getClan().getCastleId() != npc.getCastle().getResidenceId()))
		{
			return "CastleWarden-03.htm";
		}
		else if (st != null)
		{
			int npcId = npc.getId();
			int cond = 0;
			if (st.getState() == State.CREATED)
			{
				st.setCond(0);
			}
			else
			{
				cond = st.getCond();
			}
			if (_castleDungeons.containsKey(npcId) && (cond == 0))
			{
				if (player.getLevel() >= 80)
				{
					htmltext = "CastleWarden-01.htm";
				}
				else
				{
					htmltext = "CastleWarden-04.htm";
					st.exitQuest(true);
				}
			}
			else if (_castleDungeons.containsKey(npcId) && (cond > 0) && (st.getState() == State.STARTED))
			{
				if ((cond == 1) && (st.getMemoState() != 3))
				{
					htmltext = "CastleWarden-15.htm";
				}
				else if (st.getMemoState() == 3)
				{
					playSound(player, Sound.ITEMSOUND_QUEST_FINISH);
					st.giveItems(KNIGHT_EPALUETTE, 159);
					st.exitQuest(true);
					htmltext = "CastleWarden-16.htm";
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			if (npc.getCurrentHp() <= (npc.getMaxHp() * 0.1))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NPC_INJURED_FSTRINGID[1]));
			}
			else if (npc.getCurrentHp() <= (npc.getMaxHp() * 0.4))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NPC_INJURED_FSTRINGID[0]));
			}
			
			return null;
		}
		
		if (player != null)
		{
			L2Playable attacker = (isPet ? player.getSummon() : player);
			if ((attacker.getLevel() - npc.getLevel()) >= 9)
			{
				if ((attacker.getBuffCount() > 0) || (attacker.getDanceCount() > 0))
				{
					npc.setTarget(attacker);
					npc.doSimultaneousCast(RAID_CURSE.getSkill());
				}
				else if (player.getParty() != null)
				{
					for (L2PcInstance pmember : player.getParty().getMembers())
					{
						if ((pmember.getBuffCount() > 0) || (pmember.getDanceCount() > 0))
						{
							npc.setTarget(pmember);
							npc.doSimultaneousCast(RAID_CURSE.getSkill());
						}
					}
				}
			}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NPC_DIE_FSTRINGID[npc.getId() - 36562]));
			
			// All other friendly NPC's do suicide - start timer
			startQuestTimer("suicide", 1500, npc, null);
			cancelQuestTimer("check_for_foes", npc, null);
			cancelQuestTimer("buff", npc, null);
			return null;
		}
		
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			if (Util.contains(BOSSES, npc.getId()))
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), BOSS_DIE_FSTRINGID));
			}
			
			if ((tmpworld.getStatus() == 3) && (Util.contains(BOSSES, npc.getId()) || Util.contains(MONSTERS, npc.getId())))
			{
				world.allMonstersDead = true;
				Instance inst = InstanceManager.getInstance().getInstance(tmpworld.getInstanceId());
				
				if (inst != null)
				{
					for (L2Npc _npc : inst.getNpcs())
					{
						if ((_npc != null) && !_npc.isDead() && (Util.contains(BOSSES, _npc.getId()) || Util.contains(MONSTERS, _npc.getId())))
						{
							world.allMonstersDead = false;
							break;
						}
					}
					
					if (world.allMonstersDead)
					{
						tmpworld.setStatus(4);
						
						// Destroy instance after 5 minutes
						inst.setDuration(5 * 60000);
						inst.setEmptyDestroyTime(0);
						ThreadPoolManager.getInstance().scheduleGeneral(new Win(world, player), 1500);
					}
				}
			}
		}
		
		return null;
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		// buff players every two minutes, check for foes in aggro range
		if ((npc.getId() >= NPC_KNIGHT) && (npc.getId() <= NPC_WARRIOR))
		{
			startQuestTimer("buff", 120000, npc, null);
			startQuestTimer("check_for_foes", 120000, npc, null);
			
			if (npc.getId() == NPC_KNIGHT)
			{
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), NpcStringId.WARRIORS_HAVE_YOU_COME_TO_HELP_THOSE_WHO_ARE_IMPRISONED_HERE));
			}
		}
		
		else if (Arrays.binarySearch(BOSSES, npc.getId()) >= 0)
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getId(), BOSS_SPAWN_FSTRINGID[Arrays.binarySearch(BOSSES, npc.getId())]));
		}
		
		return null;
	}
}
